package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import org.json.JSONObject;

public class Room {
    // TODO Doors?

    public Room(Bitmap background, Bitmap furniture, Bitmap terrain, JSONObject roomDescription) {
        mBackground = background;
        mFurniture = furniture;
        mTerrain = terrain;
        mDescription = roomDescription;
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

    private JSONObject mDescription;
    private Bitmap mBackground;
    private Bitmap mFurniture;
    private Bitmap mTerrain;
}
