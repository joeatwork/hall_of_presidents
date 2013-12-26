package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Game {
    public Game(Bitmap screen, GameState savedState) {
        if (null != savedState) {
            throw new RuntimeException("Restoring game state is unimplemented.");
        }
        mScreen = screen;
        mCanvas = new Canvas(mScreen);
        mPaint = new Paint();
        mPaint.setStrokeWidth(2); // TODO REMOVE
        mPaint.setColor(0x00FF00);
    }

    public void update(final long nanoTime) {
        int millis = (int) (nanoTime / 1000000);
        int red = millis & 0xff;
        int green = (millis >> 8) & 0xff;
        int blue = (millis >> 16) & 0xff;
        mCanvas.drawRGB(red, green, blue);
        mCanvas.drawCircle(10, 10, 10, mPaint);
    }

    public GameState getState() {
        return new GameState();
    }

    public static class GameState {
        // TODO
    }

    private final Bitmap mScreen;
    private final Canvas mCanvas;
    private final Paint mPaint;
}
