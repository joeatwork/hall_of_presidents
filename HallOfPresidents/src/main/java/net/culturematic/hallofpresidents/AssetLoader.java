package net.culturematic.hallofpresidents;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class AssetLoader {
    public AssetLoader(AssetManager assetManager) {
        mAssetManager = assetManager;
    }

    public int scaleInt(int original) {
        long scaleUp = (long) original * SCALE_NUMERATOR;
        long scaleDown = scaleUp / SCALE_DENOMINATOR;
        return (int) scaleDown;
    }

    public JSONObject loadJSON(String path) {
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

    public Bitmap loadBitmap(String path) {
        InputStream in = null;
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

        // REQUIRED But probably overkill- the raw bitmaps are too big to fit into
        // graphics memory without some love. In the future, this should be aware
        // of the density of the screen.
        bitmapOptions.inDensity = SCALE_DENOMINATOR;
        bitmapOptions.inTargetDensity = SCALE_NUMERATOR;

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

    // TODO this is probably a bug, at the very least we should
    // pre-scale the images appropriately. Leaving it here for now
    // on the off chance we'll be sensitive to the display density
    // of the device at some point in the future.
    private static final int SCALE_NUMERATOR = 9;
    private static final int SCALE_DENOMINATOR = 20;

    private AssetManager mAssetManager;
    private static final String LOGTAG = "hallofpresidents.AssetLoader";
}
