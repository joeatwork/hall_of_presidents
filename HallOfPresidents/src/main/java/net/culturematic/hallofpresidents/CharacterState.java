package net.culturematic.hallofpresidents;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CharacterState {
    public void addState(Set<String> flags, SpriteRenderer.Sprites sprites, Dialog dialog) {
        mStates = Arrays.copyOf(mStates, mStates.length + 1);
        mStates[ mStates.length - 1] = new StateInfo(flags, sprites, dialog);
        Arrays.sort(mStates);
        if (null == mCurrentState) {
            mCurrentState = mStates[0];
            setSpeedPxPerMilli();
        }
    }

    public void addFlag(String flag) {
        if (! mCurrentFlags.contains(flag)) {
            mCurrentFlags.add(flag);
            checkCurrentState();
        }
    }

    public void removeFlag(String flag) {
        if (mCurrentFlags.contains(flag)) {
            mCurrentFlags.remove(flag);
            checkCurrentState();
        }
    }

    public SpriteRenderer.Sprites getSprites() {
        return mCurrentState.sprites;
    }

    public float getSpeedPxPerMilli() {
        return mCurrentSpeedPxPerMilli;
    }

    public void recycle() {
        for (int i = 0; i < mStates.length; i++) {
            mStates[i].sprites.recycle();
        }
    }

    private void checkCurrentState() {
        for (int i = 0; i < mStates.length; i++) {
            if (mCurrentFlags.containsAll(mStates[i].flags)) {
                mCurrentState = mStates[i];
                setSpeedPxPerMilli();
                break;
            }
        }
    }

    private void setSpeedPxPerMilli() {
        float speedPxPerSecond = mCurrentState.sprites.speedPxPerSecond;
        mCurrentSpeedPxPerMilli = speedPxPerSecond / 1000f;
    }

    private static class StateInfo implements Comparable<StateInfo> {
        public StateInfo(final Set<String> inFlags, final SpriteRenderer.Sprites inSprites, final Dialog inDialog) {
            flags = Collections.unmodifiableSet(inFlags);
            sprites = inSprites;
            dialog = inDialog;
        }

        @Override
        public int compareTo(StateInfo other) {
            // Negative if other is SMALLER, to sort longest to shortest.
            return other.flags.size() - flags.size();
        }

        public final Set<String> flags;
        public final SpriteRenderer.Sprites sprites;
        public final Dialog dialog;
    }

    private float mCurrentSpeedPxPerMilli = 0f;
    private Set<String> mCurrentFlags = new HashSet<String>();
    private StateInfo mCurrentState = null;
    private StateInfo[] mStates = new StateInfo[0];
}
