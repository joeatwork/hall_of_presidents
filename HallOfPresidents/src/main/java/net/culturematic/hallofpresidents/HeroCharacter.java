package net.culturematic.hallofpresidents;

import android.graphics.PointF;

public class HeroCharacter extends GameCharacter {

    public HeroCharacter(CharacterState characterState) {
        super(characterState, null);
    }

    public void setLevelState(LevelState levelState) {
        mLevelState = levelState;
    }

    protected PointF getPosition() {
        return mLevelState.getPosition();
    }

    private LevelState mLevelState;
}
