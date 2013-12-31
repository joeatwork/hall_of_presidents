package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Rect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class RoomLoader {
    public RoomLoader(AssetLoader assetLoader) { // TODO remove when we get this from the net
        mAssetLoader = assetLoader;
    }

    public Room load(String roomPath) {
        JSONObject description = mAssetLoader.loadJSONObject(roomPath);

        try {
            final String backgroundPath = description.getString("background");
            final Bitmap background = mAssetLoader.loadBitmap(backgroundPath, Bitmap.Config.RGB_565);

            final String furniturePath = description.getString("furniture");
            final Bitmap furniture = mAssetLoader.loadBitmap(furniturePath, null);

            final String terrainPath = description.getString("terrain");
            final Bitmap terrain = mAssetLoader.loadBitmap(terrainPath, Bitmap.Config.ALPHA_8);

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
            final Rect bounds = new Rect(
                mAssetLoader.scaleInt(unscaledLeft),
                mAssetLoader.scaleInt(unscaledTop),
                mAssetLoader.scaleInt(unscaledRight),
                mAssetLoader.scaleInt(unscaledBottom)
            );

            Dialog dialog = null;
            if (eventDescription.has("dialog")) {
                JSONObject dialogDesc = eventDescription.getJSONObject("dialog");

                String facingName = dialogDesc.getString("facing");
                RoomState.Direction facing;
                if ("Up".equals(facingName)) {
                    facing = RoomState.Direction.DIRECTION_UP;
                } else if ("Down".equals(facingName)) {
                    facing = RoomState.Direction.DIRECTION_DOWN;
                } else if ("Left".equals(facingName)) {
                    facing = RoomState.Direction.DIRECTION_LEFT;
                } else if ("Right".equals(facingName)) {
                    facing = RoomState.Direction.DIRECTION_RIGHT;
                } else {
                    throw new RuntimeException(
                        "Can't understand facing " + facingName + " (should be 'Up', 'Down', 'Left' or 'Right'"
                    );
                }

                Set<String> flagsToSet = new HashSet<String>();
                if (dialogDesc.has("set_room_flags")) {
                    JSONArray flagArray = dialogDesc.getJSONArray("set_room_flags");
                    for (int i = 0; i < flagArray.length(); i++) {
                        flagsToSet.add(flagArray.getString(i));
                    }
                }

                dialog = new Dialog(
                    dialogDesc.getString("dialog"),
                    facing,
                    flagsToSet
                );
            }

            return new WorldEvent(bounds, name, dialog);
        } catch (JSONException e) {
            throw new RuntimeException("Can't parse Event JSON", e);
        }
    }

    private final AssetLoader mAssetLoader;
    private final String LOGTAG = "hallofpresidents.RoomLoader";
}
