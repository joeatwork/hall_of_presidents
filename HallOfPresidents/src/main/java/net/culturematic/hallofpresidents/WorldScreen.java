package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.Set;

public class WorldScreen implements Screen {

    public WorldScreen(
            AssetLoader assetLoader,
            Bitmap display,
            Rect viewBounds,
            Level level,
            LevelState savedState,
            HeroCharacter hero,
            UIControls controls) {
        mAssetLoader = assetLoader;
        mLevel = level;
        mLevelState = savedState;
        mNextScreen = null;
        mViewBounds = viewBounds;
        mWorldBounds = new Rect(viewBounds);
        mDisplay = display;
        mCanvas = new Canvas(display);
        mVictoryFlags = mLevel.getVictory().getLevelFlagsToRequire();
        mHero = hero;
        mControls = controls;
    }

    @Override
    public void update(long milliTime, InputEvents.TouchSpot[] touchSpots) {

        if (mLevelState.canGetVictory()) {
            final Set<String> hasFlags = mLevelState.getLevelFlags();
            if (hasFlags.containsAll(mVictoryFlags)) {
                mLevelState.setComplete();
                mLevelState.clearLevelFlags();
                final Dialog victoryDialog = mLevel.getVictory();
                mNextScreen = new VictoryScreen(mAssetLoader, mDisplay, mViewBounds, victoryDialog);
            }
        }
        mControls.intepretInteractions(touchSpots);

        final Room room = mLevel.getRoom(mLevelState.getRoomName());
        mHero.update(milliTime, mLevelState);
        room.update(milliTime, mLevelState);

        LevelState.Direction move = mLevelState.getMovement();
        LevelState.Direction facing = mLevelState.getFacing();
        mHero.directionCommand(move, facing, room);

        final WorldEvent.Door door = room.checkForDoor(mHero);

        if (null != door) {
            mLevelState.setRoomName(door.getDestRoomName());
            mLevelState.setPosition(door.getDestPosition());
            return;
        }
        // ELSE IF NO DOOR

        final Dialog dialog = room.checkForDialog(mHero);
        if (null != dialog) {
            mLevelState.setDialogAvailable(dialog);
        } else {
            mLevelState.setDialogAvailable(null);
        }

        // We want the hero at the center of the Viewport
        final PointF heroPosition = mLevelState.getPosition();
        int worldOffsetX = (int) heroPosition.x - mViewBounds.centerX();
        int worldOffsetY = (int) heroPosition.y - mViewBounds.centerY();

        mWorldBounds.offsetTo(worldOffsetX, worldOffsetY);

        mCanvas.drawColor(Color.BLACK);
        room.drawBackground(mCanvas, mWorldBounds, mViewBounds);
        room.drawCharacters(mCanvas, mWorldBounds, mHero);

        room.drawFurniture(mCanvas, mWorldBounds, mViewBounds);
        mControls.drawControls(mCanvas, mViewBounds);

        mLevelState.setPosition(heroPosition);
    }

    @Override
    public Screen nextScreen() {
        return mNextScreen;
    }

    @Override
    public boolean done() {
        return false;
    }

    private Screen mNextScreen;

    private final AssetLoader mAssetLoader;
    private final Bitmap mDisplay;
    private final Set<String> mVictoryFlags;
    private final Canvas mCanvas;
    private final Rect mViewBounds; // Area of screen for us to draw on
    private final Rect mWorldBounds; // Area of the world to show on the screen
    private final HeroCharacter mHero;
    private final UIControls mControls;
    private final Level mLevel;
    private final LevelState mLevelState;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.WorldScreen";
}
