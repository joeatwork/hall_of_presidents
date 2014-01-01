package net.culturematic.hallofpresidents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RoomCatalogAdapter extends BaseAdapter {
    public RoomCatalogAdapter(LayoutInflater layoutInflater, JSONArray catalog) {
        super();
        mLayoutInflater = layoutInflater;
        try {
            mCatalog = new ArrayList<RoomCatalogItem>(catalog.length());
            for (int i = 0; i < catalog.length(); i++) {
                final JSONObject itemDesc = catalog.getJSONObject(i);
                final RoomCatalogItem item = RoomCatalogItem.readJSON(itemDesc);
                mCatalog.add(item);
            }
        } catch (JSONException e) {
            throw new RuntimeException("Can't parse catalog", e);
        }
    }

    @Override
    public int getCount() {
        return mCatalog.size();
    }

    @Override
    public RoomCatalogItem getItem(int i) {
        return mCatalog.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.catalog_entry_view, container, false);
            assert null != convertView;
        }
        RoomCatalogItem item = getItem(position);
        ((TextView) convertView).setText(item.getName());
        return convertView;
    }

    private final LayoutInflater mLayoutInflater;
    private final List<RoomCatalogItem> mCatalog;
}
