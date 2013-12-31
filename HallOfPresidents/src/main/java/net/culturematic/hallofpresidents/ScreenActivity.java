package net.culturematic.hallofpresidents;

import android.app.Activity;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ScreenActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final AssetLoader assetLoader = new AssetLoader(this);
        mSurfaceView = new SurfaceView(this);

        mRoomPickerView = new ListView(this);
        mRoomPickerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Point gameDimensions = getBitmapDimensions();

                mInputEvents = new InputEvents();
                mSurfaceView.setOnTouchListener(mInputEvents);
                setContentView(mSurfaceView);
                mGameLoop = new GameLoop(
                        mSurfaceView.getHolder(),
                        gameDimensions,
                        assetLoader,
                        null
                );
                mGameLoop.start();
            }
        });

        try {
            JSONObject catalogObject = assetLoader.loadJSONObject("catalog.js");
            JSONArray catalog = catalogObject.getJSONArray("catalog");
            RoomCatalogAdapter catalogAdapter = new RoomCatalogAdapter(getLayoutInflater(), catalog);
            mRoomPickerView.setAdapter(catalogAdapter);
            setContentView(mRoomPickerView);
        } catch (JSONException e) {
            throw new RuntimeException("Can't parse catalog", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (null != mGameLoop) {
            @SuppressWarnings("unused")
            Game.GameState state = mGameLoop.pause(); // At some point, we'll save this jonk.
            mGameLoop = null;
        }
    }

    @SuppressWarnings("deprecation")
    public Point getBitmapDimensions() {
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();  // deprecated
        int height = display.getHeight();  // deprecated
        return new Point(width, height);
    }

    private class GameLoop extends Thread {
        public GameLoop(SurfaceHolder holder,
                        Point gameDimensions,
                        AssetLoader assetLoader,
                        Game.GameState gameState) {
            mRunning = true;
            mDimensions = gameDimensions;
            mAssetLoader = assetLoader;
            mGameState = gameState;
            mHolder = holder;
        }

        public Game.GameState pause() {
            mRunning = false;
            while (true) {
                try {
                    join();
                    break;
                } catch (InterruptedException e) {
                    // keep trying
                }
            }
            return getGameState();
        }

        private synchronized Game.GameState getGameState() {
            return mGameState;
        }

        private synchronized void setGameState(Game.GameState gameState) {
            mGameState = gameState;
        }

        @Override
        public void run() {
            Game.GameState gameState = getGameState();
            setGameState(null);

            final Rect boundsRect = new Rect();
            final Bitmap displayBitmap = Bitmap.createBitmap(mDimensions.x, mDimensions.y, Bitmap.Config.RGB_565);

            final Rect gameDimensions = new Rect(0, 0, mDimensions.x, mDimensions.y);
            final Game game = new Game(displayBitmap, gameDimensions, gameState, mAssetLoader);
            final InputEvents.TouchSpot[] touchSpots = new InputEvents.TouchSpot[InputEvents.MAX_TOUCH_SPOTS];

            while (mRunning) {
                if (! mHolder.getSurface().isValid()) {
                    continue;
                }
                long timeMillis = System.currentTimeMillis();
                mInputEvents.getPointsDown(touchSpots);
                game.update(timeMillis, touchSpots);
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
            }// while
            setGameState(new Game.GameState());
        } // run()

        private final Point mDimensions;
        private final SurfaceHolder mHolder;
        private final AssetLoader mAssetLoader;
        private Game.GameState mGameState;
        private volatile boolean mRunning;
    } // class

    private InputEvents mInputEvents;
    private SurfaceView mSurfaceView;
    private ListView mRoomPickerView;
    private GameLoop mGameLoop;
}
