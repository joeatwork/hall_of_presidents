package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Character {
    // HARDCODED LAYOUT
    // ( 0, 0) -> Standing facing down
    // ( 1, 0) -> Standing facing up
    // ( 2, 0) -> Standing facing left
    // ( x, 1), (x, 2), (x, 3) -> Walk cycles for facing X
    public Character(Bitmap spritesheet, AssetLoader loader) {
        mSpriteSheet = spritesheet;
        int spriteSize = loader.scaleInt(128);

        // TODO this rect needs to be sensitive to bitmap scale,
        // and to be chosen based on motion direction.
        mSourceRect = new Rect(0, 0, spriteSize, spriteSize);

        // TODO this rect needs to be centered almost all of the time.
        // There should probably be a viewport abstraction that deals with this position.
        mDestRect = new Rect(0, 0, spriteSize, spriteSize);
    }

    public void drawCharacter(Canvas canvas) {
        canvas.drawBitmap(mSpriteSheet, mSourceRect, mDestRect, null);
    }

    private final Bitmap mSpriteSheet;
    private final Rect mSourceRect;
    private final Rect mDestRect;
}
