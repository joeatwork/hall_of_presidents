package net.culturematic.hallofpresidents;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class CharacterState {

    public void setDialogOffset(int yOffset) {
        mDialogOffsetY = yOffset;
    }

    public void addState(Set<String> flags, Sprites sprites, Dialog dialog) {
        mStates = Arrays.copyOf(mStates, mStates.length + 1);
        mStates[ mStates.length - 1] = new StateInfo(flags, sprites, dialog);
        Arrays.sort(mStates);
        if (null == mCurrentState) {
            mCurrentState = mStates[0];
            setSpeedPxPerMilli();
        }
    }

    public void setLevelState(final LevelState levelState) {
        Set<String> levelFlags = levelState.getLevelFlags();
        for (int i = 0; i < mStates.length; i++) {
            if (levelFlags.containsAll(mStates[i].flags)) {
                mCurrentState = mStates[i];
                setSpeedPxPerMilli();
                break;
            }
        }
    }

    public int getDialogOffsetY() {
        return mDialogOffsetY;
    }

    public Sprites getSprites() {
        return mCurrentState.sprites;
    }

    public Dialog getDialog() { return mCurrentState.dialog; }

    public float getSpeedPxPerMilli() {
        return mCurrentSpeedPxPerMilli;
    }

    private void setSpeedPxPerMilli() {
        Sprites sprites = mCurrentState.sprites;
        if (null == sprites) {
            mCurrentSpeedPxPerMilli = 0;
        } else {
            float speedPxPerSecond = sprites.speedPxPerSecond;
            mCurrentSpeedPxPerMilli = speedPxPerSecond / 1000f;
        }
    }

    private static class StateInfo implements Comparable<StateInfo> {
        public StateInfo(final Set<String> inFlags, final Sprites inSprites, final Dialog inDialog) {
            flags = Collections.unmodifiableSet(inFlags);
            sprites = inSprites;
            dialog = inDialog;
        }

        @Override
        public int compareTo(StateInfo other) {
            // Negative if other is SMALLER, to sort longest to shortest.
            return other.flags.size() - flags.size();
        }

        @Override
        public String toString() {
            return super.toString() + "(" + flags + ", " + sprites + ", " + dialog + ")";
        }

        public final Set<String> flags;
        public final Sprites sprites;
        public final Dialog dialog;
    }

    private int mDialogOffsetY = 0;
    private float mCurrentSpeedPxPerMilli = 0f;
    private StateInfo mCurrentState = null;
    private StateInfo[] mStates = new StateInfo[0];
}
