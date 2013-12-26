package net.culturematic.hallofpresidents;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class RoomLoader {
    public RoomLoader(AssetManager assetManager) { // TODO remove when we get this from the net
        mAssetManager = assetManager;
    }

    public Room load(String roomPath) {
        InputStream in = null;
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

        // REQUIRED But probably overkill- the raw bitmaps are too big to fit into
        // graphics memory without some love. In the future, this should be aware
        // of the density of the screen.
        bitmapOptions.inDensity = 20;
        bitmapOptions.inTargetDensity = 9;
        try {
            in = mAssetManager.open(roomPath);
            final String descriptionText = slurp(in);
            in.close();
            in = null;

            final JSONObject description = new JSONObject(descriptionText);

            final String backgroundPath = description.getString("background");
            in = mAssetManager.open(backgroundPath);
            final Bitmap background = BitmapFactory.decodeStream(in, null, bitmapOptions);
            in.close();
            in = null;

            final String furniturePath = description.getString("furniture");
            in = mAssetManager.open(furniturePath);
            final Bitmap furniture = BitmapFactory.decodeStream(in, null, bitmapOptions);
            in.close();
            in = null;

            final String terrainPath = description.getString("terrain");
            in = mAssetManager.open(terrainPath);
            final Bitmap terrain = BitmapFactory.decodeStream(in, null, bitmapOptions);
            in.close();
            in = null;

            return new Room(background, furniture, terrain, description);

        } catch (IOException e) {
            throw new RuntimeException("Can't open room " + roomPath, e);
        } catch (JSONException e) {
            throw new RuntimeException("Can't parse room description for room " + roomPath, e);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(LOGTAG, "Can't close room asset " + roomPath);
                }
            }
        }// finally
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
    private final String LOGTAG = "hallofpresidents.RoomLoader";
}
