package net.culturematic.hallofpresidents;

import android.graphics.PointF;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GameState {
    public GameState() {
        mRoomFlags = new HashSet<String>();
        mRoomPosition = new PointF(0, 0);
    }

    public void setRoomCatalogItem(RoomCatalogItem roomItem) {
        mRoomItem = roomItem;
    }

    public RoomCatalogItem getRoomCatalogItem() {
        return mRoomItem;
    }

    public void addRoomFlags(Collection<String> newFlags) {
        mRoomFlags.addAll(newFlags);
    }

    public Set<String> getRoomFlags() {
        return mRoomFlags;
    }

    public PointF getPosition() {
        return mRoomPosition;
    }

    public void setPosition(PointF position) {
        mRoomPosition.set(position);
    }

    private RoomCatalogItem mRoomItem;
    private PointF mRoomPosition;
    private final Set<String> mRoomFlags;
}
