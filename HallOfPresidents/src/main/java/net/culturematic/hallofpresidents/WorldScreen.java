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
            UIControls controls,
            Analytics analytics) {
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
        mHelpMessage = null;
        mFirstSawDialog = -1;
        mAnalytics = analytics;

        analytics.trackLevelOpened(mLevelState.getLevelCatalogItem().getName());
    }

    @Override
    public String getHelpMessage() {
        return mHelpMessage;
    }

    @Override
    public void update(long milliTime, InputEvents.TouchSpot[] touchSpots) {

        if (mLevelState.canGetVictory()) {
            final Set<String> hasFlags = mLevelState.getLevelFlags();
            if (hasFlags.containsAll(mVictoryFlags)) {
                mLevelState.setComplete();
                mLevelState.clearLevelFlags();
                mLevelState.setRoomName(mLevel.getStartRoomName());
                mLevelState.setPosition(mLevel.getStartPosition());
                final Dialog victoryDialog = mLevel.getVictory();
                mNextScreen = new VictoryScreen(mLevelState, mAssetLoader, mDisplay, mViewBounds, victoryDialog, mAnalytics);
            }
        }

        if (mFirstSawDialog < 0 && mLevelState.dialogHasBeenAvailable()) {
            mFirstSawDialog = milliTime;
        }

        if (mLevelState.dialogHasBeenAvailable() &&
                (! mLevelState.dialogHasBeenShown()) &&
                (milliTime - mFirstSawDialog > Config.MILLIS_TILL_POPUP)) {
            mHelpMessage = "Tap the \"!\" icon to investigate!";
        } else {
            mHelpMessage = null;
        }

        mControls.intepretInteractions(touchSpots, mWorldBounds);
        mLevelState.resetActions();

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

        room.showActions(mHero, mLevelState);

        // We want the hero ever so slightly below the center of the viewport
        final PointF heroPosition = mLevelState.getPosition();
        int worldOffsetX = (int) heroPosition.x - mViewBounds.centerX();
        int worldOffsetY = (int) heroPosition.y - mViewBounds.centerY() - Config.HERO_OFFSET_BELOW_CENTER_PX;

        mWorldBounds.offsetTo(worldOffsetX, worldOffsetY);

        mCanvas.drawColor(Color.BLACK);
        room.drawBackground(mCanvas, mWorldBounds, mViewBounds);
        room.drawCharacters(mCanvas, mWorldBounds, mHero);

        room.drawFurniture(mCanvas, mWorldBounds, mViewBounds);
        mControls.drawControls(mCanvas, mWorldBounds, mViewBounds);

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
    private long mFirstSawDialog;
    private String mHelpMessage;

    private final Analytics mAnalytics;
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
