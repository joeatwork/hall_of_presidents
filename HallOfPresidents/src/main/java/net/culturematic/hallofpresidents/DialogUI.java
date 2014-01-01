package net.culturematic.hallofpresidents;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

public class DialogUI {
    public DialogUI(AssetLoader assetLoader) {
        mDialogPaddingRect = new Rect();
        mDialogDestRect = new Rect();
        mTextPaint = assetLoader.loadDialogTextPaint();
        mDialogBackground = assetLoader.loadDialogBackground();
    }

    public void drawDialog(String dialogText, Rect viewBounds, Canvas canvas) {
        mDialogBackground.getPadding(mDialogPaddingRect);
        int dialogWidth = viewBounds.width() - (mDialogPaddingRect.left + mDialogPaddingRect.right);
        StaticLayout dialogLayout = new StaticLayout(
                dialogText,
                mTextPaint,
                dialogWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.4f, // SpacingMult (Multiply line height)
                0.0f, // SpacingAdd (add to line height)
                false // IncludePad (no idea what this does)
        );

        int dialogBottom = mDialogPaddingRect.top + dialogLayout.getHeight() + mDialogPaddingRect.bottom;
        mDialogDestRect.set(viewBounds.left, viewBounds.top, viewBounds.right, dialogBottom);
        mDialogBackground.setBounds(mDialogDestRect);
        mDialogBackground.draw(canvas);

        canvas.save();
        canvas.translate(mDialogPaddingRect.left, mDialogPaddingRect.top);
        dialogLayout.draw(canvas);
        canvas.restore();
    }

    private final TextPaint mTextPaint;
    private final Rect mDialogPaddingRect;
    private final Rect mDialogDestRect;
    private final Drawable mDialogBackground;
}
