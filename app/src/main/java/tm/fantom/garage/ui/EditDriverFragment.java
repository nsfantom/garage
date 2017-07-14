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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import tm.fantom.garage.GarageApp;
import tm.fantom.garage.R;
import tm.fantom.garage.db.DriverItem;
import tm.fantom.garage.db.VehicleItem;

/**
 * Created by fantom on 14-Jul-17.
 */

public final class EditDriverFragment extends DialogFragment {
    private static final String KEY_DRIVER_ID = "driver_id";
    private static final String DRIVER_QUERY = "SELECT * FROM "
            + DriverItem.TABLE + " WHERE " + VehicleItem.ID + " = ? ";

    public static EditDriverFragment newInstance(long driverId) {
        Bundle arguments = new Bundle();
        arguments.putLong(KEY_DRIVER_ID, driverId);

        EditDriverFragment fragment = new EditDriverFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    private final PublishSubject<String> createClicked = PublishSubject.create();

    @Inject BriteDatabase db;
    @BindView(R.id.driverName) EditText nameET;
    @BindView(R.id.driverDescription) EditText descriptionET;
    @BindView(R.id.driverValid) CheckBox validCB;
    private Disposable disposable;
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
        View view = LayoutInflater.from(context).inflate(R.layout.new_driver, null);
        ButterKnife.bind(this, view);

        disposable = db.createQuery(DriverItem.TABLE, DRIVER_QUERY, String.valueOf(getDriverId()))
                .mapToOne(DriverItem.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((driver) -> {
                    nameET.setText(driver.name());
                    descriptionET.setText(driver.description());
                    validCB.setChecked(driver.status());
                });

        Observable.combineLatest(createClicked, RxTextView.textChanges(nameET),
                (ignored, text) -> text.toString())
                .observeOn(Schedulers.io())
                .subscribe(name -> {
                    if (!isNameValid(name)) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(getActivity(), R.string.error_invalid_name,
                                        Toast.LENGTH_SHORT).show());
                        return;
                    }
                    db.update(DriverItem.TABLE,
                            new DriverItem.Builder()
                                    .name(name)  // Set driver name =)
                                    .description(descriptionET.getText().toString()) // Set driver description
                                    .status(validCB.isChecked()) // Set true if valid
                                    .build(),
                            DriverItem.ID + " = ?", String.valueOf(getDriverId()));
                });
        return new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(R.string.create, (dialog, which) -> createClicked.onNext("clicked"))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
                .create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.dispose();
    }

    //Check name length
    private boolean isNameValid(String name) {
        return !TextUtils.isEmpty(name) && name.length() > 2;
    }
}
