package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class UIControls {
    public UIControls(Bitmap dpad, Bitmap button) {
        mDpadBitmap = dpad;
        mButtonBitmap = button;

        mDpadDestRect = new Rect(0, 0, mDpadBitmap.getWidth(), mDpadBitmap.getHeight());
        mButtonDestRect = new Rect(0, 0, mButtonBitmap.getWidth(), mButtonBitmap.getHeight());
    }

    public void drawControls(Canvas canvas, Rect viewBounds) {
        // TODO cache this offset?
        mDpadDestRect.offsetTo(0, viewBounds.height() - mDpadDestRect.height());
        canvas.drawBitmap(mDpadBitmap, null, mDpadDestRect, null);

        mButtonDestRect.offsetTo(viewBounds.width() - mButtonDestRect.width(),
                                viewBounds.height() - mButtonDestRect.height());
        canvas.drawBitmap(mButtonBitmap, null, mButtonDestRect, null);
    }

    private final Bitmap mDpadBitmap;
    private final Bitmap mButtonBitmap;
    private final Rect mDpadDestRect;
    private final Rect mButtonDestRect;
}
