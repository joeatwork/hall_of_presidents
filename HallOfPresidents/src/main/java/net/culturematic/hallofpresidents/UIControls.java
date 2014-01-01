package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;

public class UIControls {

    public UIControls(AssetLoader assetLoader, RoomState roomState) {
        mRoomState = roomState;
        mDpadBitmap = assetLoader.loadDpadBitmap();
        mButtonBitmap = assetLoader.loadButtonBitmap();
        mButtonPadding = assetLoader.getButtonPadding();
        mButtonTextPaint = assetLoader.loadButtonTextPaint();

        mDefaultPaint = new Paint();

        mSemiTransparentPaint = new Paint();
        mSemiTransparentPaint.setAlpha(128);

        mDpadDestRect = new Rect(0, 0, mDpadBitmap.getWidth(), mDpadBitmap.getHeight());
        mAButtonDestRect = new Rect(0, 0, mButtonBitmap.getWidth(), mButtonBitmap.getHeight());
        mBButtonDestRect = new Rect(0, 0, mButtonBitmap.getWidth(), mButtonBitmap.getHeight());

        mDialogUI = new DialogUI(assetLoader);
    }

    public void intepretInteractions(InputEvents.TouchSpot[] spots) {
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
                    mRoomState.pressAButton();
                }
            }

            if (mBButtonDestRect.contains(x, y)) {
                bButtonIsDown = true;
                if (! mBButtonWasDown) {
                    mBButtonWasDown = true;
                    mRoomState.pressBButton();
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
                    mButtonTextPaint);
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
                    mButtonTextPaint);
        }

        String dialogText = mRoomState.getDialogText();
        if (null != dialogText) {
            mDialogUI.drawDialog(dialogText, viewBounds, canvas);
            mRoomState.showedDialog();
        }
    }

    public void recycle() {
        mDpadBitmap.recycle();
        mButtonBitmap.recycle();
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

    private final TextPaint mButtonTextPaint;
    private final DialogUI mDialogUI;
    private final float mButtonPadding;
    private final Paint mSemiTransparentPaint;
    private final Paint mDefaultPaint;
    private final Bitmap mDpadBitmap;
    private final Bitmap mButtonBitmap;
    private final Rect mDpadDestRect;
    private final Rect mAButtonDestRect;
    private final Rect mBButtonDestRect;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.UIControls";
}
