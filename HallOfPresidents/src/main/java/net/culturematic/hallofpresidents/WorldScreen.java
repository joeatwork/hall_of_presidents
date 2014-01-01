package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import java.util.Set;

public class WorldScreen implements Screen {

    public WorldScreen(
            AssetLoader assetLoader,
            Bitmap display,
            Rect viewBounds,
            RoomState savedState,
            Room room,
            GameCharacter hero,
            UIControls controls) {
        mAssetLoader = assetLoader;
        mNextScreen = null;
        mViewBounds = viewBounds;
        mWorldBounds = new Rect(viewBounds);
        mDisplay = display;
        mCanvas = new Canvas(display);
        mRoom = room;
        mVictoryFlags = mRoom.getVictory().getRoomFlagsToRequire();
        mHero = hero;
        mControls = controls;
        mRoomState = savedState;
        mHero.setRoom(room);
    }

    @Override
    public void update(long milliTime, InputEvents.TouchSpot[] touchSpots) {

        if (mRoomState.canGetVictory()) {
            Set<String> hasFlags = mRoomState.getRoomFlags();
            if (hasFlags.containsAll(mVictoryFlags)) {
                mRoomState.setComplete();
                Dialog victoryDialog = mRoom.getVictory();
                mNextScreen = new VictoryScreen(mAssetLoader, mDisplay, mViewBounds, victoryDialog);
            }
        }
        mControls.intepretInteractions(milliTime, touchSpots);

        // TODO should be handled directly by hero
        RoomState.Direction move = mRoomState.getMovement();
        RoomState.Direction facing = mRoomState.getFacing();
        mHero.directionCommand(milliTime, move, facing);

        PointF heroPosition = mRoomState.getPosition();
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
        return mNextScreen;
    }

    @Override
    public boolean done() {
        return false;
    }

    @Override
    public void recycle() {
        mHero.recycle();
        mControls.recycle();
        mRoom.recycle();
    }

    private Screen mNextScreen;

    private final AssetLoader mAssetLoader;
    private final Bitmap mDisplay;
    private final Room mRoom;
    private final Set<String> mVictoryFlags;
    private final Canvas mCanvas;
    private final Rect mViewBounds; // Area of screen for us to draw on
    private final Rect mWorldBounds; // Area of the world to show on the screen
    private final GameCharacter mHero;
    private final UIControls mControls;
    private final RoomState mRoomState;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.WorldScreen";
}
