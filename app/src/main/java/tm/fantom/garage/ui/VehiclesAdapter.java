package tm.fantom.garage.ui;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.util.Collections;
import java.util.List;

import io.reactivex.functions.Consumer;
import tm.fantom.garage.db.VehicleItem;

/**
 * Created by fantom on 12-Jul-17.
 */

final class VehiclesAdapter extends BaseAdapter implements Consumer<List<VehicleItem>> {
    private final LayoutInflater inflater;

    private List<VehicleItem> vehicles = Collections.emptyList();

    public VehiclesAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @Override public void accept(List<VehicleItem> vehicles) {
        this.vehicles = vehicles;
        notifyDataSetChanged();
    }

    @Override public int getCount() {
        return vehicles.size();
    }

    @Override public VehicleItem getItem(int position) {
        return vehicles.get(position);
    }

    @Override public long getItemId(int position) {
        return getItem(position).id();
    }

    @Override public boolean hasStableIds() {
        return true;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
        }

        VehicleItem item = getItem(position);
        CheckedTextView textView = (CheckedTextView) convertView;
        textView.setChecked(item.isDamaged());

        CharSequence description = item.description();
        if (item.isDamaged()) {
            SpannableString spannable = new SpannableString(description);
            spannable.setSpan(new StrikethroughSpan(), 0, description.length(), 0);
            description = spannable;
        }

        textView.setText(description);

        return convertView;
    }
}
