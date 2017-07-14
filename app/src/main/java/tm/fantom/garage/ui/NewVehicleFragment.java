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
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.squareup.sqlbrite2.BriteDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import tm.fantom.garage.GarageApp;
import tm.fantom.garage.R;
import tm.fantom.garage.db.VehicleItem;

/**
 * Created by fantom on 12-Jul-17.
 */

public final class NewVehicleFragment extends DialogFragment {
    private static final String KEY_DRIVER_ID = "driver_id";

    public static NewVehicleFragment newInstance(long driverId) {
        Bundle arguments = new Bundle();
        arguments.putLong(KEY_DRIVER_ID, driverId);

        NewVehicleFragment fragment = new NewVehicleFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    private final PublishSubject<String> createClicked = PublishSubject.create();

    @Inject BriteDatabase db;
    @BindView(R.id.vehicleModel) EditText modelET;
    @BindView(R.id.vehicleNumber) EditText numberET;
    @BindView(R.id.vehicleDamaged) CheckBox damagedCB;
    Activity activity;

    private long getDriverId() {
        return getArguments().getLong(KEY_DRIVER_ID);
    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        GarageApp.getComponent(activity).inject(this);
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        View view = LayoutInflater.from(context).inflate(R.layout.new_vehicle, null);
        ButterKnife.bind(this, view);

        Observable.combineLatest(createClicked, RxTextView.textChanges(modelET),
                (ignored, text) -> text.toString())
                .observeOn(Schedulers.io())
                .subscribe(description -> {
                    if(!isModelValid(description)){
                        activity.runOnUiThread(()->
                                Toast.makeText(getActivity(), R.string.error_invalid_model,
                                        Toast.LENGTH_SHORT).show());
                        return;
                    }

                    db.insert(VehicleItem.TABLE,
                            new VehicleItem.Builder()
                                    .driverId(getDriverId())  // Set current driver id
                                    .description(description) // Set vehicle model =)
                                    .number(numberET.getText().toString()) // Set number if entered
                                    .isDamaged(damagedCB.isChecked()) // Set true if damaged
                                    .build());
                });

        return new AlertDialog.Builder(context)
                .setTitle(R.string.new_vehicle)
                .setView(view)
                .setPositiveButton(R.string.create, (dialog, which) -> createClicked.onNext("clicked"))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
                .create();
    }

    //Check model length
    private boolean isModelValid(String model) {
        return !TextUtils.isEmpty(model) && model.length() > 2;
    }
}
