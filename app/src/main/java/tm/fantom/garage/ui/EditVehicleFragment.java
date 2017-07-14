package tm.fantom.garage.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.squareup.sqlbrite2.BriteDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import tm.fantom.garage.GarageApp;
import tm.fantom.garage.R;
import tm.fantom.garage.db.VehicleItem;

/**
 * Created by fantom on 14-Jul-17.
 */

public final class EditVehicleFragment extends DialogFragment {
    private static final String KEY_VEHICLE_ID = "vehicle_id";
    private static final String KEY_DRIVER_ID = "driver_id";
    private static final String VEHICLE_QUERY = "SELECT * FROM "
            + VehicleItem.TABLE + " WHERE " + VehicleItem.ID
            + " = ? ";

    public static EditVehicleFragment newInstance(long vehicleId, long driverId) {
        Bundle arguments = new Bundle();
        arguments.putLong(KEY_VEHICLE_ID, vehicleId);
        arguments.putLong(KEY_DRIVER_ID, driverId);

        EditVehicleFragment fragment = new EditVehicleFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    private final PublishSubject<String> createClicked = PublishSubject.create();

    @Inject BriteDatabase db;
    @BindView(R.id.spinner) Spinner spinner;
    @BindView(R.id.vehicleModel) EditText modelET;
    @BindView(R.id.vehicleNumber) EditText numberET;
    @BindView(R.id.vehicleDamaged) CheckBox damagedCB;
    private DriversAdapter adapter;
    private CompositeDisposable disposables;
    Activity activity;
    private long selectedDriver;

    private long getVehicleId() {
        return getArguments().getLong(KEY_VEHICLE_ID);
    }

    private long getDriverId() {
        return getArguments().getLong(KEY_DRIVER_ID);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        GarageApp.getComponent(activity).inject(this);
        adapter = new DriversAdapter(activity);
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        View view = LayoutInflater.from(context).inflate(R.layout.edit_vehicle, null);
        ButterKnife.bind(this, view);
        disposables = new CompositeDisposable();
        disposables.add(db.createQuery(DriversItem.TABLES, DriversItem.QUERY)
                .mapToList(DriversItem.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter)
        );
        spinner.setAdapter(adapter);// set adapter

        disposables.add(db.createQuery(VehicleItem.TABLE, VEHICLE_QUERY, String.valueOf(getVehicleId()))
                .mapToOne(VehicleItem.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((vehicle) -> {
                    modelET.setText(vehicle.description());
                    numberET.setText(vehicle.number());
                    damagedCB.setChecked(vehicle.isDamaged());
                    final int position = adapter.getPositionById(getDriverId()); //position in spinner
                    if (position > 0 && position < adapter.getCount()) // set if correct index
                        spinner.setSelection(position);
                })
        );

        Observable.combineLatest(createClicked, RxTextView.textChanges(modelET),
                (ignored, text) -> text.toString())
                .observeOn(Schedulers.io())
                .subscribe(description -> {
                    if (!isModelValid(description)) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(getActivity(), R.string.error_invalid_model,
                                        Toast.LENGTH_SHORT).show());
                        return;
                    }
                    selectedDriver = ((DriversItem) spinner.getSelectedItem()).id();
                    db.update(VehicleItem.TABLE,
                            new VehicleItem.Builder()
                                    .driverId(selectedDriver)  // Set current driver id
                                    .description(description) // Set vehicle model =)
                                    .number(numberET.getText().toString()) // Set number if entered
                                    .isDamaged(damagedCB.isChecked()) // Set true if damaged
                                    .build(),
                            VehicleItem.ID + " = ?", String.valueOf(getVehicleId()));
                });
        return new AlertDialog.Builder(context)
                //.setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.create, (dialog, which) -> createClicked.onNext("clicked"))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
                .create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.dispose();
    }

    //Check model length
    private boolean isModelValid(String model) {
        return !TextUtils.isEmpty(model) && model.length() > 2;
    }
}
