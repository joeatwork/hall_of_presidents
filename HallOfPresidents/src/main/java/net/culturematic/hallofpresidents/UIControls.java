package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

// TODO bit of an informal state machine here, should probably be a formal state machine.
public class UIControls {

    public enum Direction {
        DIRECTION_NONE,
        DIRECTION_UP,
        DIRECTION_RIGHT,
        DIRECTION_DOWN,
        DIRECTION_LEFT
    }

    public static class CancelCommand {}

    public UIControls(Bitmap dpad, Bitmap button, Drawable dialogboxBackground, Typeface dialogFont) {
        mDpadBitmap = dpad;
        mButtonBitmap = button;
        mDialogboxBackground = dialogboxBackground;

        mDefaultPaint = new Paint();

        mSemiTransparentPaint = new Paint();
        mSemiTransparentPaint.setAlpha(128);

        mDialogPaint = new TextPaint();
        mDialogPaint.setTypeface(dialogFont); // TODO get rid of font?
        mDialogPaint.setColor(Color.BLACK);
        mDialogPaint.setTextSize(24f);

        mDpadDestRect = new Rect(0, 0, mDpadBitmap.getWidth(), mDpadBitmap.getHeight());
        mAButtonDestRect = new Rect(0, 0, mButtonBitmap.getWidth(), mButtonBitmap.getHeight());
        mBButtonDestRect = new Rect(0, 0, mButtonBitmap.getWidth(), mButtonBitmap.getHeight());
        mDialogDestRect = new Rect(0, 0, 500, 500);
        mDialogTextDestRect = new Rect(mDialogDestRect);

        mAButtonIsPressed = false;
        mBButtonIsPressed = false;
        mCancelAvailable = null;
    }

    public void intepretInteractions(InputEvents.TouchSpot[] spots) {
        mCurrentDirection = Direction.DIRECTION_NONE;
        boolean buttonWasPressed = mAButtonIsPressed;
        mAButtonIsPressed = false;
        mBButtonIsPressed = false;
        for (int i = 0; i < spots.length; i++) {
            InputEvents.TouchSpot spot = spots[i];
            if (null == spot) break;

            int x = (int) spot.x;
            int y = (int) spot.y;
            if (mDpadDestRect.contains(x, y)) {
                setDpad(x, y);
            }
            if (mAButtonDestRect.contains(x, y)) {
                mAButtonIsPressed = true;
            }
            if (mBButtonDestRect.contains(x, y)) {
                mBButtonIsPressed = true;
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
        if (null == mDialogShowing) {
            mDialogAvailable = dialog;
        }
    }

    public void cancel(CancelCommand command) {
        mDialogShowing = null;
        mCancelAvailable = null;
    }

    public void displayDialog(Dialog dialog) {
        mDialogShowing = dialog;
        if (null != mDialogShowing) {
            mDialogAvailable = null;
            mCancelAvailable = CANCEL_COMMAND;
        }
    }

    public Dialog getDialogCommand() {
        if (mAButtonIsPressed) {
            return mDialogAvailable;
        }
        return null;
    }

    public CancelCommand getCancelCommand() {
        if (mBButtonIsPressed) {
            return mCancelAvailable;
        }
        return null;
    }

    public void drawControls(Canvas canvas, Rect viewBounds) {
        mDpadDestRect.offsetTo(0, viewBounds.height() - mDpadDestRect.height());
        canvas.drawBitmap(mDpadBitmap, null, mDpadDestRect, null);

        Paint bButtonPaint = mDefaultPaint;
        if (null == mCancelAvailable) {
            bButtonPaint = mSemiTransparentPaint;
        }
        mBButtonDestRect.offsetTo(viewBounds.width() - mBButtonDestRect.width(),
                viewBounds.height() - mBButtonDestRect.height());
        canvas.drawBitmap(mButtonBitmap, null, mBButtonDestRect, bButtonPaint);

        Paint aButtonPaint = mDefaultPaint;
        if (null == mDialogAvailable) {
            aButtonPaint = mSemiTransparentPaint;
        }
        mAButtonDestRect.set(mBButtonDestRect);
        mAButtonDestRect.offset(0, - mBButtonDestRect.height());
        canvas.drawBitmap(mButtonBitmap, null, mAButtonDestRect, aButtonPaint);

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
    private CancelCommand mCancelAvailable;
    private boolean mAButtonIsPressed;
    private boolean mBButtonIsPressed;

    private final Paint mSemiTransparentPaint;
    private final Paint mDefaultPaint;
    private final TextPaint mDialogPaint;
    private final Bitmap mDpadBitmap;
    private final Bitmap mButtonBitmap;
    private final Drawable mDialogboxBackground;
    private final Rect mDpadDestRect;
    private final Rect mAButtonDestRect;
    private final Rect mBButtonDestRect;
    private final Rect mDialogDestRect;
    private final Rect mDialogTextDestRect;

    private static final CancelCommand CANCEL_COMMAND = new CancelCommand();

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.UIControls";
}
