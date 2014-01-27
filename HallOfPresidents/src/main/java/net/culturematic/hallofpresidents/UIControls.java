package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.TextPaint;

public class UIControls {

    public UIControls(AssetLoader assetLoader, LevelState levelState) {
        mLevelState = levelState;
        mDpadBitmap = assetLoader.loadDpadBitmap();
        mActionButtonBitmap = assetLoader.loadActionButtonBitmap();
        mButtonBitmap = assetLoader.loadButtonBitmap();
        mButtonPadding = assetLoader.getButtonPadding();
        mButtonTextPaint = assetLoader.loadButtonTextPaint();

        for (int i = 0; i < mActionButtons.length; i++) {
            mActionButtons[i] = new ActionButton();
            mActionButtons[i].rect.set(0, 0, mActionButtonBitmap.getWidth(), mActionButtonBitmap.getHeight());
        }

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
                    mLevelState.pressAButton();
                }
            }

            if (mBButtonDestRect.contains(x, y)) {
                bButtonIsDown = true;
                if (! mBButtonWasDown) {
                    mBButtonWasDown = true;
                    mLevelState.pressBButton();
                }
            }
        }
        if (!dpadPressed) {
            mLevelState.requestMovement(LevelState.Direction.DIRECTION_NONE);
        }
        mAButtonWasDown = aButtonIsDown;
        mBButtonWasDown = bButtonIsDown;
    }

    public void drawControls(Canvas canvas, Rect worldBounds, Rect viewBounds) {
        mDpadDestRect.offsetTo(0, viewBounds.height() - mDpadDestRect.height());
        canvas.drawBitmap(mDpadBitmap, null, mDpadDestRect, null);

        updateActionButtons();

        for (int i = 0; i < mActionButtons.length; i++) {
            final ActionButton button = mActionButtons[i];
            if (button.enabled) {
                drawActionButton(canvas, button, worldBounds, viewBounds);
            }
        }

        final String aButtonLabel = mLevelState.getAButtonLabel();
        final String bButtonLabel = mLevelState.getBButtonLabel();

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

        String dialogText = mLevelState.getDialogText();
        if (null != dialogText) {
            mDialogUI.drawDialog(dialogText, viewBounds, canvas);
        }
    }

    private void updateActionButtons() {
        final LevelState.ActionSpot[] actions = mLevelState.getActions();
        final int actionOffsetX = mActionButtonBitmap.getWidth() / 2;
        final int actionOffsetY = mActionButtonBitmap.getHeight();
        for (int i = 0; i < actions.length; i++) {
            final LevelState.ActionSpot spot = actions[i];
            final ActionButton button = mActionButtons[i];
            if (spot.enabled) {
                button.enabled = true;
                button.key = spot;
                button.rect.offsetTo(
                        (int) spot.position.x - actionOffsetX,
                        (int) spot.position.y - actionOffsetY
                );
            } else {
                button.enabled = false;
            }
        }
    }

    private void drawActionButton(Canvas canvas, ActionButton button, Rect worldBounds, Rect viewBounds) {
        mActionButtonDestRect.set(button.rect);
        mActionButtonDestRect.offset(-worldBounds.left, -worldBounds.top);
        canvas.drawBitmap(mActionButtonBitmap, null, mActionButtonDestRect, null);
    }

    private void readDpad(int x, int y) {
        int xOffset = x - mDpadDestRect.centerX();
        int yOffset = y - mDpadDestRect.centerY();
        if (Math.abs(xOffset) > Math.abs(yOffset)) { // Left or Right
            if (xOffset > 0) {
                mLevelState.requestMovement(LevelState.Direction.DIRECTION_RIGHT);
            } else {
                mLevelState.requestMovement(LevelState.Direction.DIRECTION_LEFT);
            }
        } else { // Up or down
            if (yOffset > 0) {
                mLevelState.requestMovement(LevelState.Direction.DIRECTION_DOWN);
            } else {
                mLevelState.requestMovement(LevelState.Direction.DIRECTION_UP);
            }
        }
    }

    private class ActionButton {
        public final Rect rect = new Rect();
        public boolean enabled = false;
        public Object key = null;
    }

    private boolean mAButtonWasDown = false;
    private boolean mBButtonWasDown = false;

    private final Rect mActionButtonDestRect = new Rect();
    private final ActionButton[] mActionButtons = new ActionButton[Config.MAX_ACTION_BUTTONS];
    private final LevelState mLevelState;
    private final TextPaint mButtonTextPaint;
    private final DialogUI mDialogUI;
    private final float mButtonPadding;
    private final Paint mSemiTransparentPaint;
    private final Paint mDefaultPaint;
    private final Bitmap mDpadBitmap;
    private final Bitmap mButtonBitmap;
    private final Bitmap mActionButtonBitmap;
    private final Rect mDpadDestRect;
    private final Rect mAButtonDestRect;
    private final Rect mBButtonDestRect;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.UIControls";
}
