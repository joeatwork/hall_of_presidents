package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.Set;

public class Room {
    public Room(Bitmap background, Bitmap furniture, Bitmap terrain, WorldEvent[] events, Dialog victoryDialog) {
        mBackground = background;
        mFurniture = furniture;
        mTerrain = terrain;
        mEvents = events;
        mVictory = victoryDialog;
    }

    /**
     *
     * @param canvas canvas on which the background should be drawn
     * @param worldRect what portion of the world is visible
     * @param viewport what portion of the canvas is dedicated to showing the world
     */
    public void drawBackground(Canvas canvas, Rect worldRect, Rect viewport) {
        canvas.drawBitmap(mBackground, worldRect, viewport, null);

        /*
         * For debugging- the code below will draw a rectangle for each event in the room.
         *
        Paint redPaint = new Paint();
        redPaint.setColor(Color.RED);
        for (int i = 0; i < mEvents.length; i++) {
            final Rect bounds = new Rect(mEvents[i].getBounds()); // Bounds in WORLD COORDS.
            bounds.offset(- worldRect.left, - worldRect.top);
            canvas.drawRect(bounds, redPaint);
        }
        */
    }

    public void drawFurniture(Canvas canvas, Rect worldRect, Rect viewport) {
        canvas.drawBitmap(mFurniture, worldRect, viewport, null);
    }

    public PointF defaultDoor() {
        return new PointF(mBackground.getWidth() / 2, mBackground.getHeight() / 2);
    }

    public boolean inBounds(int x, int y) {
        if ((x < 0) || (y < 0) ||
            (x >= mTerrain.getWidth()) ||
            (y >= mTerrain.getHeight())) {
            return false;
        }

        int color = mTerrain.getPixel(x, y);
        if (0 == (color & 0xff000000)) {
            return false;
        }
        return true;
    }

    public WorldEvent checkForEvent(PointF position) {
       for (int i = 0; i < mEvents.length; i++) {
           final WorldEvent event = mEvents[i];
           final Rect rect = event.getBounds();
           if (rect.contains((int) position.x, (int) position.y)) {
               return event;
           }
        }

        return null;
    }

    public Dialog getVictory() {
        return mVictory;
    }

    private final Bitmap mBackground;
    private final Bitmap mFurniture;
    private final Bitmap mTerrain;
    private final WorldEvent[] mEvents;
    private final Dialog mVictory;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.Room";
}
