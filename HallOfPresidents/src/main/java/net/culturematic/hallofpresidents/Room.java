package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.Arrays;

public class Room {
    public Room(String name, Bitmap background, Bitmap furniture, Bitmap terrain, GameCharacter[] characters, WorldEvent[] events) {
        mName = name;
        mBackground = background;
        mFurniture = furniture;
        mTerrain = terrain;
        mEvents = events;
        mCharacters = characters;
        mCurrentTimeMillis = -1;

        Arrays.sort(mCharacters);
    }

    public String getName() { return mName; }

    public void update(long milliTime) {
        mCurrentTimeMillis = milliTime;

        for (int i = 0; i < mCharacters.length; i++) {
            mCharacters[i].directionCommand(
                    mCurrentTimeMillis,
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

    // TODO: shouldn't take a milliTime arg here, should have an update method on the room.
    public void drawCharacters(Canvas canvas, Rect worldRect, GameCharacter hero) {
        final PointF heroPosition = hero.getPosition();
        boolean heroDrawn = false;
        for (int i = 0; i < mCharacters.length; i++) {
            // TODO: since you know the viewport, you should only draw characters that intersect it
            final GameCharacter character = mCharacters[i];
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
        return true;
    }

    public WorldEvent checkForEvent(PointF position) {
       for (int i = 0; i < mEvents.length; i++) {
           final WorldEvent event = mEvents[i];
           final Rect rect = event.getBounds();
           if (rect.contains((int) position.x, (int) position.y)) {
               return event;
           }
        }

        return null;
    }

    public void recycle() {
        mBackground.recycle();
        mFurniture.recycle();
        mTerrain.recycle();
    }

    private long mCurrentTimeMillis;

    private final String mName;
    private final Bitmap mBackground;
    private final Bitmap mFurniture;
    private final Bitmap mTerrain;
    private final WorldEvent[] mEvents;
    private final GameCharacter[] mCharacters;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.Room";
}
