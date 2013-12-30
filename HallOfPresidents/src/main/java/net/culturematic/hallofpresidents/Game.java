package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

public class Game {
    public Game(Bitmap display, Rect viewBounds, GameState savedState, AssetLoader assetLoader) {
        if (null != savedState) {
            throw new RuntimeException("Restoring game state is unimplemented.");
        }
        mScreen = new LoadingScreen(display, viewBounds, savedState, assetLoader);
    }

    public void update(final long milliTime, InputEvents.TouchSpot[] touchSpots) {
        mScreen.update(milliTime, touchSpots);
        Screen nextScreen = mScreen.nextScreen();
        if (null != nextScreen) {
            mScreen.recycle();
            mScreen = nextScreen;
        }
    }

    public GameState getState() {
        return new GameState();
    }

    public static class GameState {
        // TODO
    }

    private Screen mScreen;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.Game";
}
