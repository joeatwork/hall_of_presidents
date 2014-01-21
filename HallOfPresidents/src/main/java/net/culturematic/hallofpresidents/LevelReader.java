package net.culturematic.hallofpresidents;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LevelReader {

    public static class LevelUnreadableException extends Exception {
        public LevelUnreadableException(String message) {
            super(message);
        }

        public LevelUnreadableException(Exception cause) {
            super(cause);
        }
    }

    public LevelReader(AssetLoader assetLoader) {
        mAssetLoader = assetLoader;
    }

    public Level readLevel(LevelCatalogItem item)
        throws LevelUnreadableException {
        JSONObject levelDesc = mAssetLoader.loadJSONObject(item.getPath() + "/level.json");
        try {
            final int version = levelDesc.getInt("version");
            if (version != 1) {
                throw new LevelUnreadableException("Can't read any version other than 1");
            }

            final JSONObject heroDesc = levelDesc.getJSONObject("hero");
            final JSONObject roomsDesc = levelDesc.getJSONObject("rooms");
            final JSONObject startDesc = levelDesc.getJSONObject("start");
            final JSONObject victoryDesc = levelDesc.getJSONObject("victory");

            final JSONObject victoryDialogDesc = victoryDesc.getJSONObject("dialog");
            final Dialog victory = readDialog(victoryDialogDesc);

            final GameCharacter hero = readCharacter(heroDesc, item.getPath());
            final Map<String, Room> rooms = new HashMap<String, Room>();
            final Iterator roomKeys = roomsDesc.keys();
            while (roomKeys.hasNext()) {
                String thisRoomName = (String) roomKeys.next();
                JSONObject thisRoomDesc = roomsDesc.getJSONObject(thisRoomName);
                Room room = readRoom(thisRoomName, thisRoomDesc, item.getPath());
                rooms.put(room.getName(), room);
            }

            final String startRoom = startDesc.getString("room");
            if (! rooms.containsKey(startRoom)) {
                throw new LevelUnreadableException("No room \"" + startRoom + "\" referenced in start");
            }
            final JSONObject startPositionDesc = startDesc.getJSONObject("position");
            final PointF startPosition = new PointF(
                 mAssetLoader.scaleInt(startPositionDesc.getInt("x")),
                 mAssetLoader.scaleInt(startPositionDesc.getInt("y"))
            );

            return new Level(hero, rooms, victory, startRoom, startPosition);
        } catch (JSONException e) {
            throw new LevelUnreadableException(e);
        }
    }

    private Dialog readDialog(JSONObject dialogDesc) throws JSONException {
        String facingName = dialogDesc.getString("facing");
        LevelState.Direction facing;
        if ("UP".equals(facingName)) {
            facing = LevelState.Direction.DIRECTION_UP;
        } else if ("DOWN".equals(facingName)) {
            facing = LevelState.Direction.DIRECTION_DOWN;
        } else if ("LEFT".equals(facingName)) {
            facing = LevelState.Direction.DIRECTION_LEFT;
        } else if ("RIGHT".equals(facingName)) {
            facing = LevelState.Direction.DIRECTION_RIGHT;
        } else {
            throw new RuntimeException(
                    "Can't understand facing " + facingName + " (should be 'UP', 'DOWN', 'LEFT' or 'RIGHT'"
            );
        }

        Set<String> flagsToSet = new HashSet<String>();
        if (dialogDesc.has("set_room_flags")) {
            JSONArray flagArray = dialogDesc.getJSONArray("set_room_flags");
            for (int i = 0; i < flagArray.length(); i++) {
                flagsToSet.add(flagArray.getString(i));
            }
        }

        Set<String> flagsToRequire = new HashSet<String>();
        if (dialogDesc.has("condition_room_flags")) {
            JSONArray flagArray = dialogDesc.getJSONArray("condition_room_flags");
            for (int i = 0; i < flagArray.length(); i++) {
                flagsToRequire.add(flagArray.getString(i));
            }
        }

        String dialogText = dialogDesc.getString("dialog");
        return new Dialog(dialogText, facing, flagsToSet, flagsToRequire);
    }

    private GameCharacter readCharacter(JSONObject characterDesc, String rootPath)
        throws JSONException, LevelUnreadableException {

        final JSONArray statesArray = characterDesc.getJSONArray("states");
        final JSONObject spriteCollectionDesc = characterDesc.getJSONObject("sprites");

        final Map<String, SpriteRenderer.Sprites> spritesByName = new HashMap<String, SpriteRenderer.Sprites>();
        final Iterator spriteNames = spriteCollectionDesc.keys();
        while (spriteNames.hasNext()) {
            final String spriteName = (String) spriteNames.next();
            final JSONObject spriteDesc = spriteCollectionDesc.getJSONObject(spriteName);
            final SpriteRenderer.Sprites sprites = readSprites(spriteDesc, rootPath);
            spritesByName.put(spriteName, sprites);
        }

        CharacterState characterState = new CharacterState();
        for (int stateIx = 0; stateIx < statesArray.length(); stateIx++) {
            final JSONObject stateDesc = statesArray.getJSONObject(stateIx);
            final String spriteName = stateDesc.getString("sprite");
            final SpriteRenderer.Sprites sprites = spritesByName.get(spriteName);
            if (null == sprites) {
                throw new LevelUnreadableException(
                        "Can't find sprites named \"" + spriteName + "\" " +
                                "named in character states:\n" + stateDesc.toString()
                );
            }
            final JSONArray flagsArray = stateDesc.getJSONArray("flags");
            final Set<String> flags = new HashSet<String>();
            for (int flagIx = 0; flagIx < flagsArray.length(); flagIx++) {
                flags.add(flagsArray.getString(flagIx));
            }
            characterState.addState(flags, sprites);
        }

        return new GameCharacter(characterState);
    }

    private SpriteRenderer.Sprites readSprites(JSONObject spriteDesc, String rootPath)
        throws JSONException {
        final SpriteRenderer.Sprites ret = new SpriteRenderer.Sprites();
        final String bitmapPath = spriteDesc.getString("source");
        ret.speedPxPerSecond = spriteDesc.getInt("speed_px_per_second");
        ret.standFramesPerSecond = spriteDesc.getInt("stand_frames_per_second");

        final JSONObject boundsDesc = spriteDesc.getJSONObject("bounds");
        ret.boundsWidth = mAssetLoader.scaleInt(boundsDesc.getInt("width"));
        ret.boundsHeight = mAssetLoader.scaleInt(boundsDesc.getInt("height"));

        ret.spriteBitmap = mAssetLoader.loadBitmap(rootPath + "/" + bitmapPath, null);
        if (spriteDesc.has("STAND_DOWN")) {
            ret.standDownFrames = readRects(spriteDesc.getJSONArray("STAND_DOWN"));
        }
        if (spriteDesc.has("STAND_UP")) {
            ret.standUpFrames = readRects(spriteDesc.getJSONArray("STAND_UP"));
        }
        if (spriteDesc.has("STAND_RIGHT")) {
            ret.standRightFrames = readRects(spriteDesc.getJSONArray("STAND_RIGHT"));
        }
        if (spriteDesc.has("STAND_LEFT")) {
            ret.standLeftFrames = readRects(spriteDesc.getJSONArray("STAND_LEFT"));
        }
        if (spriteDesc.has("MOVE_DOWN")) {
            ret.moveDownFrames = readRects(spriteDesc.getJSONArray("MOVE_DOWN"));
        }
        if (spriteDesc.has("MOVE_UP")) {
            ret.moveUpFrames = readRects(spriteDesc.getJSONArray("MOVE_UP"));
        }
        if (spriteDesc.has("MOVE_LEFT")) {
            ret.moveLeftFrames = readRects(spriteDesc.getJSONArray("MOVE_LEFT"));
        }
        if (spriteDesc.has("MOVE_RIGHT")) {
            ret.moveRightFrames = readRects(spriteDesc.getJSONArray("MOVE_RIGHT"));
        }

        return ret;
    }

    private Rect[] readRects(final JSONArray rectsArray)
        throws JSONException {
        final Rect[] ret = new Rect[rectsArray.length()];
        for (int i = 0; i < rectsArray.length(); i++) {
            final JSONObject rect = rectsArray.getJSONObject(i);
            int x = mAssetLoader.scaleInt(rect.getInt("x"));
            int y = mAssetLoader.scaleInt(rect.getInt("y"));
            int width = mAssetLoader.scaleInt(rect.getInt("width"));
            int height = mAssetLoader.scaleInt(rect.getInt("height"));
            ret[i] = new Rect(x, y, x + width, y + height);
        }
        return ret;
    }

    private Room readRoom(String name, JSONObject roomDesc, String rootPath)
        throws JSONException {
        final String backgroundPath = roomDesc.getString("background");
        final Bitmap background = mAssetLoader.loadBitmap(rootPath + "/" + backgroundPath, Bitmap.Config.RGB_565);

        final String furniturePath = roomDesc.getString("furniture");
        final Bitmap furniture = mAssetLoader.loadBitmap(rootPath + "/" + furniturePath, Bitmap.Config.ARGB_4444);

        final String terrainPath = roomDesc.getString("terrain");
        final Bitmap terrain = mAssetLoader.loadBitmap(rootPath + "/" + terrainPath, Bitmap.Config.ALPHA_8);

        final JSONArray eventsDescs = roomDesc.getJSONArray("events");
        final WorldEvent[] events = new WorldEvent[eventsDescs.length()];
        for (int i = 0; i < eventsDescs.length(); i++) {
            events[i] = readEvent(eventsDescs.getJSONObject(i));
        }

        return new Room(name, background, furniture, terrain, events);
    }

    private WorldEvent readEvent(JSONObject eventDescription)
            throws JSONException {
        String name = eventDescription.getString("name");
        JSONObject boundsObj = eventDescription.getJSONObject("bounds");
        final int unscaledTop = boundsObj.getInt("top");
        final int unscaledRight = boundsObj.getInt("right");
        final int unscaledLeft = boundsObj.getInt("left");
        final int unscaledBottom = boundsObj.getInt("bottom");
        final Rect bounds = new Rect(
                mAssetLoader.scaleInt(unscaledLeft),
                mAssetLoader.scaleInt(unscaledTop),
                mAssetLoader.scaleInt(unscaledRight),
                mAssetLoader.scaleInt(unscaledBottom)
        );

        Dialog dialog = null;
        if (eventDescription.has("dialog")) {
            JSONObject dialogDesc = eventDescription.getJSONObject("dialog");
            dialog = readDialog(dialogDesc);
        }

        return new WorldEvent(bounds, name, dialog);
    }

    final AssetLoader mAssetLoader;
}