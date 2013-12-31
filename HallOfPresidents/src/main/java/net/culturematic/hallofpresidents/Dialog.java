package net.culturematic.hallofpresidents;

import java.util.Collections;
import java.util.Set;

public class Dialog {
    public Dialog(String dialog, UIControls.Direction facing, Set<String> roomFlagsToSet) {
        mDialog = dialog;
        mFacing = facing;
        mRoomFlagsToSet = Collections.unmodifiableSet(roomFlagsToSet);
    }

    public String getDialog() {
        return mDialog;
    }

    public UIControls.Direction getFacing() { return mFacing; }

    public Set<String> getRoomFlagsToSet() {
        return mRoomFlagsToSet;
    }

    private final String mDialog;
    private final UIControls.Direction mFacing;
    private final Set<String> mRoomFlagsToSet;
}