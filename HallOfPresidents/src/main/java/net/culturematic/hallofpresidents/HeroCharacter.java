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

    @Override
    public void update(long milliTime, LevelState levelState) {
        mLevelState = levelState;
        super.update(milliTime, levelState);
    }

    private LevelState mLevelState;
}
