package net.culturematic.hallofpresidents;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LevelState {

    public enum Direction {
        DIRECTION_NONE,
        DIRECTION_UP,
        DIRECTION_RIGHT,
        DIRECTION_DOWN,
        DIRECTION_LEFT
    } // Direction

    public LevelState(LevelCatalogItem item) {
        mRoomPosition = null;
        mRoomName = null;
        mLevelCatalogItem = item;
        mLevelFlags = new HashSet<String>();
        mControlState = ControlState.WALKING;
        mMovement = Direction.DIRECTION_NONE;
        mFacing = Direction.DIRECTION_DOWN;
        mIsComplete = false;
    }

    public static LevelState readJSON(JSONObject stateDesc)
        throws JSONException {
        final String roomName = stateDesc.getString("room_name");
        final JSONObject positionDesc = stateDesc.getJSONObject("room_position");
        final PointF position = new PointF(
            (float) positionDesc.getDouble("x"),
            (float) positionDesc.getDouble("y")
        );

        final JSONObject itemDesc = stateDesc.getJSONObject("room_item");
        final LevelCatalogItem item = LevelCatalogItem.readJSON(itemDesc);
        final LevelState ret = new LevelState(item);
        ret.setPosition(position);
        ret.setRoomName(roomName);

        final JSONArray flagsArray = stateDesc.getJSONArray("room_flags_acquired");
        final Set<String> flagsSet = new HashSet<String>();
        for (int i = 0; i < flagsArray.length(); i++) {
            flagsSet.add(flagsArray.getString(i));
        }
        ret.addLevelFlags(flagsSet);

        if (stateDesc.getBoolean("is_complete")) {
            ret.setComplete();
        }
        return ret;
    }

    public JSONObject toJSON() {
        try {
            final JSONObject ret = new JSONObject();
            ret.put("is_complete", mIsComplete);
            ret.put("room_item", mLevelCatalogItem.toJSON());
            ret.put("room_name", mRoomName);

            final JSONObject positionObj = new JSONObject();
            positionObj.put("x", mRoomPosition.x);
            positionObj.put("y", mRoomPosition.y);
            ret.put("room_position", positionObj);

            final JSONArray flagsArray = new JSONArray(mLevelFlags);
            ret.put("room_flags_acquired", flagsArray);

          return ret;
        } catch (JSONException e) {
            throw new RuntimeException("Can't serialize LevelState to JSON", e);
        }
    }

    public void setComplete() {
        mIsComplete = true;
    }

    public boolean isComplete() {
        return mIsComplete;
    }

    public void setPosition(PointF position) {
        if (null == mRoomPosition) {
            mRoomPosition = new PointF();
        }
        mRoomPosition.set(position);
    }

    public void setRoomName(String roomName) {
        mRoomName = roomName;
    }

    public void setDialogAvailable(Dialog dialog) {
        if (null == dialog) {
            mControlState = mControlState.onNoDialogAvailable();
        } else {
            mControlState = mControlState.onDialogAvailable();
        }
        mDialogAvailable = dialog;
    }

    public boolean canGetVictory() {
        return mControlState.canGetVictory();
    }

    public String getAButtonLabel() {
        return mControlState.getAButtonLabel();
    }

    public String getBButtonLabel() {
        return mControlState.getBButtonLabel();
    }

    public void pressAButton() {
        mControlState = mControlState.onAButton();
    }

    public void pressBButton() {
        mControlState = mControlState.onBButton();
    }

    public String getDialogText() {
        if (ControlState.DIALOG_IN_PROGRESS == mControlState) {
            if (null != mDialogAvailable) {
                return mDialogAvailable.getDialog();
            }
        }
        return null;
    }

    public void showedDialog() {
        assert null != mDialogAvailable;
        addLevelFlags(mDialogAvailable.getLevelFlagsToSet());
        mFacing = mDialogAvailable.getFacing();
    }

    public void requestMovement(Direction direction) {
        mMovement = mControlState.movement(direction);
        if (mMovement != Direction.DIRECTION_NONE) {
            mFacing = mMovement;
        }
    }

    public Direction getMovement() {
        return mMovement;
    }

    public Direction getFacing() {
        return mFacing;
    }

    public LevelCatalogItem getLevelCatalogItem() {
        return mLevelCatalogItem;
    }

    public void addLevelFlags(Collection<String> newFlags) {
        mLevelFlags.addAll(newFlags);
    }

    public void clearLevelFlags() {
        mLevelFlags.clear();
    }

    public String getRoomName() {
        return mRoomName;
    }

    public Set<String> getLevelFlags() {
        return mLevelFlags;
    }

    public PointF getPosition() {
        return mRoomPosition;
    }

    public enum ControlState {
        WALKING (null, null) {
            @Override
            public ControlState onDialogAvailable() {
                return DIALOG_AVAILABLE;
            }
        },
        DIALOG_AVAILABLE ("Talk", null) {
            @Override
            public ControlState onAButton() {
                return DIALOG_IN_PROGRESS;
            }
            @Override
            public ControlState onNoDialogAvailable() {
                return WALKING;
            }
        },
        DIALOG_IN_PROGRESS ("Ok", "Cancel") {
            @Override
            public ControlState onAButton() {
                return WALKING;
            }
            @Override
            public ControlState onBButton() {
                return WALKING;
            }
            @Override
            public Direction movement(Direction request) {
                return Direction.DIRECTION_NONE;
            }
            @Override
            public boolean canGetVictory() {
                return false;
            }
        };

        ControlState(String aButtonLabel, String bButtonLabel) {
            mAButtonLabel = aButtonLabel;
            mBButtonLabel = bButtonLabel;
        }

        public String getAButtonLabel() {
            return mAButtonLabel;
        }

        public String getBButtonLabel() {
            return mBButtonLabel;
        }

        public ControlState onAButton() {
            return this;
        }

        public ControlState onBButton() {
            return this;
        }

        public ControlState onDialogAvailable() {
            return this;
        }

        public ControlState onNoDialogAvailable() {
            return this;
        }

        public Direction movement(Direction request) {
            return request;
        }

        public boolean canGetVictory() {
            return true;
        }

        private final String mAButtonLabel;
        private final String mBButtonLabel;
    }

    private boolean mIsComplete;
    private Direction mMovement;
    private Direction mFacing;
    private Dialog mDialogAvailable;
    private LevelCatalogItem mLevelCatalogItem;
    private PointF mRoomPosition;
    private String mRoomName;
    private ControlState mControlState;
    private final Set<String> mLevelFlags;

    @SuppressWarnings("unused")
    private static final String LOGTAG = "hallofpresidents.LevelState";
}
