package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

public class UIControls {

    public UIControls(AssetLoader assetLoader, RoomState roomState) {
        mRoomState = roomState;
        mDpadBitmap = assetLoader.loadDpadBitmap();
        mButtonBitmap = assetLoader.loadButtonBitmap();
        mDialogboxBackground = assetLoader.loadDialogBackground();
        mButtonPadding = assetLoader.getButtonPadding();

        mDefaultPaint = new Paint();

        mSemiTransparentPaint = new Paint();
        mSemiTransparentPaint.setAlpha(128);

        mDialogPaint = new TextPaint();
        mDialogPaint.setTypeface(assetLoader.loadDialogTypeface());
        mDialogPaint.setColor(Color.BLACK);
        mDialogPaint.setTextSize(assetLoader.getDialogFontSize());

        mDpadDestRect = new Rect(0, 0, mDpadBitmap.getWidth(), mDpadBitmap.getHeight());
        mAButtonDestRect = new Rect(0, 0, mButtonBitmap.getWidth(), mButtonBitmap.getHeight());
        mBButtonDestRect = new Rect(0, 0, mButtonBitmap.getWidth(), mButtonBitmap.getHeight());
        mDialogDestRect = new Rect(0, 0, 500, 500);
        mDialogTextDestRect = new Rect(mDialogDestRect);
    }

    public void intepretInteractions(long milliTime, InputEvents.TouchSpot[] spots) {
        boolean dpadPressed = false;
        boolean aButtonIsDown = false;
        boolean bButtonIsDown = false;
        for (int i = 0; i < spots.length && null != spots[i]; i++) {
            InputEvents.TouchSpot spot = spots[i];
            int x = (int) spot.x;
            int y = (int) spot.y;
            if (mDpadDestRect.contains(x, y)) {
                readDpad(x, y);
                dpadPressed = true;
            }

            if (mAButtonDestRect.contains(x, y)) {
                aButtonIsDown = true;
                if (! mAButtonWasDown) {
                    mAButtonWasDown = true;
                    mRoomState.aButton();
                }
            }

            if (mBButtonDestRect.contains(x, y)) {
                bButtonIsDown = true;
                if (! mBButtonWasDown) {
                    mBButtonWasDown = true;
                    mRoomState.bButton();
                }
            }
        }
        if (!dpadPressed) {
            mRoomState.requestMovement(RoomState.Direction.DIRECTION_NONE);
        }
        mAButtonWasDown = aButtonIsDown;
        mBButtonWasDown = bButtonIsDown;
    }

    public void drawControls(Canvas canvas, Rect viewBounds) {
        mDpadDestRect.offsetTo(0, viewBounds.height() - mDpadDestRect.height());
        canvas.drawBitmap(mDpadBitmap, null, mDpadDestRect, null);

        final String aButtonLabel = mRoomState.getAButtonLabel();
        final String bButtonLabel = mRoomState.getBButtonLabel();

        Paint bButtonPaint = mDefaultPaint;
        if (null == bButtonLabel) {
            bButtonPaint = mSemiTransparentPaint;
        }
        mBButtonDestRect.offsetTo(viewBounds.width() - mBButtonDestRect.width(),
                viewBounds.height() - mBButtonDestRect.height());
        canvas.drawBitmap(mButtonBitmap, null, mBButtonDestRect, bButtonPaint);
        if (null != bButtonLabel) {
            canvas.drawText(bButtonLabel,
                    mBButtonDestRect.left + mButtonPadding,
                    mBButtonDestRect.bottom - mButtonPadding,
                    mDialogPaint);
        }

        Paint aButtonPaint = mDefaultPaint;
        if (null == aButtonLabel) {
            aButtonPaint = mSemiTransparentPaint;
        }
        mAButtonDestRect.set(mBButtonDestRect);
        mAButtonDestRect.offset(0, - mBButtonDestRect.height());
        canvas.drawBitmap(mButtonBitmap, null, mAButtonDestRect, aButtonPaint);
        if (null != aButtonLabel) {
            canvas.drawText(aButtonLabel,
                    mAButtonDestRect.left + mButtonPadding,
                    mAButtonDestRect.bottom - mButtonPadding,
                    mDialogPaint);
        }

        String dialogText = mRoomState.getDialogText();
        if (null != dialogText) {
            mDialogboxBackground.getPadding(mDialogTextDestRect);
            int dialogWidth = viewBounds.width() - (mDialogTextDestRect.left + mDialogTextDestRect.right);
            StaticLayout dialogLayout = new StaticLayout(
                dialogText,
                mDialogPaint,
                dialogWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.4f, // SpacingMult (Multiply line height)
                0.0f, // SpacingAdd (add to line height)
                false // IncludePad (no idea what this does)
            );

            int dialogBottom = mDialogTextDestRect.top + dialogLayout.getHeight() + mDialogTextDestRect.bottom;
            mDialogDestRect.set(viewBounds.top, viewBounds.left, viewBounds.right, dialogBottom);
            mDialogboxBackground.setBounds(mDialogDestRect);
            mDialogboxBackground.draw(canvas);

            canvas.save();
            canvas.translate(mDialogTextDestRect.left, mDialogTextDestRect.top);
            dialogLayout.draw(canvas);
            canvas.restore();

            mRoomState.showedDialog();
        }
    }

    private void readDpad(int x, int y) {
        int xOffset = x - mDpadDestRect.centerX();
        int yOffset = y - mDpadDestRect.centerY();
        if (Math.abs(xOffset) > Math.abs(yOffset)) { // Left or Right
            if (xOffset > 0) {
                mRoomState.requestMovement(RoomState.Direction.DIRECTION_RIGHT);
            } else {
                mRoomState.requestMovement(RoomState.Direction.DIRECTION_LEFT);
            }
        } else { // Up or down
            if (yOffset > 0) {
                mRoomState.requestMovement(RoomState.Direction.DIRECTION_DOWN);
            } else {
                mRoomState.requestMovement(RoomState.Direction.DIRECTION_UP);
            }
        }
    }

    private boolean mAButtonWasDown = false;
    private boolean mBButtonWasDown = false;

    private final RoomState mRoomState;

    private final float mButtonPadding;
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

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.UIControls";
}
