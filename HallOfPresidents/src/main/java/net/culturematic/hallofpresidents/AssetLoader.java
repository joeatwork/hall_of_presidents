package net.culturematic.hallofpresidents;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class AssetLoader {
    public AssetLoader(Context context) {
        mAssetManager = context.getAssets();
        mResources = context.getResources();
        mDisplayDensity = context.getResources().getDisplayMetrics().densityDpi;
    }

    public int scaleInt(int original) {
        long scaleUp = (long) original * mDisplayDensity;
        long scaleDown = scaleUp / DisplayMetrics.DENSITY_XXHIGH;
        return (int) scaleDown;
    }

    public Drawable loadLoadingScreen() {
        return mResources.getDrawable(R.drawable.mug_of_adventure);
    }

    public Typeface loadDialogTypeface() {
        return Typeface.createFromAsset(mAssetManager, TYPEFACE_ASSET_PATH);
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
        return mResources.getDrawable(R.drawable.dialogbox);
    }

    public float getDialogFontSize() {
        return mResources.getDisplayMetrics().scaledDensity * DIALOG_FONT_SIZE_SP;
    }

    public JSONObject loadJSONObject(String path) {
        InputStream in = null;

        try {
            in = mAssetManager.open(path);
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
            in = mAssetManager.open(path);
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

    private final AssetManager mAssetManager;
    private final Resources mResources;
    private final int mDisplayDensity;

    private static final String TYPEFACE_ASSET_PATH = "pressstart2p.ttf";
    private static final String HERO_SPRITES_ASSET_PATH = "hero_sprites_128x128.png";
    private static final String DPAD_ASSET_PATH = "widget_dpad.png";
    private static final String BUTTON_ASSET_PATH = "widget_button.png";

    private static final float DIALOG_FONT_SIZE_SP = 14f;
    private static final int BUTTON_PADDING_HACK = 64; // Until we clean up the buttons

    private static final String LOGTAG = "hallofpresidents.AssetLoader";
}
