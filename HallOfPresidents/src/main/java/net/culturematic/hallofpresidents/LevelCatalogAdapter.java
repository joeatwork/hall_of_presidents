package net.culturematic.hallofpresidents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LevelCatalogAdapter extends BaseAdapter {
    public LevelCatalogAdapter(LayoutInflater layoutInflater, LevelCatalog catalog) {
        super();
        mLayoutInflater = layoutInflater;
        mCatalog = catalog;
    }

    @Override
    public int getCount() {
        return mCatalog.size();
    }

    @Override
    public LevelCatalogItem getItem(int i) {
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

        View completedTag = convertView.findViewWithTag("tag_completed_flag");
        TextView roomNameView = (TextView) convertView.findViewWithTag("tag_room_name");
        TextView roomDescView = (TextView) convertView.findViewWithTag("tag_room_description");
        assert null != completedTag;
        assert null != roomNameView;
        assert null != roomDescView;

        LevelCatalogItem item = mCatalog.get(position);
        LevelState saved = mCatalog.getSavedState(position);
        if (null != saved && saved.isComplete()) {
            completedTag.setVisibility(View.VISIBLE);
        }

        roomNameView.setText(item.getName());
        roomDescView.setText(item.getDescription());
        return convertView;
    }

    private final LayoutInflater mLayoutInflater;
    private final LevelCatalog mCatalog;
}
