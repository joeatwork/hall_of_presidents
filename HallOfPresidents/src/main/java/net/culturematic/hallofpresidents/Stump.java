package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

public class Stump implements Figure {
    public Stump(Bitmap bitmap, PointF position, Rect drawRegion, Rect collideRegion) {
        mBitmap = bitmap;
        mPosition = position;
        mDrawRegion = drawRegion;
        mCollideRegion = collideRegion;
        mImageBounds = new Rect(mDrawRegion);
    }

    @Override
    public void update(long millitime, LevelState levelState) {
        ; // Do nothing - Stumps don't change
    }

    @Override
    public PointF getPosition() {
        return mPosition;
    }

    @Override
    public Rect getCollisionBounds() {
        final PointF position = getPosition();
        final int halfWidth = mCollideRegion.width() / 2;
        final int height = mCollideRegion.height();
        mCollideRegion.offsetTo(
             ((int) position.x) - halfWidth,
             ((int) position.y) - height
        );
        return mCollideRegion;
    }

    @Override
    public Rect getImageBounds() {
        final PointF position = getPosition();
        final int halfWidth = mImageBounds.width() / 2;
        final int height = mImageBounds.height();
        mImageBounds.offsetTo(
            ((int) position.x) - halfWidth,
            ((int) position.y) - height
        );
        return mImageBounds;
    }

    @Override
    public Dialog getDialog() {
        return null;
    }

    @Override
    public int getDialogOffsetY() {
        return 0;
    }

    @Override
    public void directionCommand(LevelState.Direction direction, LevelState.Direction facing, Room currentRoom) {
        ; // Do nothing, this method shouldn't even be here.
    }

    @Override
    public void drawCharacter(Canvas canvas, Rect worldRect) {
        Rect bounds = getImageBounds();
        if (Rect.intersects(bounds, worldRect) && ! bounds.isEmpty()) {
            int viewportOffsetX = worldRect.left;
            int viewportOffsetY = worldRect.top;
            bounds.offset(-viewportOffsetX, -viewportOffsetY);
            canvas.drawBitmap(mBitmap, mDrawRegion, bounds, null);
        }
    }

    private final Bitmap mBitmap;
    private final PointF mPosition;
    private final Rect mDrawRegion;
    private final Rect mImageBounds;
    private final Rect mCollideRegion;

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
}
