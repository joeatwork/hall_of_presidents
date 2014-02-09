package net.culturematic.hallofpresidents;

import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

public class Analytics {

    public Analytics(MixpanelAPI mixpanel) {
        mMixpanel = mixpanel;
    }

    public void trackLevelLoading(String levelName) {
        try {
            final JSONObject props = new JSONObject();
            props.put("Level Name", levelName);
            mMixpanel.track("Loading Level", props);
        } catch (JSONException e) {
            Log.e(LOGTAG, "Impossible tracking error", e);
        }
    }

    public void trackLevelOpened(String levelName) {
        try {
            final JSONObject props = new JSONObject();
            props.put("Level Name", levelName);
            mMixpanel.track("Level Opened", props);
            mMixpanel.getPeople().setOnce("Level Opened " + levelName, true);
        } catch (JSONException e) {
            Log.e(LOGTAG, "Impossible tracking error", e);
        }
    }

    public void trackLevelVictory(String levelName) {
        try {
            final JSONObject props = new JSONObject();
            props.put("Level Name", levelName);
            mMixpanel.track("Level Victory", props);
            mMixpanel.getPeople().setOnce("Level Victory " + levelName, true);
        } catch (JSONException e) {
            Log.e(LOGTAG, "Impossible tracking error", e);
        }
    }

    private final MixpanelAPI mMixpanel;
    private static final String LOGTAG = "HallOfPresidents Analytics";
}
