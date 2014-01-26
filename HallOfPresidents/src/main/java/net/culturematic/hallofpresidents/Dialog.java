package net.culturematic.hallofpresidents;

import java.util.Collections;
import java.util.Set;

public class Dialog {
    public Dialog(String dialog, LevelState.Direction facing, Set<String> levelFlagsToSet, Set<String> levelFlagsToRequire) {
        mDialog = dialog;
        mFacing = facing;
        mLevelFlagsToSet = Collections.unmodifiableSet(levelFlagsToSet);
        mLevelFlagsToRequire = Collections.unmodifiableSet(levelFlagsToRequire);
    }

    public String getDialog() {
        return mDialog;
    }

    public LevelState.Direction getFacing() { return mFacing; }

    public Set<String> getLevelFlagsToSet() {
        return mLevelFlagsToSet;
    }

    public Set<String> getLevelFlagsToRequire() { return mLevelFlagsToRequire; }

    private final String mDialog;
    private final LevelState.Direction mFacing;
    private final Set<String> mLevelFlagsToSet;
    private final Set<String> mLevelFlagsToRequire;
}