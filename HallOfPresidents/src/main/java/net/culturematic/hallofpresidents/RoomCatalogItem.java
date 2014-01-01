package net.culturematic.hallofpresidents;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class RoomCatalogItem {
    public RoomCatalogItem(String name, String path, String storage) {
        mName = name;
        mPath = path;
        mStorage = storage;
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

    public String getFullPath() {
        return getStorage() + ':' + getPath();
    }

    public static RoomCatalogItem readJSON(JSONObject itemDesc)
        throws JSONException {
        return new RoomCatalogItem(
            itemDesc.getString("name"),
            itemDesc.getString("path"),
            itemDesc.getString("storage")
        );
    }

    public JSONObject toJSON()
        throws JSONException {
        JSONObject ret = new JSONObject();
        ret.put("name", mName);
        ret.put("path", mPath);
        ret.put("storage", mStorage);
        return ret;
    }

    private final String mName;
    private final String mPath;
    private final String mStorage;
}
