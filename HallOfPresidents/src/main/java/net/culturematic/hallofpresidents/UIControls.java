package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

public class UIControls {

    public enum Direction {
        DIRECTION_NONE,
        DIRECTION_UP,
        DIRECTION_RIGHT,
        DIRECTION_DOWN,
        DIRECTION_LEFT
    }

    public UIControls(Bitmap dpad, Bitmap button) {
        mDpadBitmap = dpad;
        mButtonBitmap = button;

        mDpadDestRect = new Rect(0, 0, mDpadBitmap.getWidth(), mDpadBitmap.getHeight());
        mButtonDestRect = new Rect(0, 0, mButtonBitmap.getWidth(), mButtonBitmap.getHeight());
    }

    public void intepretInteractions(InputEvents.TouchSpot[] spots) {
        mCurrentDirection = Direction.DIRECTION_NONE;
        mButtonIsPressed = false;
        for (int i = 0; i < spots.length; i++) {
            InputEvents.TouchSpot spot = spots[i];
            if (null == spot) break;

            int x = (int) spot.x;
            int y = (int) spot.y;
            if (mDpadDestRect.contains(x, y)) {
                int xOffset = x - mDpadDestRect.centerX();
                int yOffset = y - mDpadDestRect.centerY();
                if (Math.abs(xOffset) > Math.abs(yOffset)) { // Left or Right
                    if (xOffset > 0) {
                        mCurrentDirection = Direction.DIRECTION_RIGHT;
                    } else {
                        mCurrentDirection = Direction.DIRECTION_LEFT;
                    }
                } else { // Up or down
                    if (yOffset > 0) {
                        mCurrentDirection = Direction.DIRECTION_DOWN;
                    } else {
                        mCurrentDirection = Direction.DIRECTION_UP;
                    }
                }
            }
            if (mButtonDestRect.contains(x, y)) {
                mButtonIsPressed = true;
            }
        }
    }

    public Direction currentDirection() {
        return mCurrentDirection;
    }

    public void clearCommands() {
        mDialog = null;
    }

    public void addDialogCommand(Dialog dialog) {
        mDialog = dialog;
    }

    public Dialog getDialogCommand() {
        if (mButtonIsPressed) {
            return mDialog;
        }
        return null;
    }

    public void drawControls(Canvas canvas, Rect viewBounds) {
        // TODO cache this offset?
        mDpadDestRect.offsetTo(0, viewBounds.height() - mDpadDestRect.height());
        canvas.drawBitmap(mDpadBitmap, null, mDpadDestRect, null);

        if (null != mDialog) {
            mButtonDestRect.offsetTo(viewBounds.width() - mButtonDestRect.width(),
                                    viewBounds.height() - mButtonDestRect.height());
            canvas.drawBitmap(mButtonBitmap, null, mButtonDestRect, null);
        }
    }

    private Direction mCurrentDirection;
    private Dialog mDialog;
    private boolean mButtonIsPressed;

    private final Bitmap mDpadBitmap;
    private final Bitmap mButtonBitmap;
    private final Rect mDpadDestRect;
    private final Rect mButtonDestRect;

    private static final String LOGTAG = "hallofpresidents.UIControls";
}
