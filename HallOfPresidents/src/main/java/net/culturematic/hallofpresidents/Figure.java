package net.culturematic.hallofpresidents;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

public interface Figure extends Comparable<Figure> {
    /**
     * Update the figure state in the room, given the new circumstances of this tick.
     * @param millitime
     * @param levelState
     */
    public void update(long millitime, LevelState levelState);

    public PointF getPosition();
    public Rect getCollisionBounds();
    public Rect getImageBounds();
    public Dialog getDialog();
    public int getDialogOffsetY();

    // TODO REMOVE - THIS SHOULD BE HIDDEN AND CALLED BY A DRIVER
    public void directionCommand(LevelState.Direction direction, LevelState.Direction facing, Room currentRoom);

    // TODO RENAME to draw
    public void drawCharacter(Canvas canvas, Rect worldRect);
}
