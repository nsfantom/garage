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

import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import tm.fantom.garage.GarageApp;
import tm.fantom.garage.R;
import tm.fantom.garage.db.DriverItem;

/**
 * Created by fantom on 12-Jul-17.
 */

public final class NewDriverFragment extends DialogFragment {
    public static NewDriverFragment newInstance() {
        return new NewDriverFragment();
    }

    private final PublishSubject<String> createClicked = PublishSubject.create();

    @Inject BriteDatabase db;
    @BindView(R.id.driverName) EditText nameET;
    @BindView(R.id.driverDescription) EditText descriptionET;
    @BindView(R.id.driverValid) CheckBox validCB;
    Activity activity;

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        GarageApp.getComponent(activity).inject(this);
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        View view = LayoutInflater.from(context).inflate(R.layout.new_driver, null);
        ButterKnife.bind(this, view);

        Observable.combineLatest(createClicked, RxTextView.textChanges(nameET),
                (ignored, text) -> text.toString())
                .observeOn(Schedulers.io())
                .subscribe(name -> {
                    if(!isNameValid(name)){
                        activity.runOnUiThread(()->
                                Toast.makeText(getActivity(), R.string.error_invalid_name,
                                Toast.LENGTH_SHORT).show());
                        return;
                    }
                    Random r = new Random();
                    int age = r.nextInt(99 - 18 + 1) + 18;
                    db.insert(DriverItem.TABLE,
                            new DriverItem.Builder()
                                    .name(name)
                                    .description(descriptionET.getText().toString())
                                    .age(age)
                                    .status(validCB.isChecked())
                                    .build());
                });

        return new AlertDialog.Builder(context)
                .setTitle(R.string.new_driver) // Set title for dialog
                .setView(view) // Set custom View
                .setPositiveButton(R.string.create, (dialog, which) -> createClicked.onNext("clicked"))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
                .create();
    }

    //Check name length
    private boolean isNameValid(String name) {
        return !TextUtils.isEmpty(name) && name.length() > 2;
    }
}
