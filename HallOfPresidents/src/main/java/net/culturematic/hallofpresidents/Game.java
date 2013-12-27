package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Game {
    public Game(Bitmap screen,
                GameState savedState,
                RoomLoader roomLoader,
                Character hero,
                Rect viewBounds) {
        if (null != savedState) {
            throw new RuntimeException("Restoring game state is unimplemented.");
        }
        mViewBounds = viewBounds;
        mRoomLoader = roomLoader;
        mRoom = mRoomLoader.load("intro.js");
        mCanvas = new Canvas(screen);
        mHero = hero;
    }

    public void update(final long nanoTime) {
        assert(nanoTime >= 0);
        Rect viewOffset = mViewBounds; // TODO this is the OFFSET into the world
        mRoom.drawBackground(mCanvas, viewOffset, mViewBounds);
        mHero.drawCharacter(mCanvas);
        mRoom.drawFurniture(mCanvas, viewOffset, mViewBounds);
    }

    public GameState getState() {
        return new GameState();
    }

    public static class GameState {
        // TODO
    }

    private final Canvas mCanvas;
    private final RoomLoader mRoomLoader;
    private final Rect mViewBounds; // Area of screen for us to draw on
    private final Character mHero;
    private Room mRoom;
}
