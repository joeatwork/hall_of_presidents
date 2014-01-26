package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Sprites {
    public Bitmap spriteBitmap;
    public int speedPxPerSecond;
    public int boundsWidth;
    public int boundsHeight;
    public int standFramesPerSecond;
    public Rect[] standDownFrames = null;
    public Rect[] standUpFrames = null;
    public Rect[] standLeftFrames = null;
    public Rect[] standRightFrames = null;
    public Rect[] moveUpFrames = null;
    public Rect[] moveDownFrames = null;
    public Rect[] moveLeftFrames = null;
    public Rect[] moveRightFrames = null;
}
