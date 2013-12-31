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
            mCatalog = new ArrayList<CatalogItem>(catalog.length());
            for (int i = 0; i < catalog.length(); i++) {
                JSONObject itemDesc = catalog.getJSONObject(i);
                CatalogItem item = new CatalogItem(
                    itemDesc.getString("name"),
                    itemDesc.getString("path"),
                    itemDesc.getString("storage")
                );
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
    public Object getItem(int i) {
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
        }
        CatalogItem item = (CatalogItem) getItem(position);
        ((TextView) convertView).setText(item.name);
        return convertView;

    }

    public static class CatalogItem {
        public CatalogItem(String name, String path, String storage) {
            this.name = name;
            this.path = path;
            this.storage = storage;
        }
        public final String name;
        public final String path;
        public final String storage;
    }

    private final LayoutInflater mLayoutInflater;
    private final List<CatalogItem> mCatalog;
}
