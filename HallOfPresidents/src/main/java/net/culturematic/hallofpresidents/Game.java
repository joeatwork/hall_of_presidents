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
        mAssetLoader = assetLoader;

        // TODO Temporary
        RoomLoader roomLoader = new RoomLoader(assetLoader);
        GameCharacter hero = new GameCharacter(assetLoader);
        UIControls controls = new UIControls(assetLoader);
        Room room = roomLoader.load("intro.js");

        mScreen = new WorldScreen(display, viewBounds, savedState, room, hero, controls);
    }

    public void update(final long milliTime, InputEvents.TouchSpot[] touchSpots) {
        mScreen.update(milliTime, touchSpots);
    }

    public GameState getState() {
        return new GameState();
    }

    public static class GameState {
        // TODO
    }

    private Screen mScreen;
    private final AssetLoader mAssetLoader;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.Game";
}
