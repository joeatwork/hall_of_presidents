package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class LoadingScreen implements Screen {

    public LoadingScreen(Bitmap display, Rect viewBounds, LevelState levelState, AssetLoader loader, Analytics analytics) {
        mAnalytics = analytics;
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
        mLevelName = levelState.getLevelCatalogItem().getName();

        LoadThread loadThread = new LoadThread(levelState);
        loadThread.start();
    }

    @Override
    public String getHelpMessage() {
        return null;
    }

    @Override
    public void update(long milliTime, InputEvents.TouchSpot[] ignored) {
        mDisplayCanvas.drawColor(Color.WHITE);
        mLoadingDrawable.draw(mDisplayCanvas);

        if (mFirstTime < 0) {
            mAnalytics.trackLevelLoading(mLevelName);
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

            HeroCharacter hero = level.getHero();
            UIControls controls = new UIControls(mAssetLoader, mLevelState);
            Screen loaded = new WorldScreen(mAssetLoader, mDisplay, mViewBounds, level, mLevelState, hero, controls, mAnalytics);
            setLoadedScreen(loaded);
        }

        private final LevelState mLevelState;
    }

    private final Analytics mAnalytics;
    private final String mLevelName;
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
