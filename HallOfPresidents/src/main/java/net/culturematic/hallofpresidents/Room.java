package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.provider.CalendarContract;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Room {
    public Room(Bitmap background, Bitmap furniture, Bitmap terrain, JSONObject roomDescription) {
        mBackground = background;
        mFurniture = furniture;
        mTerrain = terrain;
        mDescription = roomDescription;

        try {
            JSONArray events = mDescription.getJSONArray("events");
            mEvents = new WorldEvent[events.length()];
            for (int i = 0; i < events.length(); i++) {
                WorldEvent event = new WorldEvent(events.getJSONObject(i));
                mEvents[i] = event;
            }
        } catch (JSONException e) {
            throw new RuntimeException("Can't find events in room JSON", e);
        }
    }

    /**
     *
     * @param canvas canvas on which the background should be drawn
     * @param worldRect what portion of the world is visible
     * @param viewport what portion of the canvas is dedicated to showing the world
     */
    public void drawBackground(Canvas canvas, Rect worldRect, Rect viewport) {
        canvas.drawBitmap(mBackground, worldRect, viewport, null);
    }

    public void drawFurniture(Canvas canvas, Rect worldRect, Rect viewport) {
        canvas.drawBitmap(mFurniture, worldRect, viewport, null);
    }

    public boolean inBounds(int x, int y) {
        if ((x < 0) || (y < 0) ||
            (x >= mTerrain.getWidth()) ||
            (y >= mTerrain.getHeight())) {
            Log.d(LOGTAG, "OUT OF BOUNDS (Beyond terrain border) " + x + ", " + y);
            return false;
        }

        int color = mTerrain.getPixel(x, y);
        if (0 == (color & 0xff000000)) {
            Log.d(LOGTAG, String.format("FOUND ALPHA 0 IN %x", color));
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

    private final JSONObject mDescription;
    private final Bitmap mBackground;
    private final Bitmap mFurniture;
    private final Bitmap mTerrain;
    private final WorldEvent[] mEvents;

    private static final String LOGTAG = "hallofpresidents.Room";
}
