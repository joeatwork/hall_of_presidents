package net.culturematic.hallofpresidents;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class ScreenActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, Config.MIXPANEL_TOKEN);
        mAssetLoader = new AssetLoader(this);
        mSurfaceView = new SurfaceView(this);
        mRoomPickerView = new ListView(this);

        // MIXPANEL STUFF

        final String oldPeopleId = mixpanel.getPeople().getDistinctId();
        if (null == oldPeopleId) {
            final String newPeopleId = UUID.randomUUID().toString();
            mixpanel.getPeople().identify(newPeopleId);
        }
        mixpanel.getPeople().initPushHandling(Config.GOOGLE_API_PROJECT);
        mixpanel.getPeople().set("App Version", BuildConfig.VERSION_CODE);
        try {
            final JSONObject props = new JSONObject();
            props.put("App Version", BuildConfig.VERSION_CODE);
            mixpanel.registerSuperProperties(props);
        } catch (final JSONException e) {
            Log.e(LOGTAG, "Impossible exception", e);
        }

        // Should be mMixpanel.getPeople().setOnce(Open Date)
        mixpanel.track("App Opened", null);
        final Date now = new Date();
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        mixpanel.getPeople().set("Last Open", dateFormat.format(now));
        mixpanel.getPeople().set("Identifier", mixpanel.getPeople().getDistinctId());
        mixpanel.getPeople().set("Debug Build", BuildConfig.DEBUG);

        if (BuildConfig.DEBUG) {
            try {
                final JSONObject props = new JSONObject();
                props.put("Debug", BuildConfig.DEBUG);
                mixpanel.registerSuperProperties(props);
            } catch (final JSONException e) {
                throw new RuntimeException("Impossible exception", e);
            }
        }

        showRoomPicker();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (null != mAlreadyShowingToast) {
            mAlreadyShowingToast.cancel();
        }

        if (null != mGameLoop) {
            final LevelState levelState = mGameLoop.pause();
            mGameLoop = null;
            saveRoomState(levelState);
        }
    }

    @SuppressWarnings("deprecation")
    public Point getBitmapDimensions() {
        final Display display = getWindowManager().getDefaultDisplay();
        final int width = display.getWidth();  // deprecated
        final int height = display.getHeight();  // deprecated
        return new Point(width, height);
    }

    private LevelCatalog loadRoomCatalog(AssetLoader assetLoader) {
        try {
            final SharedPreferences prefs = getSharedPreferences(ROOM_STATE_PREFS_NAME, Context.MODE_PRIVATE);
            final JSONObject catalogObject = assetLoader.loadJSONObject("catalog.js");
            final LevelCatalog ret = LevelCatalog.loadFromJSON(catalogObject);
            for (int i = 0; i < ret.size(); i++) {
                final LevelCatalogItem item = ret.get(i);
                LevelState savedState = null;
                final String savedString = prefs.getString(item.getFullPath(), null);
                if (null != savedString) {
                    final JSONObject savedDesc = new JSONObject(savedString);
                    savedState = LevelState.readJSON(savedDesc);
                }
                ret.putSavedState(i, savedState);
            }
            return ret;
        } catch (JSONException e) {
            throw new RuntimeException("Can't load room catalog", e);
        }
    }

    private void showRoomPicker() {
        if (null != mGameLoop) {
            // Probably already paused, but there is a slight race condition
            // since this can be called during the tail end of the game loop.
            mGameLoop.pause();
            mGameLoop = null;
        }

        final LevelCatalog catalog = loadRoomCatalog(mAssetLoader);
        mRoomPickerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIx, long itemId) {
                final LevelCatalogAdapter adapter =
                        (LevelCatalogAdapter) adapterView.getAdapter();
                LevelState savedState = catalog.getSavedState(itemIx);
                if (null == savedState) {
                    final LevelCatalogItem item = adapter.getItem(itemIx);
                    savedState = new LevelState(item);
                }
                showGame(savedState);
            }
        });

        LevelCatalogAdapter catalogAdapter = new LevelCatalogAdapter(getLayoutInflater(), catalog);
        mRoomPickerView.setAdapter(catalogAdapter);

        setContentView(mRoomPickerView);
    }

    private void showGame(LevelState levelState) {
        final Point gameDimensions = getBitmapDimensions();
        mInputEvents = new InputEvents();
        mSurfaceView.setOnTouchListener(mInputEvents);

        // TODO This shouldn't really work? Should probably be a separate activity,
        // or (even better) a screen.
        setContentView(mSurfaceView);
        mGameLoop = new GameLoop(
                mSurfaceView.getHolder(),
                gameDimensions,
                mAssetLoader,
                levelState
        );
        mGameLoop.start();
    }

    private void saveRoomState(LevelState levelState) {
        final SharedPreferences prefs = getSharedPreferences(ROOM_STATE_PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        final String storagePath = levelState.getLevelCatalogItem().getFullPath();
        final JSONObject storagePayload = levelState.toJSON();
        editor.putString(storagePath, storagePayload.toString());
        editor.commit();
    }

    private void showToast(CharSequence toastMessage) {
        if (null == mLastToastMessage || ! mLastToastMessage.equals(toastMessage)) {
            if (null != mAlreadyShowingToast) {
                mAlreadyShowingToast.cancel();
            }
            mLastToastMessage = toastMessage;
            mAlreadyShowingToast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG);
            mAlreadyShowingToast.show();
        }
    }

    private class GameLoop extends Thread {
        public GameLoop(SurfaceHolder holder,
                        Point gameDimensions,
                        AssetLoader assetLoader,
                        LevelState levelState) {
            mRunning = true;
            mDimensions = gameDimensions;
            mAssetLoader = assetLoader;
            mLevelState = levelState;
            mHolder = holder;
        }

        public LevelState pause() {
            mRunning = false;
            while (true) {
                try {
                    join();
                    break;
                } catch (InterruptedException e) {
                    // keep trying
                }
            }
            return mLevelState;
        }

        @Override
        public void run() {
            final Rect boundsRect = new Rect();
            final Bitmap displayBitmap = Bitmap.createBitmap(mDimensions.x, mDimensions.y, Bitmap.Config.RGB_565);
            final MixpanelAPI mixpanel = MixpanelAPI.getInstance(getApplicationContext(), Config.MIXPANEL_TOKEN);
            final Analytics analytics = new Analytics(mixpanel);

            final Rect gameDimensions = new Rect(0, 0, mDimensions.x, mDimensions.y);
            final Game game = new Game(displayBitmap, gameDimensions, mLevelState, mAssetLoader, analytics);
            final InputEvents.TouchSpot[] touchSpots = new InputEvents.TouchSpot[InputEvents.MAX_TOUCH_SPOTS];


            while (mRunning) {
                if (! mHolder.getSurface().isValid()) {
                    continue;
                }
                long timeMillis = System.currentTimeMillis();
                mInputEvents.getPointsDown(touchSpots);
                boolean isDone = game.update(timeMillis, touchSpots);
                Canvas canvas = null;
                try {
                    canvas = mHolder.lockCanvas();

                    assert canvas != null;
                    canvas.getClipBounds(boundsRect);

                    canvas.drawBitmap(displayBitmap, null, boundsRect, null);
                } finally {
                    if (null != canvas) {
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }
                final String helpText = game.getHelpMessage();
                if (null != helpText) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(helpText);
                        }
                    });
                }

                if (isDone) {
                    mRunning = false;
                }
            }// while

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LevelState levelState = pause();
                    saveRoomState(levelState);
                    showRoomPicker();
                }
            });
        } // run()

        private final Point mDimensions;
        private final SurfaceHolder mHolder;
        private final AssetLoader mAssetLoader;
        private final LevelState mLevelState;
        private volatile boolean mRunning;
    } // class

    private InputEvents mInputEvents;
    private SurfaceView mSurfaceView;
    private ListView mRoomPickerView;
    private GameLoop mGameLoop;
    private AssetLoader mAssetLoader;
    private CharSequence mLastToastMessage;
    private Toast mAlreadyShowingToast;

    private static final String LOGTAG = "MixpanelAPI ScreenActivity";
    private static final String ROOM_STATE_PREFS_NAME = "RoomStates";
}
