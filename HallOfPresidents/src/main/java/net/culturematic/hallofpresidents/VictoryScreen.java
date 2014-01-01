package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

public class VictoryScreen implements Screen {
    public VictoryScreen(AssetLoader assetLoader, Bitmap display, Rect viewBounds, Dialog victoryDialog) {
        mDisplay = display;
        mCanvas = new Canvas(mDisplay);
        mViewBounds = viewBounds;
        mVictoryDialog = victoryDialog;
        mDialogUI = new DialogUI(assetLoader);
        mDone = false;
    }

    @Override
    public void update(long milliTime, InputEvents.TouchSpot[] touchSpots) {
        mCanvas.drawColor(Color.WHITE);
        mDialogUI.drawDialog(mVictoryDialog.getDialog(), mViewBounds, mCanvas);

        if (touchSpots.length > 0 && null != touchSpots[0]) {
            mDone = true;
        }
    }

    @Override
    public boolean done() {
        return mDone;
    }

    @Override
    public void recycle() {

    }

    @Override
    public Screen nextScreen() {
        return null;
    }

    private boolean mDone;

    private final Bitmap mDisplay;
    private final Canvas mCanvas;
    private final Rect mViewBounds;
    private final Dialog mVictoryDialog;
    private final DialogUI mDialogUI;
}
