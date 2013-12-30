package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;

/**
 * Created by joe on 12/30/13.
 */
public class WorldScreen implements Screen {

    public WorldScreen(
            Bitmap display,
            Rect viewBounds,
            Game.GameState savedState,
            Room room,
            GameCharacter hero,
            UIControls controls) {
        if (null != savedState) {
            throw new RuntimeException("Restoring world from GameState isn't implemented yet");
        }
        mViewBounds = viewBounds;
        mWorldBounds = new Rect(viewBounds);
        mCanvas = new Canvas(display);
        mRoom = room;
        mHero = hero;
        mControls = controls;
        mHero.setRoom(room, room.defaultDoor());
    }

    @Override
    public void update(long milliTime, InputEvents.TouchSpot[] touchSpots) {
        mControls.intepretInteractions(touchSpots);

        UIControls.CancelCommand cancelCommand = mControls.getCancelCommand();
        if (null != cancelCommand) {
            mControls.cancel(cancelCommand);
        }

        Dialog dialogCommand = mControls.getDialogCommand();
        if (null != dialogCommand) {
            mControls.displayDialog(dialogCommand);
            mHero.setFacing(dialogCommand.getFacing());
        }

        mControls.clearCommands();

        mHero.directionCommand(milliTime, mControls.currentDirection());

        PointF heroOffset = mHero.getPosition();
        WorldEvent worldEvent = mRoom.checkForEvent(heroOffset);
        if (null != worldEvent) {
            Dialog dialog = worldEvent.getDialog();
            mControls.addDialogCommand(dialog);
        }
        // We want the hero at the center of the Viewport
        int worldOffsetX = (int) heroOffset.x - mViewBounds.centerX();
        int worldOffsetY = (int) heroOffset.y - mViewBounds.centerY();

        mWorldBounds.offsetTo(worldOffsetX, worldOffsetY);

        mCanvas.drawColor(Color.BLACK);
        mRoom.drawBackground(mCanvas, mWorldBounds, mViewBounds);
        mHero.drawCharacter(mCanvas, worldOffsetX, worldOffsetY);

        mRoom.drawFurniture(mCanvas, mWorldBounds, mViewBounds);
        mControls.drawControls(mCanvas, mViewBounds);
    }

    @Override
    public Screen nextScreen() {
        return null;
    }

    @Override
    public void recycle() {
        // TODO- recycle mHero, mRoom, mControls here
    }

    private Room mRoom;
    private final Canvas mCanvas;
    private final Rect mViewBounds; // Area of screen for us to draw on
    private final Rect mWorldBounds; // Area of the world to show on the screen
    private final GameCharacter mHero;
    private final UIControls mControls;
}
