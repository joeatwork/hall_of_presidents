package net.culturematic.hallofpresidents;

import java.util.Collections;
import java.util.Set;

public class Dialog {
    public Dialog(String dialog, LevelState.Direction facing, Set<String> roomFlagsToSet, Set<String> roomFlagsToRequire) {
        mDialog = dialog;
        mFacing = facing;
        mRoomFlagsToSet = Collections.unmodifiableSet(roomFlagsToSet);
        mRoomFlagsToRequire = Collections.unmodifiableSet(roomFlagsToRequire);
    }

    public String getDialog() {
        return mDialog;
    }

    public LevelState.Direction getFacing() { return mFacing; }

    public Set<String> getLevelFlagsToSet() {
        return mRoomFlagsToSet;
    }

    public Set<String> getRoomFlagsToRequire() { return mRoomFlagsToRequire; }

    private final String mDialog;
    private final LevelState.Direction mFacing;
    private final Set<String> mRoomFlagsToSet;
    private final Set<String> mRoomFlagsToRequire;
}