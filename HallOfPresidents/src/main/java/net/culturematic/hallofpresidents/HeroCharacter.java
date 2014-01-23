package net.culturematic.hallofpresidents;

import android.graphics.PointF;

public class HeroCharacter extends GameCharacter {

    public HeroCharacter(CharacterState characterState) {
        super(characterState, null);
    }

    @Override
    public PointF getPosition() {
        return mLevelState.getPosition();
    }

    public void setLevelState(LevelState levelState) {
        mLevelState = levelState;
    }

    private LevelState mLevelState;
}
