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

            final CharacterState heroState = readCharacter(heroDesc, item.getPath());
            final HeroCharacter hero = new HeroCharacter(heroState);
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

    private CharacterState readCharacter(JSONObject characterDesc, String rootPath)
        throws JSONException, LevelUnreadableException {

        final JSONArray statesArray = characterDesc.getJSONArray("states");
        final JSONObject spriteCollectionDesc = characterDesc.getJSONObject("sprites");

        JSONObject dialogCollectionDesc = new JSONObject();
        if (characterDesc.has("dialog")) {
            dialogCollectionDesc = characterDesc.getJSONObject("dialog");
        }

        final Map<String, Sprites> spritesByName = new HashMap<String, Sprites>();
        final Iterator spriteNames = spriteCollectionDesc.keys();
        while (spriteNames.hasNext()) {
            final String spriteName = (String) spriteNames.next();
            final JSONObject spriteDesc = spriteCollectionDesc.getJSONObject(spriteName);
            final Sprites sprites = readSprites(spriteDesc, rootPath);
            spritesByName.put(spriteName, sprites);
        }

        final Map<String, Dialog> dialogByName = new HashMap<String, Dialog>();
        final Iterator dialogNames = dialogCollectionDesc.keys();
        while (dialogNames.hasNext()) {
            final String dialogName = (String) dialogNames.next();
            final JSONObject dialogDesc = dialogCollectionDesc.getJSONObject(dialogName);
            final Dialog dialog = readDialog(dialogDesc);
            dialogByName.put(dialogName, dialog);
        }

        CharacterState characterState = new CharacterState();

        // This is a HACK to deal with giant characters who are taller
        // then the edge of the screen.
        if (characterDesc.has("dialogOffset")) {
            int dialogOffset = mAssetLoader.scaleInt(characterDesc.getInt("dialogOffset"));
            characterState.setDialogOffset(dialogOffset);
        }

        for (int stateIx = 0; stateIx < statesArray.length(); stateIx++) {
            final JSONObject stateDesc = statesArray.getJSONObject(stateIx);

            final JSONArray flagsArray = stateDesc.getJSONArray("flags");
            final Set<String> flags = new HashSet<String>();
            for (int flagIx = 0; flagIx < flagsArray.length(); flagIx++) {
                flags.add(flagsArray.getString(flagIx));
            }

            Sprites sprites = null;
            if (stateDesc.has("sprite")) {
                final String spriteName = stateDesc.getString("sprite");
                sprites = spritesByName.get(spriteName);
                if (null == sprites) {
                    throw new LevelUnreadableException(
                            "Can't find sprites named \"" + spriteName + "\" " +
                                    "named in character states:\n" + stateDesc.toString()
                    );
                }
            }

            Dialog dialog = null;
            if (stateDesc.has("dialog")) {
                final String dialogName = stateDesc.getString("dialog");
                dialog = dialogByName.get(dialogName);
                if (null == dialog) {
                    throw new LevelUnreadableException(
                            "Can't find dialog named \"" + dialogName + "\" " +
                                    "named in character states:\n" + stateDesc.toString()
                    );
                }
            }

            characterState.addState(flags, sprites, dialog);
        }

        return characterState;
    }

    private Sprites readSprites(JSONObject spriteDesc, String rootPath)
        throws JSONException {
        final Sprites ret = new Sprites();
        final String bitmapPath = spriteDesc.getString("source");
        ret.speedPxPerSecond = spriteDesc.getInt("speed_px_per_second");
        ret.standFramesPerSecond = spriteDesc.getInt("stand_frames_per_second");

        if (spriteDesc.has("step_size")) {
            final JSONObject boundsDesc = spriteDesc.getJSONObject("step_size");
            ret.stepWidth = mAssetLoader.scaleInt(boundsDesc.getInt("width"));
            ret.stepHeight = mAssetLoader.scaleInt(boundsDesc.getInt("height"));
        } else {
            ret.stepWidth = -1;
            ret.stepHeight = -1;
        }

        ret.spriteBitmap = mAssetLoader.loadBitmap(rootPath + "/" + bitmapPath, null);
        if (spriteDesc.has("STAND_DOWN")) {
            ret.standDownFrames = readFrames(spriteDesc.getJSONArray("STAND_DOWN"));
        }
        if (spriteDesc.has("STAND_UP")) {
            ret.standUpFrames = readFrames(spriteDesc.getJSONArray("STAND_UP"));
        }
        if (spriteDesc.has("STAND_RIGHT")) {
            ret.standRightFrames = readFrames(spriteDesc.getJSONArray("STAND_RIGHT"));
        }
        if (spriteDesc.has("STAND_LEFT")) {
            ret.standLeftFrames = readFrames(spriteDesc.getJSONArray("STAND_LEFT"));
        }
        if (spriteDesc.has("MOVE_DOWN")) {
            ret.moveDownFrames = readFrames(spriteDesc.getJSONArray("MOVE_DOWN"));
        }
        if (spriteDesc.has("MOVE_UP")) {
            ret.moveUpFrames = readFrames(spriteDesc.getJSONArray("MOVE_UP"));
        }
        if (spriteDesc.has("MOVE_LEFT")) {
            ret.moveLeftFrames = readFrames(spriteDesc.getJSONArray("MOVE_LEFT"));
        }
        if (spriteDesc.has("MOVE_RIGHT")) {
            ret.moveRightFrames = readFrames(spriteDesc.getJSONArray("MOVE_RIGHT"));
        }

        return ret;
    }

    private Sprites.FrameInfo[] readFrames(final JSONArray rectsArray)
        throws JSONException {
        final Sprites.FrameInfo[] ret = new Sprites.FrameInfo[rectsArray.length()];
        for (int i = 0; i < rectsArray.length(); i++) {
            final JSONObject rectDesc = rectsArray.getJSONObject(i);
            final int x = mAssetLoader.scaleInt(rectDesc.getInt("x"));
            final int y = mAssetLoader.scaleInt(rectDesc.getInt("y"));
            final int width = mAssetLoader.scaleInt(rectDesc.getInt("width"));
            final int height = mAssetLoader.scaleInt(rectDesc.getInt("height"));
            final Sprites.FrameInfo info = new Sprites.FrameInfo();
            info.frame = new Rect(x, y, x + width, y + height);
            if (rectDesc.has("collision")) {
                final JSONObject collisionDesc = rectDesc.getJSONObject("collision");
                final int left = mAssetLoader.scaleInt(collisionDesc.getInt("x"));
                final int top = mAssetLoader.scaleInt(collisionDesc.getInt("y"));
                final int right = left + mAssetLoader.scaleInt(collisionDesc.getInt("width"));
                final int bottom = top + mAssetLoader.scaleInt(collisionDesc.getInt("height"));
                info.collision = new Rect(left, top, right, bottom);
            } else {
                int top = height - (height / 3);
                info.collision = new Rect(0, top, width, height);
            }
            ret[i] = info;
        }
        return ret;
    }

    private Room readRoom(String name, JSONObject roomDesc, String rootPath)
        throws JSONException, LevelUnreadableException {
        final String backgroundPath = roomDesc.getString("background");
        final Bitmap background = mAssetLoader.loadBitmap(rootPath + "/" + backgroundPath, Bitmap.Config.RGB_565);

        Bitmap furniture = null;
        if (roomDesc.has("furniture")) {
            final String furniturePath = roomDesc.getString("furniture");
            furniture = mAssetLoader.loadBitmap(rootPath + "/" + furniturePath, Bitmap.Config.RGB_565);
        }

        final String terrainPath = roomDesc.getString("terrain");
        final Bitmap terrain = mAssetLoader.loadBitmap(rootPath + "/" + terrainPath, Bitmap.Config.ALPHA_8);

        final JSONArray eventsDescs = roomDesc.getJSONArray("events");
        final WorldEvent[] events = new WorldEvent[eventsDescs.length()];
        for (int i = 0; i < eventsDescs.length(); i++) {
            events[i] = readEvent(eventsDescs.getJSONObject(i));
        }

        final JSONArray charactersArray = roomDesc.getJSONArray("characters");
        final JSONArray stumpsArray = roomDesc.getJSONArray("stumps");

        final int figuresLength = charactersArray.length() + stumpsArray.length();
        final Figure[] figures = new Figure[figuresLength];
        for (int i = 0; i < charactersArray.length(); i++) {
            final JSONObject nextCharacterDesc = charactersArray.getJSONObject(i);
            final JSONObject positionDesc = nextCharacterDesc.getJSONObject("position");
            final PointF position = new PointF(
                    mAssetLoader.scaleInt(positionDesc.getInt("x")),
                    mAssetLoader.scaleInt(positionDesc.getInt("y"))
            );
            final CharacterState characterState = readCharacter(nextCharacterDesc, rootPath);
            figures[i] = new GameCharacter(characterState, position);
        }

        final int stumpsOffset = charactersArray.length();
        for (int i = 0; i < stumpsArray.length(); i++) {
            final JSONObject stumpDesc = stumpsArray.getJSONObject(i);
            figures[i + stumpsOffset] = readStump(stumpDesc, rootPath);
        }

        return new Room(name, background, furniture, terrain, figures, events);
    }

    private Stump readStump(JSONObject stumpDesc, String rootPath)
        throws JSONException {
        final String sourcePath = stumpDesc.getString("source");
        final Bitmap source = mAssetLoader.loadBitmap(rootPath + "/" + sourcePath, Bitmap.Config.RGB_565);
        final JSONObject positionDesc = stumpDesc.getJSONObject("position");
        final PointF position = new PointF(
            mAssetLoader.scaleInt(positionDesc.getInt("x")),
            mAssetLoader.scaleInt(positionDesc.getInt("y"))
        );
        final JSONObject drawRegionDesc = stumpDesc.getJSONObject("draw_region");

        final int drawLeft = drawRegionDesc.getInt("x");
        final int drawTop = drawRegionDesc.getInt("y");
        final int drawRight = drawLeft + drawRegionDesc.getInt("width");
        final int drawBottom = drawTop + drawRegionDesc.getInt("height");
        final Rect drawRegion = new Rect(
            mAssetLoader.scaleInt(drawLeft),
            mAssetLoader.scaleInt(drawTop),
            mAssetLoader.scaleInt(drawRight),
            mAssetLoader.scaleInt(drawBottom)
        );

        final JSONObject collideRegionDesc = stumpDesc.getJSONObject("collide_region");
        final int collideLeft = collideRegionDesc.getInt("x");
        final int collideTop = collideRegionDesc.getInt("y");
        final int collideRight = collideLeft + collideRegionDesc.getInt("width");
        final int collideBottom = collideTop + collideRegionDesc.getInt("height");
        final Rect collideRegion = new Rect(
            mAssetLoader.scaleInt(collideLeft),
            mAssetLoader.scaleInt(collideTop),
            mAssetLoader.scaleInt(collideRight),
            mAssetLoader.scaleInt(collideBottom)
        );

        return new Stump(source, position, drawRegion, collideRegion);
    }

    private WorldEvent readEvent(JSONObject eventDescription)
            throws JSONException {
        String name = eventDescription.getString("name");
        JSONObject boundsObj = eventDescription.getJSONObject("bounds");
        final int unscaledLeft = boundsObj.getInt("x");
        final int unscaledTop = boundsObj.getInt("y");
        final int unscaledWidth = boundsObj.getInt("width");
        final int unscaledHeight = boundsObj.getInt("height");
        final Rect bounds = new Rect(
                mAssetLoader.scaleInt(unscaledLeft),
                mAssetLoader.scaleInt(unscaledTop),
                mAssetLoader.scaleInt(unscaledLeft + unscaledWidth),
                mAssetLoader.scaleInt(unscaledTop + unscaledHeight)
        );

        Dialog dialog = null;
        WorldEvent.Door door = null;
        if (eventDescription.has("dialog")) {
            JSONObject dialogDesc = eventDescription.getJSONObject("dialog");
            dialog = readDialog(dialogDesc);
        }
        if (eventDescription.has("door")) {
            JSONObject doorDesc = eventDescription.getJSONObject("door");
            JSONObject destPositionDesc = doorDesc.getJSONObject("dest_position");
            PointF destPosition = new PointF(
                mAssetLoader.scaleInt(destPositionDesc.getInt("x")),
                mAssetLoader.scaleInt(destPositionDesc.getInt("y"))
            );
            String destRoomName = doorDesc.getString("dest_room");
            door = new WorldEvent.Door(destRoomName, destPosition);
        }

        return new WorldEvent(bounds, name, dialog, door);
    }

    final AssetLoader mAssetLoader;
}
