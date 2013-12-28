package net.culturematic.hallofpresidents;

import android.graphics.Rect;

import org.json.JSONException;
import org.json.JSONObject;

public class WorldEvent {
    public WorldEvent(final JSONObject eventDescription) {
        mDescription = eventDescription;
        try {
            mName = mDescription.getString("name");
            JSONObject boundsObj = mDescription.getJSONObject("bounds");
            mBounds = new Rect(
                boundsObj.getInt("left"),
                boundsObj.getInt("top"),
                boundsObj.getInt("right"),
                boundsObj.getInt("bottom")
            );
        } catch (JSONException e) {
            throw new RuntimeException("Can't parse Event JSON");
        }
    }

    public Rect getBounds() {
        return mBounds;
    }

    public String getName() {
        return mName;
    }

    private final Rect mBounds;
    private final String mName;
    private final JSONObject mDescription;
}
