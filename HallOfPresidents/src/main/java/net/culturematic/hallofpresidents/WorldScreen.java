package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;

public class WorldScreen implements Screen {

    public WorldScreen(
            Bitmap display,
            Rect viewBounds,
            RoomState savedState,
            Room room,
            GameCharacter hero,
            UIControls controls) {
        mViewBounds = viewBounds;
        mWorldBounds = new Rect(viewBounds);
        mCanvas = new Canvas(display);
        mRoom = room;
        mHero = hero;
        mControls = controls;
        mRoomState = savedState;
        mHero.setRoom(room, mRoomState.getPosition());
    }

    @Override
    public void update(long milliTime, InputEvents.TouchSpot[] touchSpots) {
        mControls.intepretInteractions(milliTime, touchSpots);

        // TODO should be handled directly by hero
        RoomState.Direction move = mRoomState.getMovement();
        RoomState.Direction facing = mRoomState.getFacing();
        mHero.directionCommand(milliTime, move, facing);

        PointF heroPosition = mHero.getPosition(); // TODO Refactor to mRoomState
        WorldEvent worldEvent = mRoom.checkForEvent(heroPosition);
        if (null != worldEvent) {
            Dialog dialog = worldEvent.getDialog();
            mRoomState.setDialogAvailable(dialog);
        } else {
            mRoomState.setDialogAvailable(null);
        }
        // We want the hero at the center of the Viewport
        int worldOffsetX = (int) heroPosition.x - mViewBounds.centerX();
        int worldOffsetY = (int) heroPosition.y - mViewBounds.centerY();

        mWorldBounds.offsetTo(worldOffsetX, worldOffsetY);

        mCanvas.drawColor(Color.BLACK);
        mRoom.drawBackground(mCanvas, mWorldBounds, mViewBounds);
        mHero.drawCharacter(mCanvas, worldOffsetX, worldOffsetY);

        mRoom.drawFurniture(mCanvas, mWorldBounds, mViewBounds);
        mControls.drawControls(mCanvas, mViewBounds);

        mRoomState.setPosition(heroPosition);
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
    private final RoomState mRoomState;
}
