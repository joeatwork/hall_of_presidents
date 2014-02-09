package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Sprites {
    public static class FrameInfo {
        public FrameInfo() {}
        public FrameInfo(Rect frame, Rect collision) {
            this.frame = frame;
            this.collision = collision;
        }

        public Rect frame;
        public Rect collision;
    }

    public Bitmap spriteBitmap;
    public int speedPxPerSecond;
    public int stepWidth;
    public int stepHeight;
    public int standFramesPerSecond;
    public FrameInfo[] standDownFrames = null;
    public FrameInfo[] standUpFrames = null;
    public FrameInfo[] standLeftFrames = null;
    public FrameInfo[] standRightFrames = null;
    public FrameInfo[] moveUpFrames = null;
    public FrameInfo[] moveDownFrames = null;
    public FrameInfo[] moveLeftFrames = null;
    public FrameInfo[] moveRightFrames = null;
}
