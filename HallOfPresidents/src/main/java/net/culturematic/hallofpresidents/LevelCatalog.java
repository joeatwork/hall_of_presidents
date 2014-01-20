package net.culturematic.hallofpresidents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LevelCatalog {
    public LevelCatalog(List<LevelCatalogItem> items) {
        mItems = items;
        mSavedStates = new ArrayList<LevelState>(mItems.size());
    }

    public int size() {
        return mItems.size();
    }

    public LevelCatalogItem get(int i) {
        return mItems.get(i);
    }

    public void putSavedState(int i, LevelState state) {
        mSavedStates.add(i, state);
    }

    public LevelState getSavedState(int i) {
        return mSavedStates.get(i);
    }

    public static LevelCatalog loadFromJSON(JSONObject catalogDesc)
        throws JSONException {
        final JSONArray catalogArray = catalogDesc.getJSONArray("catalog");
        final List<LevelCatalogItem> items = new ArrayList<LevelCatalogItem>(catalogArray.length());
        for (int i = 0; i < catalogArray.length(); i++) {
            final JSONObject itemDesc = catalogArray.getJSONObject(i);
            final LevelCatalogItem item = LevelCatalogItem.readJSON(itemDesc);
            items.add(item);
        }

        return new LevelCatalog(items);
    }

    private final List<LevelCatalogItem> mItems;
    private final List<LevelState> mSavedStates;
}
