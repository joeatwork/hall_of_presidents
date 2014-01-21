package net.culturematic.hallofpresidents;

import android.graphics.PointF;
import android.graphics.Rect;

public class WorldEvent {
    public static class Door {
        public Door(final String destRoomName, final PointF destPosition) {
            mDestRoomName = destRoomName;
            mDestPosition = destPosition;
        }

        public String getDestRoomName() {
            return mDestRoomName;
        }

        public PointF getDestPosition() {
            return mDestPosition;
        }

        private final String mDestRoomName;
        private final PointF mDestPosition;
    }

    public WorldEvent(final Rect bounds, final String name, final Dialog dialog, final Door door) {
        mBounds = bounds;
        mName = name;
        mDialog = dialog;
        mDoor = door;
    }

    public Rect getBounds() {
        return mBounds;
    }

    public String getName() {
        return mName;
    }

    public Dialog getDialog() {
        return mDialog;
    }

    public Door getDoor() {
        return mDoor;
    }

    private final Rect mBounds;
    private final String mName;
    private final Dialog mDialog;
    private final Door mDoor;
}
