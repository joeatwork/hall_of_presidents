package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
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
        mSpriteSize = loader.scaleInt(128);
        mAnimationFrameDistance = loader.scaleInt(128 / 4);

        // TODO this rect needs to be sensitive to bitmap scale,
        // and to be chosen based on motion direction.
        mSourceRect = new Rect(0, 0, mSpriteSize, mSpriteSize);

        // TODO this rect needs to be centered almost all of the time.
        // There should probably be a viewport abstraction that deals with this position.
        mDestRect = new Rect(0, 0, mSpriteSize, mSpriteSize);

        mPosition = startPosition; // TODO This should be in GameState.
        mAnimationDirection = UIControls.Direction.DIRECTION_DOWN;
        mAnimationDistance = 0;
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
                float yUp = mPosition.y - distance;
                if (mCurrentRoom.inBounds((int) mPosition.x, (int) yUp)) {
                    mPosition.y = yUp;
                }
                break;
            case DIRECTION_DOWN:
                float yDown = mPosition.y + distance;
                if (mCurrentRoom.inBounds((int) mPosition.x, (int) yDown)) {
                    mPosition.y = yDown;
                }
                break;
            case DIRECTION_RIGHT:
                float xRight = mPosition.x + distance;
                if (mCurrentRoom.inBounds((int) xRight, (int) mPosition.y)) {
                    mPosition.x = xRight;
                }
                break;
            case DIRECTION_LEFT:
                float xLeft = mPosition.x - distance;
                if (mCurrentRoom.inBounds((int) xLeft, (int) mPosition.y)) {
                    mPosition.x = xLeft;
                }
                break;
            case DIRECTION_NONE:
                break;
        }

        mLastTime = nanoTime;

        if (UIControls.Direction.DIRECTION_NONE == direction) {
            mAnimationDistance = 0;
        } else if (mAnimationDirection != direction) {
            mAnimationDirection = direction;
            mAnimationDistance = distance % (mAnimationFrameDistance * ANIMATION_LENGTH_IN_FRAMES);
        } else {
            mAnimationDistance =
                    (mAnimationDistance + distance) % (mAnimationFrameDistance * ANIMATION_LENGTH_IN_FRAMES);
        }
    }

    public void setRoom(Room room) {
        mCurrentRoom = room;
    }

    public PointF getPosition() {
        return mPosition;
    }

    public void drawCharacter(Canvas canvas, int viewportOffsetX, int viewportOffsetY) {
        int frame = (int) mAnimationDistance / mAnimationFrameDistance;
        int frameOffset = frame * mSpriteSize;
        int spritesheetXOffset = 0;
        switch (mAnimationDirection) {
            case DIRECTION_NONE:
                throw new RuntimeException("Animation direction should never be NONE");
            case DIRECTION_DOWN:
                spritesheetXOffset = 0;
                break;
            case DIRECTION_UP:
                spritesheetXOffset = mSpriteSize;
                break;
            case DIRECTION_LEFT:
                spritesheetXOffset = mSpriteSize * 2;
                break;
            case DIRECTION_RIGHT:
                spritesheetXOffset = mSpriteSize * 3;
                break;
        }
        mSourceRect.set(spritesheetXOffset, frameOffset, spritesheetXOffset + mSpriteSize, frameOffset + mSpriteSize);

        int halfSize = mSpriteSize / 2;
        int xDestOffset = ((int) mPosition.x) - (halfSize + viewportOffsetX);
        int yDestOffset = ((int) mPosition.y) - (halfSize + viewportOffsetY);
        mDestRect.offsetTo(xDestOffset, yDestOffset);
        canvas.drawBitmap(mSpriteSheet, mSourceRect, mDestRect, null);
    }

    private long mLastTime;
    private UIControls.Direction mAnimationDirection;
    private float mAnimationDistance;
    private Room mCurrentRoom;

    private final int mSpriteSize;
    private final int mAnimationFrameDistance;
    private final PointF mPosition;
    private final float mSpeed;
    private final Bitmap mSpriteSheet;
    private final Rect mSourceRect;
    private final Rect mDestRect;

    private static final int ANIMATION_LENGTH_IN_FRAMES = 4;
    private static final String LOGTAG = "hallofpresidents.Character";
}
