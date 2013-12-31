package net.culturematic.hallofpresidents;

import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;
import java.util.Stack;

public class InputEvents implements View.OnTouchListener {

    public static final int MAX_TOUCH_SPOTS = 10;

    public static class TouchSpot {
        public float x;
        public float y;
    }

    public InputEvents() {
        mPointsDown = new SparseArray<TouchSpot>();
        mSpareSpots = new Stack<TouchSpot>();
        for (int i = 0; i < MAX_TOUCH_SPOTS; i++) {
            mSpareSpots.push(new TouchSpot());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        final int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            int pointerId = event.getPointerId(i);
            synchronized(mPointsDown) {
                switch(action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        if (! mSpareSpots.empty()) {
                            TouchSpot spot = mSpareSpots.pop();
                            spot.x = event.getX(i);
                            spot.y = event.getY(i);
                            mPointsDown.put(pointerId, spot);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        TouchSpot old = mPointsDown.get(pointerId);
                        if (null == old) {
                            Log.i(LOGTAG, "Got a pointer up without a corresponding pointer down.");
                        } else {
                            mSpareSpots.push(old);
                            mPointsDown.delete(pointerId);
                        }
                        break;
                }
            }// synchronized
        }
        return true;
    }// onTouch

    public void getPointsDown(TouchSpot[] spots) {
        Arrays.fill(spots, null);
        synchronized (mPointsDown) {
            int maxPoint = Math.min(spots.length, mPointsDown.size());
            for (int i = 0; i < maxPoint; i++) {
                spots[i] = mPointsDown.valueAt(i);
            }
        }
    }

    private final SparseArray<TouchSpot> mPointsDown;
    private final Stack<TouchSpot> mSpareSpots;

    @SuppressWarnings("unused")
    private final String LOGTAG = "hallofpresidents.InputEvents";
}
