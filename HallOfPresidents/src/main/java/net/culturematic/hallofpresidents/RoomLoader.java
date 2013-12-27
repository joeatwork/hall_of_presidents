package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;

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

            return new Room(background, furniture, terrain, description);
        } catch (JSONException e) {
            throw new RuntimeException("Malformed or missing room description JSON for " + roomPath, e);
        }
    }

    private final AssetLoader mAssetLoader;
    private final String LOGTAG = "hallofpresidents.RoomLoader";
}
