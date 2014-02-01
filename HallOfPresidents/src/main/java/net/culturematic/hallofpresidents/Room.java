package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.Arrays;

public class Room {
    public Room(String name, Bitmap background, Bitmap furniture, Bitmap terrain, Figure[] figures, WorldEvent[] events) {
        mName = name;
        mBackground = background;
        mFurniture = furniture;
        mTerrain = terrain;
        mEvents = events;
        mFigures = figures;
        mVisionRect = new Rect();

        Arrays.sort(mFigures);
    }

    public String getName() { return mName; }

    public void update(long milliTime, LevelState levelState) {
        for (int i = 0; i < mFigures.length; i++) {
            final Figure character = mFigures[i];
            character.update(milliTime, levelState);

            // TODO this is bad. It's initializing things that shouldn't need to be initialized.
            character.directionCommand(
                    LevelState.Direction.DIRECTION_NONE,
                    LevelState.Direction.DIRECTION_DOWN,
                    this
            );
        }
    }

    /**
     *
     * @param canvas canvas on which the background should be drawn
     * @param worldRect what portion of the world is visible
     * @param viewport what portion of the canvas is dedicated to showing the world
     */
    public void drawBackground(Canvas canvas, Rect worldRect, Rect viewport) {
        canvas.drawBitmap(mBackground, worldRect, viewport, null);

        /*
         * For debugging- the code below will draw a rectangle for each event in the room.
         *
        Paint redPaint = new Paint();
        redPaint.setColor(Color.RED);
        for (int i = 0; i < mEvents.length; i++) {
            final Rect bounds = new Rect(mEvents[i].getBounds()); // Bounds in WORLD COORDS.
            bounds.offset(- worldRect.left, - worldRect.top);
            canvas.drawRect(bounds, redPaint);
        }
        *
        * END DEBUGGING
        */
    }

    public void drawCharacters(Canvas canvas, Rect worldRect, GameCharacter hero) {
        final PointF heroPosition = hero.getPosition();
        boolean heroDrawn = false;
        for (int i = 0; i < mFigures.length; i++) {
            final Figure character = mFigures[i];
            final PointF characterPosition = character.getPosition();
            if (characterPosition.y > heroPosition.y && !heroDrawn) {
                hero.drawCharacter(canvas, worldRect.left, worldRect.top);
                heroDrawn = true;
            }
            character.drawCharacter(canvas, worldRect.left, worldRect.top);
        }
        if (! heroDrawn) {
            hero.drawCharacter(canvas, worldRect.left, worldRect.top);
        }
    }

    public void drawFurniture(Canvas canvas, Rect worldRect, Rect viewport) {
        canvas.drawBitmap(mFurniture, worldRect, viewport, null);
    }

    public boolean inBounds(int x, int y) {
        if ((x < 0) || (y < 0) ||
            (x >= mTerrain.getWidth()) ||
            (y >= mTerrain.getHeight())) {
            return false;
        }

        int color = mTerrain.getPixel(x, y);
        if (0 == (color & 0xff000000)) {
            return false;
        }

        for (int i = 0; i < mFigures.length; i++) {
            final Rect rect = mFigures[i].getCollisionBounds();
            if (null != rect && rect.contains(x, y)) {
                return false;
            }
        }

        return true;
    }

    public WorldEvent.Door checkForDoor(HeroCharacter hero) {
        final PointF position = hero.getPosition();
        for (int i = 0; i < mEvents.length; i++) {
            final WorldEvent event = mEvents[i];
            final Rect rect = event.getBounds();
            if (rect.contains((int) position.x, (int) position.y)) {
                return event.getDoor();
            }
        }

        return null;
    }

    public void showActions(HeroCharacter hero, LevelState levelState) {
        final PointF position = hero.getPosition();
        for (int i = 0; i < mEvents.length; i++) {
            final WorldEvent event = mEvents[i];
            final Dialog dialog = event.getDialog();
            if (null != dialog) {
                final Rect rect = event.getBounds();
                if (rect.contains((int) position.x, (int) position.y)) {
                    // TODO this position ain't right.
                    levelState.setDialogAvailable(dialog, rect.centerX(), rect.top);
                }
            }
        }

        final Rect heroBounds = hero.getImageBounds();
        int heroWidth = heroBounds.width();
        int heroHeight = heroBounds.height();
        mVisionRect.set(
            heroBounds.left - heroWidth,
            heroBounds.top - heroHeight,
            heroBounds.right + heroWidth,
            heroBounds.bottom + heroHeight
        );

        for (int i = 0; i < mFigures.length; i++) {
            final Figure character = mFigures[i];
            final Dialog dialog = character.getDialog();
            if (null != dialog) {
                final Rect characterBounds = character.getImageBounds();
                if (null != character.getImageBounds()) {
                    if (Rect.intersects(mVisionRect, characterBounds)) {
                        int top = characterBounds.top + character.getDialogOffsetY();
                        levelState.setDialogAvailable(dialog, characterBounds.centerX(), top);
                    }
                }
            }
        }
    }

    private final String mName;
    private final Bitmap mBackground;
    private final Bitmap mFurniture;
    private final Bitmap mTerrain;
    private final WorldEvent[] mEvents;
    private final Figure[] mFigures;
    private final Rect mVisionRect;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.Room";
}
