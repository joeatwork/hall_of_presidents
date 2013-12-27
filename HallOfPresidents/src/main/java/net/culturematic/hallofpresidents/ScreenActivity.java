package net.culturematic.hallofpresidents;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class ScreenActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mSurfaceView = new SurfaceView(this);
        mInputEvents = new InputEvents();
        mSurfaceView.setOnTouchListener(mInputEvents);
        setContentView(mSurfaceView);
    }

    @Override
    public void onResume() {
        super.onResume();

        final Point gameDimensions = getBitmapDimensions();
        AssetLoader assetLoader = new AssetLoader(getAssets());
        RoomLoader roomLoader = new RoomLoader(assetLoader);

        // TODO refactor- All loading should come from the same place, which isn't here.
        final Bitmap heroSprites = assetLoader.loadBitmap("hero_sprites_128x128.png");
        final Character hero = new Character(heroSprites, assetLoader);

        final Bitmap dpad = assetLoader.loadBitmap("widget_dpad.png");
        final Bitmap button = assetLoader.loadBitmap("widget_button.png");
        final UIControls controls = new UIControls(dpad, button);

        mGameLoop = new GameLoop(
            mSurfaceView.getHolder(),
            gameDimensions,
            hero,
            controls,
            roomLoader,
            null
        );
        mGameLoop.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        @SuppressWarnings("unused")
        Game.GameState state = mGameLoop.pause(); // At some point, we'll save this jonk.
        mGameLoop = null;
    }

    @SuppressWarnings("deprecation")
    public Point getBitmapDimensions() {
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();  // deprecated
        int height = display.getHeight();  // deprecated
        return new Point(width, height);
    }

    private class GameLoop extends Thread {
        // TODO this unwieldy constructor is a smell.
        public GameLoop(SurfaceHolder holder,
                        Point gameDimensions,
                        Character hero,
                        UIControls controls,
                        RoomLoader roomLoader,
                        Game.GameState gameState) {
            mRunning = true;
            mDimensions = gameDimensions;
            mGameState = gameState;
            mHolder = holder;
            mHero = hero;
            mControls = controls;
            mRoomLoader = roomLoader;
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

            int frameCount = 0;
            final Rect boundsRect = new Rect();
            final Bitmap screen = Bitmap.createBitmap(mDimensions.x, mDimensions.y, Bitmap.Config.RGB_565);

            final Rect gameDimensions = new Rect(0, 0, mDimensions.x, mDimensions.y);
            final Game game = new Game(screen, gameState, mRoomLoader, mHero, mControls, gameDimensions);
            final InputEvents.TouchSpot[] touchSpots = new InputEvents.TouchSpot[InputEvents.MAX_TOUCH_SPOTS];

            while (mRunning) {
                if (! mHolder.getSurface().isValid()) {
                    continue;
                }
                long time = System.nanoTime();
                mInputEvents.getPointsDown(touchSpots);
                game.update(time, touchSpots);
                Canvas canvas = null;
                try {
                    canvas = mHolder.lockCanvas();
                    canvas.getClipBounds(boundsRect);
                    canvas.drawBitmap(screen, null, boundsRect, null);
                } finally {
                    if (null != canvas) {
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }
                if (0 == frameCount % 100) {
                    System.out.println("Looped " + frameCount + " times.");
                }
                frameCount++;
            }// while
            setGameState(new Game.GameState());
        } // run()

        private final Point mDimensions;
        private final SurfaceHolder mHolder;
        private final Character mHero;
        private final UIControls mControls;
        private final RoomLoader mRoomLoader;
        private Game.GameState mGameState;
        private volatile boolean mRunning;
    } // class

    private InputEvents mInputEvents;
    private SurfaceView mSurfaceView;
    private GameLoop mGameLoop;
}
