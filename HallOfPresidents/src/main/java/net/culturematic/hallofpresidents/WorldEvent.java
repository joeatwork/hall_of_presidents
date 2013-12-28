package net.culturematic.hallofpresidents;

import android.graphics.Rect;

import org.json.JSONException;
import org.json.JSONObject;

public class WorldEvent {
    public WorldEvent(final Rect bounds, final String name) {
        mBounds = bounds;
        mName = name;
    }

    public Rect getBounds() {
        return mBounds;
    }

    public String getName() {
        return mName;
    }

    private final Rect mBounds;
    private final String mName;
}
