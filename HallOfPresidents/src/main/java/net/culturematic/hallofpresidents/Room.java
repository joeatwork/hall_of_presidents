package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

public class Room {
    public Room(String name, Bitmap background, Bitmap furniture, Bitmap terrain, WorldEvent[] events) {
        mName = name;
        mBackground = background;
        mFurniture = furniture;
        mTerrain = terrain;
        mEvents = events;
    }

    public String getName() { return mName; }

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
        *
        * END DEBUGGING
        */
    }

    public void drawFurniture(Canvas canvas, Rect worldRect, Rect viewport) {
        canvas.drawBitmap(mFurniture, worldRect, viewport, null);
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

    public void recycle() {
        mBackground.recycle();
        mFurniture.recycle();
        mTerrain.recycle();
    }

    private final String mName;
    private final Bitmap mBackground;
    private final Bitmap mFurniture;
    private final Bitmap mTerrain;
    private final WorldEvent[] mEvents;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.Room";
}
