package net.culturematic.hallofpresidents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RoomCatalog {
    public RoomCatalog(List<RoomCatalogItem> items) {
        mItems = items;
        mSavedStates = new ArrayList<RoomState>(mItems.size());
    }

    public int size() {
        return mItems.size();
    }

    public RoomCatalogItem get(int i) {
        return mItems.get(i);
    }

    public void putSavedState(int i, RoomState state) {
        mSavedStates.add(i, state);
    }

    public RoomState getSavedState(int i) {
        return mSavedStates.get(i);
    }

    public static RoomCatalog loadFromJSON(JSONObject catalogDesc)
        throws JSONException {
        final JSONArray catalogArray = catalogDesc.getJSONArray("catalog");
        final List<RoomCatalogItem> items = new ArrayList<RoomCatalogItem>(catalogArray.length());
        for (int i = 0; i < catalogArray.length(); i++) {
            final JSONObject itemDesc = catalogArray.getJSONObject(i);
            final RoomCatalogItem item = RoomCatalogItem.readJSON(itemDesc);
            items.add(item);
        }

        return new RoomCatalog(items);
    }

    private final List<RoomCatalogItem> mItems;
    private final List<RoomState> mSavedStates;
}
