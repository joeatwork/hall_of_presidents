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
        setContentView(mSurfaceView);
    }

    @Override
    public void onResume() {
        super.onResume();

        final Point gameDimensions = getBitmapDimensions();
        mGameLoop = new GameLoop(mSurfaceView.getHolder(), gameDimensions, null);
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
        public GameLoop(SurfaceHolder holder, Point gameDimensions, Game.GameState gameState) {
            mRunning = true;
            mDimensions = gameDimensions;
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
            int frameCount = 0;
            final Rect boundsRect = new Rect();
            Bitmap screen = Bitmap.createBitmap(mDimensions.x, mDimensions.y, Bitmap.Config.RGB_565);
            Game.GameState gameState = getGameState();
            setGameState(null);
            Game game = new Game(screen, gameState);

            while (mRunning) {
                if (! mHolder.getSurface().isValid()) {
                    continue;
                }
                long time = System.nanoTime();
                game.update(time);
                Canvas canvas = null;
                try {
                    canvas = mHolder.lockCanvas();
                    canvas.drawBitmap(screen, 0, 0, null);
                    // canvas.getClipBounds(boundsRect);
                    // canvas.drawBitmap(bitmap, (Rect) null, boundsRect, (Paint) null);
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

        private Game.GameState mGameState;
        private Point mDimensions;
        private SurfaceHolder mHolder;
        private volatile boolean mRunning;
    } // class

    private SurfaceView mSurfaceView;
    private GameLoop mGameLoop;
}
