package tm.fantom.garage.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.squareup.sqlbrite2.BriteDatabase;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import tm.fantom.garage.GarageApp;
import tm.fantom.garage.R;
import tm.fantom.garage.db.VehicleItem;

import static butterknife.ButterKnife.findById;

/**
 * Created by fantom on 12-Jul-17.
 */

public final class EditVehicleFragment extends DialogFragment {
    private static final String KEY_DRIVER_ID = "driver_id";

    public static EditVehicleFragment newInstance(long driverId) {
        Bundle arguments = new Bundle();
        arguments.putLong(KEY_DRIVER_ID, driverId);

        EditVehicleFragment fragment = new EditVehicleFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    private final PublishSubject<String> createClicked = PublishSubject.create();

    @Inject BriteDatabase db;

    private long getDriverId() {
        return getArguments().getLong(KEY_DRIVER_ID);
    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        GarageApp.getComponent(activity).inject(this);
    }

    @NonNull
    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        View view = LayoutInflater.from(context).inflate(R.layout.new_vehicle, null);

        EditText name = findById(view, R.id.vehicleModel);
        Observable.combineLatest(createClicked, RxTextView.textChanges(name),
                new BiFunction<String, CharSequence, String>() {
                    @Override public String apply(String ignored, CharSequence text) {
                        return text.toString();
                    }
                }) //
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override public void accept(String description) {
                        db.insert(VehicleItem.TABLE,
                                new VehicleItem.Builder().driverId(getDriverId()).description(description).build());
                    }
                });

        return new AlertDialog.Builder(context)
                .setTitle(R.string.new_vehicle)
                .setView(view)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        createClicked.onNext("clicked");
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override public void onClick(@NonNull DialogInterface dialog, int which) {
                    }
                })
                .create();
    }
}
