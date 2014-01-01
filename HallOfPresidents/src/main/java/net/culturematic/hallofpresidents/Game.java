package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Rect;


public class Game {
    public Game(Bitmap display, Rect viewBounds, RoomState savedState, AssetLoader assetLoader) {
        mScreen = new LoadingScreen(display, viewBounds, savedState, assetLoader);
    }

    public boolean update(final long milliTime, InputEvents.TouchSpot[] touchSpots) {
        mScreen.update(milliTime, touchSpots);
        Screen nextScreen = mScreen.nextScreen();
        if (null != nextScreen) {
            mScreen.recycle();
            mScreen = nextScreen;
        }
        return mScreen.done();
    }

    private Screen mScreen;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.Game";
}
