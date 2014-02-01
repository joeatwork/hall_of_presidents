package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

public class GameCharacter implements Figure {
    // HARDCODED LAYOUT
    // ( 0, 0) -> Standing facing down
    // ( 1, 0) -> Standing facing up
    // ( 2, 0) -> Standing facing left
    // ( x, 1), (x, 2), (x, 3) -> Walk cycles for facing X
    public GameCharacter(CharacterState characterState, PointF startPosition) {
        mCharacterState = characterState;
        mPosition = startPosition;
        mAnimationDistance = 0;
        mLastTime = -1;
        mImageBoundsRect = new Rect();
        mCollisionBoundsRect = new Rect();

        Sprites sprites = mCharacterState.getSprites();
        if (null == sprites) {
            mCurrentSpriteRect = EMPTY_RECT;
        } else {
            setCurrentStandingSprite(LevelState.Direction.DIRECTION_DOWN, 0, sprites);
        }
    }

    /**
     * Sorts by y position. (That means moving characters sorts will go stale!)
     */
    @Override
    public int compareTo(Figure other) {
        float myY = getPosition().y;
        float otherY = other.getPosition().y;
        if (myY < otherY) {
            return -1;
        } else if (myY > otherY) {
            return 1;
        } else {
            return 0;
        }
    }

    public PointF getPosition() {
        return mPosition;
    }

    public Rect getImageBounds() {
        int spriteWidth = mCurrentSpriteRect.width();
        int spriteHeight = mCurrentSpriteRect.height();
        int halfWidth = spriteWidth / 2;
        final PointF position = getPosition();
        int xDestOffset = ((int) position.x) - halfWidth;
        int yDestOffset = ((int) position.y) - spriteHeight;

        mImageBoundsRect.set(mCurrentSpriteRect);
        mImageBoundsRect.offsetTo(xDestOffset, yDestOffset);
        return mImageBoundsRect;
    }

    public Rect getCollisionBounds() {
        int spriteWidth = mCurrentSpriteRect.width();
        int spriteHeight = mCurrentSpriteRect.height();
        int halfWidth = spriteWidth / 2;
        int thirdHeight = spriteHeight / 3;
        final PointF position = getPosition();
        int left = ((int) position.x) - halfWidth;
        int top = ((int) position.y) - thirdHeight;
        int right = ((int) position.x) + halfWidth;
        int bottom = (int) position.y;

        mCollisionBoundsRect.set(left, top, right, bottom);
        return mCollisionBoundsRect;
    }

    public Dialog getDialog() {
        return mCharacterState.getDialog();
    }

    public int getDialogOffsetY() {
        return mCharacterState.getDialogOffsetY();
    }

    public void update(long milliTime, LevelState levelState) {
        mThisTime = milliTime;
        mCharacterState.setLevelState(levelState);
    }

    public void directionCommand(LevelState.Direction direction, LevelState.Direction facing, Room currentRoom) {
        if (mLastTime < 0) {
            mLastTime = mThisTime;
        }

        final long deltaTime = mThisTime - mLastTime;
        final PointF position = getPosition();
        final float distance = mCharacterState.getSpeedPxPerMilli() * deltaTime;

        final boolean moved = updatePosition(direction, distance, position, currentRoom, 3);
        final Sprites sprites = mCharacterState.getSprites();
        if (null == sprites) {
            mCurrentSpriteRect = EMPTY_RECT;
        } else {
            if (moved) {
                // This is useful when debugging starting positions and event layouts
                // System.out.println("CHARACTER POSITION CHANGE TO " + position.x + ", " + position.y);
                mAnimationDistance = mAnimationDistance + distance;
                setCurrentMovingSprite(direction, mAnimationDistance, sprites);
            } else {
                mAnimationDistance = 0;
                setCurrentStandingSprite(facing, mThisTime, sprites);
            }
        }

        mLastTime = mThisTime;
    }

    public void drawCharacter(Canvas canvas, Rect worldRect) {
        if (mCurrentSpriteRect.isEmpty()) {
            return;
        }

        Rect bounds = getImageBounds();
        if (Rect.intersects(worldRect, bounds)) {
            int viewportOffsetX = worldRect.left;
            int viewportOffsetY = worldRect.top;
            bounds.offset(-viewportOffsetX, -viewportOffsetY);
            canvas.drawBitmap(mCurrentBitmap, mCurrentSpriteRect, bounds, null);
        }
    }

