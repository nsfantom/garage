package tm.fantom.garage.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.squareup.sqlbrite2.BriteDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import tm.fantom.garage.GarageApp;
import tm.fantom.garage.R;
import tm.fantom.garage.db.Db;
import tm.fantom.garage.db.DriverItem;
import tm.fantom.garage.db.VehicleItem;

import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_IF_ROOM;
import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT;

/**
 * Created by fantom on 12-Jul-17.
 */

public final class VehiclesFragment extends Fragment {
    private static final String KEY_DRIVER_ID = "driver_id";
    private static final String DRIVER_QUERY = "SELECT * FROM "
            + VehicleItem.TABLE + " WHERE " + VehicleItem.DRIVER_ID
            + " = ? ORDER BY " + VehicleItem.DAMAGED + " ASC";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM "
            + VehicleItem.TABLE + " WHERE " + VehicleItem.DAMAGED + " = " + Db.BOOLEAN_FALSE
            + " AND " + VehicleItem.DRIVER_ID + " = ?";
    private static final String TITLE_QUERY = "SELECT " + DriverItem.NAME
            + " FROM " + DriverItem.TABLE + " WHERE " + DriverItem.ID + " = ?";

    public interface Listener {
        void onNewVehicleClicked(long driverId);

        void onEditVehicleClicked(long vehicleId, long driverId);
    }

    public static VehiclesFragment newInstance(long driverId) {
        Bundle arguments = new Bundle();
        arguments.putLong(KEY_DRIVER_ID, driverId);

        VehiclesFragment fragment = new VehiclesFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Inject BriteDatabase db;

    @BindView(android.R.id.list) ListView listView;
    @BindView(android.R.id.empty) View emptyView;

    private Listener listener;
    private VehiclesAdapter adapter;
    private CompositeDisposable disposables;

    private long getDriverId() {
        return getArguments().getLong(KEY_DRIVER_ID);
    }

    @Override public void onAttach(Activity activity) {
        if (!(activity instanceof Listener)) {
            throw new IllegalStateException("Activity must implement fragment Listener.");
        }

        super.onAttach(activity);
        GarageApp.getComponent(activity).inject(this);
        setHasOptionsMenu(true);

        listener = (Listener) activity;
        adapter = new VehiclesAdapter(activity);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.add(R.string.new_vehicle)
                .setOnMenuItemClickListener(item1 -> {
                    listener.onNewVehicleClicked(getDriverId());
                    return true;
                });
        MenuItemCompat.setShowAsAction(item, SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == android.R.id.list) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            VehicleItem item = (VehicleItem) listView.getItemAtPosition(acmi.position);
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            menu.setHeaderTitle(item.description()); // Set model as title
        }
    }

    @Override public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit:
                VehicleItem vehicleToEdit = (VehicleItem) listView.getItemAtPosition(info.position);
                listener.onEditVehicleClicked(vehicleToEdit.id(), vehicleToEdit.driverId());
                return true;
            case R.id.delete:
                VehicleItem vehicleToDelete = (VehicleItem) listView.getItemAtPosition(info.position);
                deleteVehicle(vehicleToDelete.id());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                       @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.vehicles, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        listView.setEmptyView(emptyView);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);

        RxAdapterView.itemClickEvents(listView)
                .observeOn(Schedulers.io())
                .subscribe(event -> {
                    boolean newValue = !adapter.getItem(event.position()).isDamaged();
                    db.update(VehicleItem.TABLE, new VehicleItem.Builder().isDamaged(newValue).build(),
                            VehicleItem.ID + " = ?", String.valueOf(event.id()));
                });
    }

    @Override public void onResume() {
        super.onResume();
        String driverId = String.valueOf(getDriverId());

        disposables = new CompositeDisposable();

        Observable<Integer> itemCount = db.createQuery(VehicleItem.TABLE, COUNT_QUERY, driverId)
                .map(query -> {
                    Cursor cursor = query.run();
                    try {
                        if (!cursor.moveToNext()) {
                            throw new AssertionError("No rows");
                        }
                        return cursor.getInt(0);
                    } finally {
                        cursor.close();
                    }
                });
        Observable<String> driverName =
                db.createQuery(DriverItem.TABLE, TITLE_QUERY, driverId).map(query -> {
                    Cursor cursor = query.run();
                    try {
                        if (!cursor.moveToNext()) {
                            throw new AssertionError("No rows");
                        }
                        return cursor.getString(0);
                    } finally {
                        cursor.close();
                    }
                });
        disposables.add(
                Observable.combineLatest(driverName, itemCount, (driverName1, itemCount1) ->
                        driverName1 + " (" + itemCount1 + ")")
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(title -> getActivity().setTitle(title)));

        disposables.add(db.createQuery(VehicleItem.TABLE, DRIVER_QUERY, driverId)
                .mapToList(VehicleItem.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter));
    }

    @Override public void onPause() {
        super.onPause();
        disposables.dispose();
    }

    private void deleteVehicle(long vehicleId){
        db.delete(VehicleItem.TABLE, VehicleItem.ID + " = ?", String.valueOf(vehicleId));
    }
}
