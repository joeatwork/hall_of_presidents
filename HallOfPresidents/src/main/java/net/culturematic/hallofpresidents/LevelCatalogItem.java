package net.culturematic.hallofpresidents;

import org.json.JSONException;
import org.json.JSONObject;

public class LevelCatalogItem {
    public LevelCatalogItem(String name, String path, String storage, String description) {
        mName = name;
        mPath = path;
        mStorage = storage;
        mDescription = description;
    }

    public String getName() {
        return mName;
    }

    public String getPath() {
        return mPath;
    }

    public String getStorage() {
        return mStorage;
    }

    public String getDescription() { return mDescription; }

    public String getFullPath() {
        return getStorage() + ':' + getPath();
    }

    public static LevelCatalogItem readJSON(JSONObject itemDesc)
        throws JSONException {
        return new LevelCatalogItem(
            itemDesc.getString("name"),
            itemDesc.getString("path"),
            itemDesc.getString("storage"),
            itemDesc.getString("description")
        );
    }

    public JSONObject toJSON()
        throws JSONException {
        JSONObject ret = new JSONObject();
        ret.put("name", mName);
        ret.put("path", mPath);
        ret.put("storage", mStorage);
        ret.put("description", mDescription);
        return ret;
    }

    private final String mName;
    private final String mPath;
    private final String mStorage;
    private final String mDescription;
}
