package net.culturematic.hallofpresidents;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CharacterState {
    public void addState(Set<String> flags, SpriteRenderer.Sprites sprites) {
        mStates = Arrays.copyOf(mStates, mStates.length + 1);
        mStates[ mStates.length - 1] = new StateSprites(flags, sprites);
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

    private static class StateSprites implements Comparable<StateSprites> {
        public StateSprites(final Set<String> inFlags, final SpriteRenderer.Sprites inSprites) {
            flags = Collections.unmodifiableSet(inFlags);
            sprites = inSprites;
        }

        @Override
        public int compareTo(StateSprites other) {
            // Negative if other is SMALLER, to sort longest to shortest.
            return other.flags.size() - flags.size();
        }

        public final Set<String> flags;
        public final SpriteRenderer.Sprites sprites;
    }

    private float mCurrentSpeedPxPerMilli = 0f;
    private Set<String> mCurrentFlags = new HashSet<String>();
    private StateSprites mCurrentState = null;
    private StateSprites[] mStates = new StateSprites[0];
}
