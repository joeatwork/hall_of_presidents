package net.culturematic.hallofpresidents;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

public class ScreenActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAssetLoader = new AssetLoader(this);
        mSurfaceView = new SurfaceView(this);
        mRoomPickerView = new ListView(this);

        showRoomPicker();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (null != mGameLoop) {
            final RoomState roomState = mGameLoop.pause();
            mGameLoop = null;
            saveRoomState(roomState);
        }
    }

    @SuppressWarnings("deprecation")
    public Point getBitmapDimensions() {
        final Display display = getWindowManager().getDefaultDisplay();
        final int width = display.getWidth();  // deprecated
        final int height = display.getHeight();  // deprecated
        return new Point(width, height);
    }

    private RoomCatalog loadRoomCatalog(AssetLoader assetLoader) {
        try {
            final SharedPreferences prefs = getSharedPreferences(ROOM_STATE_PREFS_NAME, Context.MODE_PRIVATE);
            final JSONObject catalogObject = assetLoader.loadJSONObject("catalog.js");
            final RoomCatalog ret = RoomCatalog.loadFromJSON(catalogObject);
            for (int i = 0; i < ret.size(); i++) {
                final RoomCatalogItem item = ret.get(i);
                RoomState savedState = null;
                final String savedString = prefs.getString(item.getFullPath(), null);
                if (null != savedString) {
                    final JSONObject savedDesc = new JSONObject(savedString);
                    savedState = RoomState.readJSON(savedDesc);
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

        // TODO Waaay to much IO for here. Need a loading interaction while this is happening.
        final RoomCatalog catalog = loadRoomCatalog(mAssetLoader);
        mRoomPickerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIx, long itemId) {
                final RoomCatalogAdapter adapter =
                        (RoomCatalogAdapter) adapterView.getAdapter();
                RoomState savedState = catalog.getSavedState(itemIx);
                if (null == savedState) {
                    final RoomCatalogItem item = adapter.getItem(itemIx);
                    savedState = new RoomState(item);
                }
                showGame(savedState);
            }
        });

        RoomCatalogAdapter catalogAdapter = new RoomCatalogAdapter(getLayoutInflater(), catalog);
        mRoomPickerView.setAdapter(catalogAdapter);

        // TODO This shouldn't really work?
        setContentView(mRoomPickerView);
    }

    private void showGame(RoomState roomState) {
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
                roomState
        );
        mGameLoop.start();
    }

    private void saveRoomState(RoomState roomState) {
        final SharedPreferences prefs = getSharedPreferences(ROOM_STATE_PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        final String storagePath = roomState.getRoomCatalogItem().getFullPath();
        final JSONObject storagePayload = roomState.toJSON();
        editor.putString(storagePath, storagePayload.toString());
        editor.commit();
    }

    private class GameLoop extends Thread {
        public GameLoop(SurfaceHolder holder,
                        Point gameDimensions,
                        AssetLoader assetLoader,
                        RoomState roomState) {
            mRunning = true;
            mDimensions = gameDimensions;
            mAssetLoader = assetLoader;
            mRoomState = roomState;
            mHolder = holder;
        }

        public RoomState pause() {
            mRunning = false;
            while (true) {
                try {
                    join();
                    break;
                } catch (InterruptedException e) {
                    // keep trying
                }
            }
            return mRoomState;
        }

        @Override
        public void run() {
            final Rect boundsRect = new Rect();
            final Bitmap displayBitmap = Bitmap.createBitmap(mDimensions.x, mDimensions.y, Bitmap.Config.RGB_565);

            final Rect gameDimensions = new Rect(0, 0, mDimensions.x, mDimensions.y);
            final Game game = new Game(displayBitmap, gameDimensions, mRoomState, mAssetLoader);
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
                if (isDone) {
                    mRunning = false;
                }
            }// while

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RoomState roomState = pause();
                    saveRoomState(roomState);
                    showRoomPicker();
                }
            });
        } // run()

        private final Point mDimensions;
        private final SurfaceHolder mHolder;
        private final AssetLoader mAssetLoader;
        private final RoomState mRoomState;
        private volatile boolean mRunning;
    } // class

    private InputEvents mInputEvents;
    private SurfaceView mSurfaceView;
    private ListView mRoomPickerView;
    private GameLoop mGameLoop;
    private AssetLoader mAssetLoader;

    private static final String ROOM_STATE_PREFS_NAME = "RoomStates";
}
