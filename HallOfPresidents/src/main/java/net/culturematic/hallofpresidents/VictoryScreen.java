package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

public class VictoryScreen implements Screen {
    public VictoryScreen(AssetLoader assetLoader, Bitmap display, Rect viewBounds, Dialog victoryDialog) {
        mCanvas = new Canvas(display);
        mViewBounds = viewBounds;
        mVictoryDialog = victoryDialog;
        mDialogUI = new DialogUI(assetLoader);
        mDone = false;
    }

    @Override
    public String getHelpMessage() {
        return null;
    }

    @Override
    public void update(long milliTime, InputEvents.TouchSpot[] touchSpots) {
        if (mStartedMillis < 0) {
            mStartedMillis = milliTime;
        }
        mCanvas.drawColor(Color.WHITE);
        mDialogUI.drawDialog(mVictoryDialog.getDialog(), mViewBounds, mCanvas);

        if (milliTime - mStartedMillis > MIN_SHOW_TIME_MILLIS) {
            if (touchSpots.length > 0 && null != touchSpots[0]) {
                mDone = true;
            }
        }
    }

    @Override
    public boolean done() {
        return mDone;
    }

    @Override
    public Screen nextScreen() {
        return null;
    }

    private boolean mDone;
    private long mStartedMillis = -1;

    private final Canvas mCanvas;
    private final Rect mViewBounds;
    private final Dialog mVictoryDialog;
    private final DialogUI mDialogUI;

    private static final int MIN_SHOW_TIME_MILLIS = 1000;
}
