package net.culturematic.hallofpresidents;

import android.graphics.PointF;

import java.util.Map;

public class Level {
    public Level(GameCharacter hero, Map<String, Room> rooms, Dialog victory, String startRoom, PointF startPosition) {
        mHero = hero;
        mRooms = rooms;
        mVictory = victory;
        mStartRoomName = startRoom;
        mStartPosition = startPosition;
    }

    public Room getRoom(String roomName) {
        Room ret = mRooms.get(roomName);
        if (null == ret) {
            throw new RuntimeException("No room named " + roomName + " in level");
        }
        return ret;
    }

    public GameCharacter getHero() {
        return mHero;
    }

    public String getStartRoomName() {
        return mStartRoomName;
    }

    public PointF getStartPosition() {
        return mStartPosition;
    }

    public Dialog getVictory() {
        return mVictory;
    }

    public void recycle() {
        mHero.recycle();
        for (Room room:mRooms.values()) {
            room.recycle();
        }
    }

    private final GameCharacter mHero;
    private final Map<String, Room> mRooms;
    private final Dialog mVictory;
    private final String mStartRoomName;
    private final PointF mStartPosition;
}
