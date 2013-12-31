package net.culturematic.hallofpresidents;

import java.util.Collections;
import java.util.Set;

public class Dialog {
    public Dialog(String dialog, RoomState.Direction facing, Set<String> roomFlagsToSet) {
        mDialog = dialog;
        mFacing = facing;
        mRoomFlagsToSet = Collections.unmodifiableSet(roomFlagsToSet);
    }

    public String getDialog() {
        return mDialog;
    }

    public RoomState.Direction getFacing() { return mFacing; }

    public Set<String> getRoomFlagsToSet() {
        return mRoomFlagsToSet;
    }

    private final String mDialog;
    private final RoomState.Direction mFacing;
    private final Set<String> mRoomFlagsToSet;
}