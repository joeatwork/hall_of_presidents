package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class UIControls {

    public UIControls(AssetLoader assetLoader, LevelState levelState) {
        mLevelState = levelState;
        mDpadBitmap = assetLoader.loadDpadBitmap();
        mActionButtonBitmap = assetLoader.loadActionButtonBitmap();

        for (int i = 0; i < mActionButtons.length; i++) {
            mActionButtons[i] = new ActionButton();
            mActionButtons[i].rect.set(0, 0, mActionButtonBitmap.getWidth(), mActionButtonBitmap.getHeight());
        }

        mDpadDestRect = new Rect(0, 0, mDpadBitmap.getWidth(), mDpadBitmap.getHeight());

        mDialogUI = new DialogUI(assetLoader);
    }

    public void intepretInteractions(InputEvents.TouchSpot[] spots, Rect worldBounds) {
        boolean dpadPressed = false;
        for (int spotIx = 0; spotIx < spots.length && null != spots[spotIx]; spotIx++) {
            final InputEvents.TouchSpot spot = spots[spotIx];
            final int x = (int) spot.x;
            final int y = (int) spot.y;
            boolean touchWasOutside = true;

            if (mLevelState.canMove()) {
                if (mDpadDestRect.contains(x, y)) {
                    readDpad(x, y);
                    dpadPressed = true;
                    touchWasOutside = false;
                }
            }

            int worldX = x + worldBounds.left;
            int worldY = y + worldBounds.top;

            // ISSUE- button rects are in WORLD coords, touches are in SCREEN coords.
            for (int i = 0; i < mActionButtons.length; i++) {
                final ActionButton button = mActionButtons[i];
                if (button.enabled && button.rect.contains(worldX, worldY)) {
                    mLevelState.requestAction(button.spot);
                    touchWasOutside = false;
                }
            }

            if (touchWasOutside) {
                mLevelState.requestDismiss();
            }
        }
        if (!dpadPressed) {
            mLevelState.requestMovement(LevelState.Direction.DIRECTION_NONE);
        }
    }

    public void drawControls(Canvas canvas, Rect worldBounds, Rect viewBounds) {
        if (mLevelState.canMove()) {
            mDpadDestRect.offsetTo(0, viewBounds.height() - mDpadDestRect.height());
            canvas.drawBitmap(mDpadBitmap, null, mDpadDestRect, null);
        }

        updateActionButtons();

        for (int i = 0; i < mActionButtons.length; i++) {
            final ActionButton button = mActionButtons[i];
            if (button.enabled) {
                drawActionButton(canvas, button, worldBounds, viewBounds);
            }
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
                button.spot = spot;
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
        public LevelState.ActionSpot spot = null;
    }

    private final Bitmap mDpadBitmap;
    private final Bitmap mActionButtonBitmap;
    private final Rect mActionButtonDestRect = new Rect();
    private final ActionButton[] mActionButtons = new ActionButton[Config.MAX_ACTION_BUTTONS];
    private final LevelState mLevelState;
    private final DialogUI mDialogUI;
    private final Rect mDpadDestRect;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.UIControls";
}
