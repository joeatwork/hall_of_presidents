package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Rect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class RoomLoader {
    public RoomLoader(AssetLoader assetLoader) {
        mAssetLoader = assetLoader;
    }

    public Room load(String roomPath) {
        JSONObject description = mAssetLoader.loadJSONObject(roomPath);

        try {
            final String backgroundPath = description.getString("background");
            final Bitmap background = mAssetLoader.loadBitmap(backgroundPath, Bitmap.Config.RGB_565);

            final String furniturePath = description.getString("furniture");
            final Bitmap furniture = mAssetLoader.loadBitmap(furniturePath, Bitmap.Config.ARGB_4444);

            final String terrainPath = description.getString("terrain");
            final Bitmap terrain = mAssetLoader.loadBitmap(terrainPath, Bitmap.Config.ALPHA_8);

            final JSONArray eventsDescs = description.getJSONArray("events");
            final WorldEvent[] events = new WorldEvent[eventsDescs.length()];
            for (int i = 0; i < eventsDescs.length(); i++) {
                events[i] = readEvent(eventsDescs.getJSONObject(i));
            }

            final JSONObject victoryDesc = description.getJSONObject("victory");
            final JSONObject victoryDialogDesc = victoryDesc.getJSONObject("dialog");
            final Dialog victoryDialog = readDialog(victoryDialogDesc);

            return new Room(background, furniture, terrain, events, victoryDialog);
        } catch (JSONException e) {
            throw new RuntimeException("Malformed or missing room description JSON for " + roomPath, e);
        }
    }

    private WorldEvent readEvent(JSONObject eventDescription)
        throws JSONException {
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
            dialog = readDialog(dialogDesc);
        }

        return new WorldEvent(bounds, name, dialog);
    }

    private Dialog readDialog(JSONObject dialogDesc)
        throws JSONException {
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

        Set<String> flagsToRequire = new HashSet<String>();
        if (dialogDesc.has("condition_room_flags")) {
            JSONArray flagArray = dialogDesc.getJSONArray("condition_room_flags");
            for (int i = 0; i < flagArray.length(); i++) {
                flagsToRequire.add(flagArray.getString(i));
            }
        }

        String dialogText = dialogDesc.getString("dialog");
        return new Dialog(dialogText, facing, flagsToSet, flagsToRequire);
    }

    private final AssetLoader mAssetLoader;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.RoomLoader";
}
