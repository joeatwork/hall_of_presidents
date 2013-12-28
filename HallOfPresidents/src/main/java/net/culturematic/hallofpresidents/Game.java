package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Game {
    public Game(Bitmap screen,
                GameState savedState,
                RoomLoader roomLoader,
                Character hero,
                UIControls controls,
                Rect viewBounds) {
        if (null != savedState) {
            throw new RuntimeException("Restoring game state is unimplemented.");
        }
        mViewBounds = viewBounds;
        mRoomLoader = roomLoader;
        mRoom = mRoomLoader.load("intro.js");
        mCanvas = new Canvas(screen);
        mHero = hero;
        mControls = controls;

        mRedPaint = new Paint();
        mRedPaint.setColor(Color.RED);
    }

    public void update(final long nanoTime, InputEvents.TouchSpot[] touchSpots) {
        assert(nanoTime >= 0);

        mControls.intepretInteractions(touchSpots);
        mHero.directionCommand(nanoTime, mControls.currentDirection());

        mCanvas.drawColor(Color.BLACK);

        Rect viewOffset = mViewBounds; // TODO this is the OFFSET into the world
        mRoom.drawBackground(mCanvas, viewOffset, mViewBounds);
        mHero.drawCharacter(mCanvas);
        mRoom.drawFurniture(mCanvas, viewOffset, mViewBounds);
        mControls.drawControls(mCanvas, mViewBounds);

        for (int i = 0; i < touchSpots.length; i++) {
            InputEvents.TouchSpot next = touchSpots[i];
            if (null == next) break;

            mCanvas.drawCircle(next.x, next.y, 20, mRedPaint);
        }
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
    private final UIControls mControls;

    private final Paint mRedPaint;
    private Room mRoom;
}
