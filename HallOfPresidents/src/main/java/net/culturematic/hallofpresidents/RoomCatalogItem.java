package net.culturematic.hallofpresidents;

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

    private final String mName;
    private final String mPath;
    private final String mStorage;
}
