package net.culturematic.hallofpresidents;

import android.graphics.PointF;

import java.util.Map;

public class Level {
    public Level(HeroCharacter hero, Map<String, Room> rooms, Dialog victory, String startRoom, PointF startPosition) {
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

    public HeroCharacter getHero() {
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

    private final HeroCharacter mHero;
    private final Map<String, Room> mRooms;
    private final Dialog mVictory;
    private final String mStartRoomName;
    private final PointF mStartPosition;
}
