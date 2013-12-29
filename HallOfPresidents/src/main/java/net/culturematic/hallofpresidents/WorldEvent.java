package net.culturematic.hallofpresidents;

import android.graphics.Rect;

import org.json.JSONException;
import org.json.JSONObject;

public class WorldEvent {


    public WorldEvent(final Rect bounds, final String name, final Dialog dialog) {
        mBounds = bounds;
        mName = name;
        mDialog = dialog;
    }

    public Rect getBounds() {
        return mBounds;
    }

    public String getName() {
        return mName;
    }

    public Dialog getDialog() {
        return mDialog;
    }

    private final Rect mBounds;
    private final String mName;
    private final Dialog mDialog;
}
