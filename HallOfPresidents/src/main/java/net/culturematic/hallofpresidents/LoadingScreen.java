package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class LoadingScreen implements Screen {

    public LoadingScreen(Bitmap display, Rect viewBounds, RoomState roomState, AssetLoader loader) {
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

        mLoadThread = new LoadThread(roomState);
        mLoadThread.start();
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
    public RoomState getRoomState() {
        return mLoadThread.getRoomState();
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
        public LoadThread(RoomState roomState) {
            mRoomState = roomState;
        }

        public void run() {
            RoomLoader roomLoader = new RoomLoader(mAssetLoader);
            GameCharacter hero = new GameCharacter(mAssetLoader);
            UIControls controls = new UIControls(mAssetLoader, mRoomState);
            Room room = roomLoader.load(mRoomState.getRoomCatalogItem().getPath());
            mRoomState.setPosition(room.defaultDoor());

            Screen loaded = new WorldScreen(mDisplay, mViewBounds, mRoomState, room, hero, controls);
            setLoadedScreen(loaded);
        }

        public RoomState getRoomState() {
            while (true) {
                try {
                    this.join();
                    return mRoomState;
                } catch (InterruptedException e) {
                    ; // keep trying
                }
            }// forever
        }

        private final RoomState mRoomState;
    }

    private final Bitmap mDisplay;
    private final Rect mViewBounds;
    private final AssetLoader mAssetLoader;
    private final Drawable mLoadingDrawable;
    private final Canvas mDisplayCanvas;
    private final LoadThread mLoadThread;

    private Screen mLoadedScreen;

    private long mFirstTime = -1;
    private boolean mReadyToPlay = false;

    private static int MINIMUM_LOAD_TIME_MILLIS = 3000;
}
