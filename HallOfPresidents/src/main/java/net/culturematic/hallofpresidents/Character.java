package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

public class Character {
    // HARDCODED LAYOUT
    // ( 0, 0) -> Standing facing down
    // ( 1, 0) -> Standing facing up
    // ( 2, 0) -> Standing facing left
    // ( x, 1), (x, 2), (x, 3) -> Walk cycles for facing X
    public Character(Bitmap spritesheet, AssetLoader loader, PointF startPosition) {
        mSpriteSheet = spritesheet;
        int spriteSize = loader.scaleInt(128);

        // TODO this rect needs to be sensitive to bitmap scale,
        // and to be chosen based on motion direction.
        mSourceRect = new Rect(0, 0, spriteSize, spriteSize);

        // TODO this rect needs to be centered almost all of the time.
        // There should probably be a viewport abstraction that deals with this position.
        mDestRect = new Rect(0, 0, spriteSize, spriteSize);

        mPosition = startPosition; // TODO This should be in GameState.
        mLastTime = -1;
        mSpeed = 1f/10000000f; // pixels per nano?
    }

    public void directionCommand(long nanoTime, UIControls.Direction direction) {
        if (mLastTime < 0) {
            mLastTime = nanoTime;
        }

        long deltaTime = nanoTime - mLastTime;
        float distance = mSpeed * deltaTime;

        switch (direction) {
            case DIRECTION_UP:
                mPosition.y -= distance;
                break;
            case DIRECTION_DOWN:
                mPosition.y += distance;
                break;
            case DIRECTION_RIGHT:
                mPosition.x += distance;
                Log.d(LOGTAG, "MOVE dTime " + deltaTime + ", distance " + distance + ", position x " + mPosition.x);
                break;
            case DIRECTION_LEFT:
                mPosition.x -= distance;
                break;
            case DIRECTION_NONE:
                break;
        }

        mLastTime = nanoTime;
    }

    public void drawCharacter(Canvas canvas) {
        mDestRect.offsetTo((int) mPosition.x, (int) mPosition.y);
        canvas.drawBitmap(mSpriteSheet, mSourceRect, mDestRect, null);
    }

    private long mLastTime;


    private final PointF mPosition;
    private final float mSpeed;
    private final Bitmap mSpriteSheet;
    private final Rect mSourceRect;
    private final Rect mDestRect;

    private static final String LOGTAG = "hallofpresidents.Character";
}
