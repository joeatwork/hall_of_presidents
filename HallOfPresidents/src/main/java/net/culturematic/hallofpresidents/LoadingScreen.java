package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class LoadingScreen implements Screen {

    public LoadingScreen(Bitmap display, Rect viewBounds, LevelState levelState, AssetLoader loader) {
        mDisplay = display;
        mViewBounds = viewBounds;
        mAssetLoader = loader;
        mLoadingDrawable = mAssetLoader.loadLoadingScreen();

        int viewWidth = viewBounds.width();
        int loadingHeight = mLoadingDrawable.getIntrinsicHeight();
        int loadingWidth = mLoadingDrawable.getIntrinsicWidth();
        int scaledHeight = (loadingHeight * viewWidth) / loadingWidth;
        mLoadingDrawable.setBounds(0, 0, viewWidth, scaledHeight);

        mDisplayCanvas = new Canvas(display);
        mLoadedScreen = null;

        LoadThread loadThread = new LoadThread(levelState);
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
    public boolean done() {
        return false;
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
        public LoadThread(LevelState levelState) {
            mLevelState = levelState;
        }

        public void run() {
            LevelReader levelReader = new LevelReader(mAssetLoader);
            LevelCatalogItem item = mLevelState.getLevelCatalogItem();
            Level level;
            try {
                level = levelReader.readLevel(item);
            } catch (LevelReader.LevelUnreadableException e) {
                throw new RuntimeException("Can't read level", e);
            }

            if (null == mLevelState.getRoomName()) {
                mLevelState.setRoomName(level.getStartRoomName());
            }
            if (null == mLevelState.getPosition()) {
                mLevelState.setPosition(level.getStartPosition());
            }

            GameCharacter hero = level.getHero();
            hero.setLevelState(mLevelState);
            UIControls controls = new UIControls(mAssetLoader, mLevelState);
            Screen loaded = new WorldScreen(mAssetLoader, mDisplay, mViewBounds, level, mLevelState, hero, controls);
            setLoadedScreen(loaded);
        }

        private final LevelState mLevelState;
    }

    private final Bitmap mDisplay;
    private final Rect mViewBounds;
    private final AssetLoader mAssetLoader;
    private final Drawable mLoadingDrawable;
    private final Canvas mDisplayCanvas;

    private Screen mLoadedScreen;

    private long mFirstTime = -1;
    private boolean mReadyToPlay = false;

    private static final int MINIMUM_LOAD_TIME_MILLIS = 2000;
}