    /**
     * Returns true if position is updated.
     */
    private boolean updatePosition(LevelState.Direction direction, float distance, PointF position, Room currentRoom, int tries) {
        // Recursion is a hack workaround. Should be RECTANGLES.
        if (0 >= tries) {
            return false;
        }

        final Sprites sprites = mCharacterState.getSprites();
        if (null == sprites) {
            return false;
        }

        final Rect collisionBounds = getCollisionBounds();
        final int halfBoundsWidth = collisionBounds.width() / 2;
        final int boundsHeight = collisionBounds.height();
        final int halfBoundsHeight = boundsHeight / 2;

        boolean ret = false;
        switch (direction) {
            case DIRECTION_UP:
                float yUp = position.y - distance;
                int yCheckUp = (int) yUp - boundsHeight;

                if (currentRoom.inBounds((int) position.x + halfBoundsWidth, yCheckUp) &&
                        currentRoom.inBounds((int) position.x, yCheckUp) &&
                        currentRoom.inBounds((int) position.x - halfBoundsWidth, yCheckUp)) {
                    position.y = yUp;
                    ret = true;
                } else {
                    ret = updatePosition(direction, distance/2, position, currentRoom, tries - 1);
                }
                break;
            case DIRECTION_DOWN:
                float yDown = position.y + distance;
                int yCheckDown = (int) yDown;

                if (currentRoom.inBounds((int) position.x + halfBoundsWidth, yCheckDown) &&
                        currentRoom.inBounds((int) position.x, yCheckDown) &&
                        currentRoom.inBounds((int) position.x - halfBoundsWidth, yCheckDown)) {
                    position.y = yDown;
                    ret = true;
                } else {
                    ret = updatePosition(direction, distance/2, position, currentRoom, tries - 1);
                }
                break;
            case DIRECTION_RIGHT:
                float xRight = position.x + distance;
                int xCheckRight = (int) xRight + halfBoundsWidth;

                if (currentRoom.inBounds(xCheckRight, (int) position.y - boundsHeight) &&
                        currentRoom.inBounds(xCheckRight, (int) position.y - halfBoundsHeight) &&
                        currentRoom.inBounds(xCheckRight, (int) position.y)) {
                    position.x = xRight;
                    ret = true;
                } else {
                    ret = updatePosition(direction, distance/2, position, currentRoom, tries - 1);
                }
                break;
            case DIRECTION_LEFT:
                float xLeft = position.x - distance;
                int xCheckLeft = (int) xLeft - halfBoundsWidth;

                if (currentRoom.inBounds(xCheckLeft, (int) position.y - boundsHeight) &&
                        currentRoom.inBounds(xCheckLeft, (int) position.y - halfBoundsHeight) &&
                        currentRoom.inBounds(xCheckLeft, (int) position.y)) {
                    position.x = xLeft;
                    ret = true;
                } else {
                    ret = updatePosition(direction, distance/2, position, currentRoom, tries - 1);
                }
                break;
            case DIRECTION_NONE:
                break;
        }

        return ret;
    }

    private void setCurrentMovingSprite(LevelState.Direction direction, float distance, Sprites sprites) {
        mCurrentBitmap = sprites.spriteBitmap;

        Rect[] animationFrames = null;
        int animationLength = 0;
        switch(direction) {
            case DIRECTION_UP:
                animationFrames = sprites.moveUpFrames;
                animationLength = sprites.boundsHeight;
                break;
            case DIRECTION_DOWN:
                animationFrames = sprites.moveDownFrames;
                animationLength = sprites.boundsHeight;
                break;
            case DIRECTION_LEFT:
                animationFrames = sprites.moveLeftFrames;
                animationLength = sprites.boundsWidth;
                break;
            case DIRECTION_RIGHT:
                animationFrames = sprites.moveRightFrames;
                animationLength = sprites.boundsWidth;
                break;
            case DIRECTION_NONE:
                throw new RuntimeException("Can't move in DIRECTION_NONE");
        }

        float frameDistance = distance % animationLength;
        int frameIndex = (int) (animationFrames.length * frameDistance / animationLength);
        mCurrentSpriteRect = animationFrames[frameIndex];
    }

    private void setCurrentStandingSprite(LevelState.Direction facing, long milliTime, Sprites sprites) {
        mCurrentBitmap = sprites.spriteBitmap;
        Rect[] animationFrames = null;
        switch (facing) {
            case DIRECTION_UP:
                animationFrames = sprites.standUpFrames;
                break;
            case DIRECTION_DOWN:
                animationFrames = sprites.standDownFrames;
                break;
            case DIRECTION_LEFT:
                animationFrames = sprites.standLeftFrames;
                break;
            case DIRECTION_RIGHT:
                animationFrames = sprites.standRightFrames;
                break;
            case DIRECTION_NONE:
                throw new RuntimeException("Can't face DIRECTION_NONE");
        }

        long twoHour = milliTime % (2 * 60 * 60 * 1000); // Overflow prevention
        long totalFrames = (twoHour * sprites.standFramesPerSecond) / 1000;
        int offsetFrame = (int) (totalFrames % animationFrames.length);
        mCurrentSpriteRect = animationFrames[offsetFrame];
    }

    private long mThisTime;
    private long mLastTime;
    private Bitmap mCurrentBitmap;
    private Rect mCurrentSpriteRect;
    private float mAnimationDistance;

    private final PointF mPosition;
    private final Rect mImageBoundsRect;
    private final Rect mCollisionBoundsRect;
    private final CharacterState mCharacterState;

    private static final Rect EMPTY_RECT = new Rect(0, 0, 0, 0);

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.GameCharacter";
}
