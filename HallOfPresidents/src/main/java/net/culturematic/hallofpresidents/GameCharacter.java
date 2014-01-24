package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

public class GameCharacter implements Comparable<GameCharacter> {
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
        mBoundsRect = new Rect();
        mCurrentSpriteRect = null;
    }

    /**
     * Sorts by y position. (That means moving characters sorts will go stale!)
     * @param gameCharacter
     * @return
     */
    @Override
    public int compareTo(GameCharacter gameCharacter) {
        float myY = getPosition().y;
        float otherY = gameCharacter.getPosition().y;
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

    public Rect getBounds() {
        int spriteWidth = mCurrentSpriteRect.width();
        int spriteHeight = mCurrentSpriteRect.height();
        int halfWidth = spriteWidth / 2;
        final PointF position = getPosition();
        int xDestOffset = ((int) position.x) - halfWidth;
        int yDestOffset = ((int) position.y) - spriteHeight;

        mBoundsRect.set(mCurrentSpriteRect);
        mBoundsRect.offsetTo(xDestOffset, yDestOffset);
        return mBoundsRect;
    }

    public Dialog getDialog() {
        return mCharacterState.getDialog();
    }

    public void directionCommand(long milliTime, LevelState.Direction direction, LevelState.Direction facing, Room currentRoom) {
        if (mLastTime < 0) {
            mLastTime = milliTime;
        }

        final long deltaTime = milliTime - mLastTime;
        final PointF position = getPosition();
        final float distance = mCharacterState.getSpeedPxPerMilli() * deltaTime;

        if (updatePosition(direction, distance, position, currentRoom, 2)) {
            // This is useful when debugging starting positions and event layouts
            // System.out.println("CHARACTER POSITION CHANGE TO " + position.x + ", " + position.y);
            mAnimationDistance = mAnimationDistance + distance;
            setCurrentMovingSprite(direction, mAnimationDistance);
        } else {
            mAnimationDistance = 0;
            setCurrentStandingSprite(facing, milliTime);
        }

        mLastTime = milliTime;
    }

    public void drawCharacter(Canvas canvas, int viewportOffsetX, int viewportOffsetY) {
        Rect bounds = getBounds();
        bounds.offset(-viewportOffsetX, -viewportOffsetY);
        canvas.drawBitmap(mCurrentBitmap, mCurrentSpriteRect, bounds, null);
    }

    public void recycle() {
        mCharacterState.recycle();
    }

    /**
     * Returns true if position is updated.
     */
    private boolean updatePosition(LevelState.Direction direction, float distance, PointF position, Room currentRoom, int tries) {
        // Recursion is a hack workaround. Should be RECTANGLES.
        if (0 >= tries) {
            return false;
        }
        final SpriteRenderer.Sprites sprites = mCharacterState.getSprites();
        final int halfBoundsWidth = sprites.boundsWidth / 2;
        final int boundsHeight = sprites.boundsHeight;
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

    private void setCurrentMovingSprite(LevelState.Direction direction, float distance) {
        SpriteRenderer.Sprites sprites = mCharacterState.getSprites();
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

    private void setCurrentStandingSprite(LevelState.Direction facing, long milliTime) {
        SpriteRenderer.Sprites sprites = mCharacterState.getSprites();
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

    private long mLastTime;
    private Bitmap mCurrentBitmap;
    private Rect mCurrentSpriteRect;
    private float mAnimationDistance;

    private final PointF mPosition;
    private final Rect mBoundsRect;
    private final CharacterState mCharacterState;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.GameCharacter";
}
