package net.culturematic.hallofpresidents;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

public class AssetLoader {
    public AssetLoader(Context context) {
        mContext = context;
        mDisplayDensity = context.getResources().getDisplayMetrics().densityDpi;
        mCachedDialogBackground = null;
        mCachedTextPaint = null;
    }

    public int scaleInt(int original) {
        long scaleUp = (long) original * mDisplayDensity;
        long scaleDown = scaleUp / DisplayMetrics.DENSITY_XXHIGH;
        return (int) scaleDown;
    }

    public RoomState loadSavedRoomState(RoomCatalogItem item) {
        final SharedPreferences prefs = mContext.getSharedPreferences(ROOM_STATE_PREFS_NAME, Context.MODE_PRIVATE);
        final String prefsString = prefs.getString(item.getFullPath(), null);
        RoomState ret = null;

        if (null != prefsString) {
            try {
                final JSONObject prefsJson = new JSONObject(prefsString);
                ret =  RoomState.readJSON(prefsJson);
            } catch (JSONException e) {
                Log.e(LOGTAG, "(Apparently) Corrupted room preferences found.", e);
            }
        }

        if (null == ret) {
            ret = new RoomState(item);
            return ret;
        }

        return ret;
    }

    public void saveRoomState(RoomState roomState) {
        final SharedPreferences prefs = mContext.getSharedPreferences(ROOM_STATE_PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        final String storagePath = roomState.getRoomCatalogItem().getFullPath();
        final JSONObject storagePayload = roomState.toJSON();
        editor.putString(storagePath, storagePayload.toString());
        editor.commit();
    }

    public Drawable loadLoadingScreen() {
        return mContext.getResources().getDrawable(R.drawable.mug_of_adventure);
    }

    public TextPaint loadDialogTextPaint() {
        if (null == mCachedTextPaint) {
            float fontSize = mContext.getResources().getDisplayMetrics().scaledDensity * DIALOG_FONT_SIZE_SP;
            Typeface dialogFace = Typeface.createFromAsset(mContext.getAssets(), TYPEFACE_ASSET_PATH);
            mCachedTextPaint = new TextPaint();
            mCachedTextPaint.setTypeface(dialogFace);
            mCachedTextPaint.setColor(Color.BLACK);
            mCachedTextPaint.setTextSize(fontSize);
        }
        return mCachedTextPaint;
    }

    public TextPaint loadButtonTextPaint() {
        return loadDialogTextPaint();
    }

    public Bitmap loadHeroSpritesBitmap() {
        return loadBitmap(HERO_SPRITES_ASSET_PATH, null);
    }

    public Bitmap loadDpadBitmap() {
        return loadBitmap(DPAD_ASSET_PATH, null);
    }

    public Bitmap loadButtonBitmap() {
        return loadBitmap(BUTTON_ASSET_PATH, null);
    }

    public float getButtonPadding() {
        return (float) scaleInt(BUTTON_PADDING_HACK);
    }

    public Drawable loadDialogBackground() {
        if (null == mCachedDialogBackground) {
            mCachedDialogBackground = mContext.getResources().getDrawable(R.drawable.dialogbox);
        }
        return mCachedDialogBackground;
    }

    public JSONObject loadJSONObject(String path) {
        InputStream in = null;

        try {
            in = mContext.getAssets().open(path);
            final String objText = slurp(in);
            return new JSONObject(objText);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read JSON Object at asset path " + path, e);
        } catch (JSONException e) {
            throw new RuntimeException("Couldn't parse JSON object at path " + path, e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                Log.e(LOGTAG, "Couldn't close asset stream at " + path, e);
            }
        }
    }

    public Bitmap loadBitmap(String path, Bitmap.Config preferredConfig) {
        InputStream in = null;
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

        bitmapOptions.inDensity = DisplayMetrics.DENSITY_XXHIGH;
        bitmapOptions.inTargetDensity = mDisplayDensity;
        if (null != preferredConfig) {
            bitmapOptions.inPreferredConfig = preferredConfig;
        }

        try {
            in = mContext.getAssets().open(path);
            return BitmapFactory.decodeStream(in, null, bitmapOptions);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read Bitmap at asset path " + path, e);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(LOGTAG, "Can't close asset: " + path, e);
                }
            }
        }
    }

    private String slurp(InputStream in)
            throws IOException {
        StringBuilder retBuffer = new StringBuilder();
        byte[] bytes = new byte[4096];
        while (true) {
            int n = in.read(bytes);
            if (-1 == n) break;
            retBuffer.append(new String(bytes, 0, n));
        }
        return retBuffer.toString();
    }

    // We should only LruCache bitmaps and such, but we know
    // 1) these are used in multiple spots and 2) are kinda small.
    // Might wanna cache the UIControls stuff, too.
    private TextPaint mCachedTextPaint;
    private Drawable mCachedDialogBackground;

    private final Context mContext;
    private final int mDisplayDensity;

    private static final String ROOM_STATE_PREFS_NAME = "RoomStates";
    private static final String TYPEFACE_ASSET_PATH = "pressstart2p.ttf";
    private static final String HERO_SPRITES_ASSET_PATH = "hero_sprites_128x128.png";
    private static final String DPAD_ASSET_PATH = "widget_dpad.png";
    private static final String BUTTON_ASSET_PATH = "widget_button.png";

    private static final float DIALOG_FONT_SIZE_SP = 14f;
    private static final int BUTTON_PADDING_HACK = 64; // Until we clean up the buttons

    private static final String LOGTAG = "hallofpresidents.AssetLoader";
}
