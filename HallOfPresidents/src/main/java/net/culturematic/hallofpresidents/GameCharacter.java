package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

public class GameCharacter {
    // HARDCODED LAYOUT
    // ( 0, 0) -> Standing facing down
    // ( 1, 0) -> Standing facing up
    // ( 2, 0) -> Standing facing left
    // ( x, 1), (x, 2), (x, 3) -> Walk cycles for facing X
    public GameCharacter(AssetLoader loader, RoomState roomState) {
        mRoomState = roomState;
        mSpriteSheet = loader.loadHeroSpritesBitmap();
        mSpriteSize = loader.scaleInt(128);
        mHalfSpriteSize = mSpriteSize / 2;
        mAnimationFrameDistance = loader.scaleInt(128 / 4);
        mSourceRect = new Rect(0, 0, mSpriteSize, mSpriteSize);
        mDestRect = new Rect(0, 0, mSpriteSize, mSpriteSize);

        mAnimationDirection = RoomState.Direction.DIRECTION_DOWN;
        mAnimationDistance = 0;
        mLastTime = -1;

        long stepsPerSecond = 2;
        mSpeedPxPerMilli = stepsPerSecond * mSpriteSize / 1000f;
    }

    public void directionCommand(long milliTime, RoomState.Direction direction, RoomState.Direction facing) {
        if (mLastTime < 0) {
            mLastTime = milliTime;
        }

        final long deltaTime = milliTime - mLastTime;
        final float distance = mSpeedPxPerMilli * deltaTime;
        final PointF position = mRoomState.getPosition();

        switch (direction) {
            case DIRECTION_UP:
                float yUp = position.y - distance;
                int yCheckUp = (int) yUp - mHalfSpriteSize;
                if (mCurrentRoom.inBounds((int) position.x + mHalfSpriteSize, yCheckUp) &&
                        mCurrentRoom.inBounds((int) position.x - mHalfSpriteSize, yCheckUp)) {
                    position.y = yUp;
                }
                break;
            case DIRECTION_DOWN:
                float yDown = position.y + distance;
                int yCheckDown = (int) yDown + mHalfSpriteSize;
                if (mCurrentRoom.inBounds((int) position.x + mHalfSpriteSize, yCheckDown) &&
                        mCurrentRoom.inBounds((int) position.x - mHalfSpriteSize, yCheckDown)) {
                    position.y = yDown;
                }
                break;
            case DIRECTION_RIGHT:
                float xRight = position.x + distance;
                int xCheckRight = (int) xRight + mHalfSpriteSize;
                if (mCurrentRoom.inBounds(xCheckRight, (int) position.y + mHalfSpriteSize) &&
                        mCurrentRoom.inBounds(xCheckRight, (int) position.y - mHalfSpriteSize)) {
                    position.x = xRight;
                }
                break;
            case DIRECTION_LEFT:
                float xLeft = position.x - distance;
                int xCheckLeft = (int) xLeft - mHalfSpriteSize;
                if (mCurrentRoom.inBounds(xCheckLeft, (int) position.y + mHalfSpriteSize) &&
                        mCurrentRoom.inBounds(xCheckLeft, (int) position.y - mHalfSpriteSize)) {
                    position.x = xLeft;
                }
                break;
            case DIRECTION_NONE:
                break;
        }

        mLastTime = milliTime;

        if (RoomState.Direction.DIRECTION_NONE == direction) {
            mAnimationDirection = facing;
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

        final PointF position = mRoomState.getPosition();
        int xDestOffset = ((int) position.x) - (mHalfSpriteSize + viewportOffsetX);
        int yDestOffset = ((int) position.y) - (mHalfSpriteSize + viewportOffsetY);
        mDestRect.offsetTo(xDestOffset, yDestOffset);
        canvas.drawBitmap(mSpriteSheet, mSourceRect, mDestRect, null);
    }

    public void recycle() {
        mSpriteSheet.recycle();
    }

    private long mLastTime;
    private RoomState.Direction mAnimationDirection;
    private float mAnimationDistance;
    private Room mCurrentRoom;

    private final RoomState mRoomState;
    private final int mSpriteSize;
    private final int mHalfSpriteSize;
    private final int mAnimationFrameDistance;
    private final float mSpeedPxPerMilli;
    private final Bitmap mSpriteSheet;
    private final Rect mSourceRect;
    private final Rect mDestRect;

    private static final int ANIMATION_LENGTH_IN_FRAMES = 4;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.GameCharacter";
}
