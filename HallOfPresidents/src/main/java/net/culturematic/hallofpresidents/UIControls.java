package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

public class UIControls {

    public enum Direction {
        DIRECTION_NONE,
        DIRECTION_UP,
        DIRECTION_RIGHT,
        DIRECTION_DOWN,
        DIRECTION_LEFT
    }

    public UIControls(Bitmap dpad, Bitmap button, Drawable dialogboxBackground, Typeface dialogFont) {
        mDpadBitmap = dpad;
        mButtonBitmap = button;
        mDialogboxBackground = dialogboxBackground;
        mDialogPaint = new TextPaint();
        mDialogPaint.setTypeface(dialogFont); // TODO get rid of font?
        mDialogPaint.setColor(Color.BLACK);
        mDialogPaint.setTextSize(24f);

        mDpadDestRect = new Rect(0, 0, mDpadBitmap.getWidth(), mDpadBitmap.getHeight());
        mButtonDestRect = new Rect(0, 0, mButtonBitmap.getWidth(), mButtonBitmap.getHeight());
        mDialogDestRect = new Rect(0, 0, 500, 500);
        mDialogTextDestRect = new Rect(mDialogDestRect);

        mButtonIsPressed = false;
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
                setDpad(x, y);
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
        mDialogAvailable = null;
    }

    public void addDialogCommand(Dialog dialog) {
        mDialogAvailable = dialog;
    }

    public void displayDialog(Dialog dialog) {
        mDialogShowing = dialog;
    }

    public Dialog getDialogCommand() {
        if (mButtonIsPressed) {
            return mDialogAvailable;
        }
        return null;
    }

    public void drawControls(Canvas canvas, Rect viewBounds) {
        mDpadDestRect.offsetTo(0, viewBounds.height() - mDpadDestRect.height());
        canvas.drawBitmap(mDpadBitmap, null, mDpadDestRect, null);

        if (null != mDialogAvailable) {
            mButtonDestRect.offsetTo(viewBounds.width() - mButtonDestRect.width(),
                                    viewBounds.height() - mButtonDestRect.height());
            canvas.drawBitmap(mButtonBitmap, null, mButtonDestRect, null);
        }

        if (null != mDialogShowing) {
            mDialogDestRect.set(viewBounds.top, viewBounds.left, viewBounds.right, viewBounds.centerY());
            mDialogboxBackground.setBounds(mDialogDestRect);
            mDialogboxBackground.draw(canvas);
            mDialogboxBackground.getPadding(mDialogTextDestRect);

            StaticLayout dialogLayout = new StaticLayout(
                mDialogShowing.getDialog(),
                mDialogPaint,
                mDialogDestRect.width(),
                Layout.Alignment.ALIGN_NORMAL,
                1.4f, // SpacingMult (Multiply line height)
                0.0f, // SpacingAdd (add to line height)
                false // IncludePad (no idea what this does)
            );

            canvas.save();
            canvas.translate(mDialogTextDestRect.left, mDialogTextDestRect.top);
            dialogLayout.draw(canvas);
            canvas.restore();
        }
    }

    private void setDpad(int x, int y) {
        if (null != mDialogShowing) {
            return;
        }

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

    private Direction mCurrentDirection;
    private Dialog mDialogAvailable;
    private Dialog mDialogShowing;
    private boolean mButtonIsPressed;

    private final TextPaint mDialogPaint;
    private final Bitmap mDpadBitmap;
    private final Bitmap mButtonBitmap;
    private final Drawable mDialogboxBackground;
    private final Rect mDpadDestRect;
    private final Rect mButtonDestRect;
    private final Rect mDialogDestRect;
    private final Rect mDialogTextDestRect;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.UIControls";
}
