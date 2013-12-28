package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Rect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RoomLoader {
    public RoomLoader(AssetLoader assetLoader) { // TODO remove when we get this from the net
        mAssetLoader = assetLoader;
    }

    public Room load(String roomPath) {
        JSONObject description = mAssetLoader.loadJSON(roomPath);

        try {
            final String backgroundPath = description.getString("background");
            final Bitmap background = mAssetLoader.loadBitmap(backgroundPath);

            final String furniturePath = description.getString("furniture");
            final Bitmap furniture = mAssetLoader.loadBitmap(furniturePath);

            final String terrainPath = description.getString("terrain");
            final Bitmap terrain = mAssetLoader.loadBitmap(terrainPath);

            final JSONArray eventsDescs = description.getJSONArray("events");
            final WorldEvent[] events = new WorldEvent[eventsDescs.length()];
            for (int i = 0; i < eventsDescs.length(); i++) {
                events[i] = readEvent(eventsDescs.getJSONObject(i));
            }

            return new Room(background, furniture, terrain, events);
        } catch (JSONException e) {
            throw new RuntimeException("Malformed or missing room description JSON for " + roomPath, e);
        }
    }

    private WorldEvent readEvent(JSONObject eventDescription) {
        try {
            String name = eventDescription.getString("name");
            JSONObject boundsObj = eventDescription.getJSONObject("bounds");
            final int unscaledTop = boundsObj.getInt("top");
            final int unscaledRight = boundsObj.getInt("right");
            final int unscaledLeft = boundsObj.getInt("left");
            final int unscaledBottom = boundsObj.getInt("bottom");
            Rect bounds = new Rect(
                mAssetLoader.scaleInt(unscaledLeft),
                mAssetLoader.scaleInt(unscaledTop),
                mAssetLoader.scaleInt(unscaledRight),
                mAssetLoader.scaleInt(unscaledBottom)
            );
            return new WorldEvent(bounds, name);
        } catch (JSONException e) {
            throw new RuntimeException("Can't parse Event JSON");
        }
    }

    private final AssetLoader mAssetLoader;
    private final String LOGTAG = "hallofpresidents.RoomLoader";
}
