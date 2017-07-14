package tm.fantom.garage.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * Created by fantom on 12-Jul-17.
 */

final class DriversAdapter extends BaseAdapter implements Consumer<List<DriversItem>> {
    private final LayoutInflater inflater;

    private List<DriversItem> drivers = Collections.emptyList();

    public DriversAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @Override public void accept(List<DriversItem> drivers) {
        this.drivers = drivers;
        notifyDataSetChanged();
    }

    @Override public int getCount() {
        return drivers.size();
    }

    @Override public DriversItem getItem(int position) {
        return drivers.get(position);
    }

    @Override public long getItemId(int position) {
        return getItem(position).id();
    }

    @Override public boolean hasStableIds() {
        return true;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        DriversItem item = getItem(position);
        ((TextView) convertView).setText(item.name() + " (" + item.itemCount() + ")");

        return convertView;
    }

    public int getPositionById(long id){
        for (DriversItem d: drivers) {
            if(d.id() == id) return drivers.indexOf(d);
        }
        return 0;
    }
}
