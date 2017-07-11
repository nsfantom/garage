package tm.fantom.garage.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.sqlbrite2.BriteDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import tm.fantom.garage.GarageApp;
import tm.fantom.garage.R;

import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_IF_ROOM;
import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT;

/**
 * Created by fantom on 11-Jul-17.
 */

public class DriversFragment  extends Fragment{
    interface Listener {
        void onDriverClicked(long id);
        void onNewDriverClicked();
    }
    static DriversFragment newInstance() {
        return new DriversFragment();
    }

    @Inject BriteDatabase db;

    @BindView(android.R.id.list) ListView listView;
    @BindView(android.R.id.empty) View emptyView;

    private Listener listener;
    private DriversAdapter adapter;
    private Disposable disposable;

    @Override public void onAttach(Activity activity) {
        if (!(activity instanceof Listener)) {
            throw new IllegalStateException("Activity must implement fragment Listener.");
        }

        super.onAttach(activity);
        GarageApp.getComponent(activity).inject(this);
        setHasOptionsMenu(true);

        listener = (Listener) activity;
        adapter = new DriversAdapter(activity);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.add(R.string.new_driver)
                .setOnMenuItemClickListener(item1 -> {
                    listener.onNewDriverClicked();
                    return true;
                });
        MenuItemCompat.setShowAsAction(item, SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                       @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.drivers, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        listView.setEmptyView(emptyView);
        listView.setAdapter(adapter);
    }

    @OnItemClick(android.R.id.list) void driverClicked(long id) {
        listener.onDriverClicked(id);
    }

    @Override public void onResume() {
        super.onResume();

        getActivity().setTitle("GARAGE");

        disposable = db.createQuery(DriversItem.TABLES, DriversItem.QUERY)
                .mapToList(DriversItem.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter);
    }

    @Override public void onPause() {
        super.onPause();
        disposable.dispose();
    }

}
