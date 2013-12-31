package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class LoadingScreen implements Screen {

    public LoadingScreen(Bitmap display, Rect viewBounds, Game.GameState savedState, AssetLoader loader) {
        mDisplay = display;
        mViewBounds = viewBounds;
        mSavedState = savedState;
        mAssetLoader = loader;
        mLoadingDrawable = mAssetLoader.loadLoadingScreen();

        int viewWidth = viewBounds.width();
        int loadingHeight = mLoadingDrawable.getIntrinsicHeight();
        int loadingWidth = mLoadingDrawable.getIntrinsicWidth();
        int scaledHeight = (loadingHeight * viewWidth) / loadingWidth;
        mLoadingDrawable.setBounds(0, 0, viewWidth, scaledHeight);

        mDisplayCanvas = new Canvas(display);
        mLoadedScreen = null;

        Thread loadThread = new LoadThread();
        loadThread.start();
    }

    @Override
    public void update(long milliTime, InputEvents.TouchSpot[] touchSpots) {
        mDisplayCanvas.drawColor(Color.WHITE);
        mLoadingDrawable.draw(mDisplayCanvas);

        if (mFirstTime < 0) {
            mFirstTime = milliTime;
        }

        if (milliTime - mFirstTime > MINIMUM_LOAD_TIME_MILLIS) {
            mReadyToPlay = true;
        }
    }

    @Override
    public Screen nextScreen() {
        if (mReadyToPlay) {
            return getLoadedScreen();
        }
        return null;
    }

    @Override
    public void recycle() {
        // TODO recycle mLoadingDrawable
    }

    private synchronized void setLoadedScreen(Screen loadedScreen) {
        mLoadedScreen = loadedScreen;
    }

    private synchronized Screen getLoadedScreen() {
        return mLoadedScreen;
    }

    private class LoadThread extends Thread {
        public void run() {
            RoomLoader roomLoader = new RoomLoader(mAssetLoader);
            GameCharacter hero = new GameCharacter(mAssetLoader);
            UIControls controls = new UIControls(mAssetLoader);
            Room room = roomLoader.load("intro.js");

            Screen loaded = new WorldScreen(mDisplay, mViewBounds, mSavedState, room, hero, controls);
            setLoadedScreen(loaded);
        }
    }

    private final Bitmap mDisplay;
    private final Rect mViewBounds;
    private final AssetLoader mAssetLoader;
    private final Game.GameState mSavedState;
    private final Drawable mLoadingDrawable;
    private final Canvas mDisplayCanvas;

    private Screen mLoadedScreen;

    private long mFirstTime = -1;
    private boolean mReadyToPlay = false;

    private static int MINIMUM_LOAD_TIME_MILLIS = 3000;
}
