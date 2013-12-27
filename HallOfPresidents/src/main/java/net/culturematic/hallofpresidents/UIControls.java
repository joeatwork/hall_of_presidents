package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

public class UIControls {
    public UIControls(Bitmap dpad, Bitmap button) {
        mDpadBitmap = dpad;
        mButtonBitmap = button;

        mDpadDestRect = new Rect(0, 0, mDpadBitmap.getWidth(), mDpadBitmap.getHeight());
        mButtonDestRect = new Rect(0, 0, mButtonBitmap.getWidth(), mButtonBitmap.getHeight());
    }

    public void intepretInteractions(InputEvents.TouchSpot[] spots) {
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
                        Log.d(LOGTAG, "RIGHT");
                    } else {
                        Log.d(LOGTAG, "LEFT");
                    }
                } else { // Up or down
                    if (yOffset > 0) {
                        Log.d(LOGTAG, "DOWN");
                    } else {
                        Log.d(LOGTAG, "UP");
                    }
                }
            }
            if (mButtonDestRect.contains(x, y)) {
                Log.d(LOGTAG, "Button");
            }
        }
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

    private static final String LOGTAG = "hallofpresidents.UIControls";
}
