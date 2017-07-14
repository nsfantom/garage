package tm.fantom.garage.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import tm.fantom.garage.R;

/**
 * Created by fantom on 11-Jul-17.
 */

public final class MainActivity extends FragmentActivity
        implements DriversFragment.Listener, VehiclesFragment.Listener {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, DriversFragment.newInstance())
                    .commit();
        }
    }

    @Override public void onDriverClicked(long driverId) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left,
                        R.anim.slide_out_right)
                .replace(android.R.id.content, VehiclesFragment.newInstance(driverId))
                .addToBackStack(null)
                .commit();
    }

    @Override public void onNewDriverClicked() {
        NewDriverFragment.newInstance().show(getSupportFragmentManager(), "new-driver");
    }

    @Override public void onEditDriver(long id) {
        EditDriverFragment.newInstance(id).show(getSupportFragmentManager(),"edit-driver");
    }

    @Override public void onNewVehicleClicked(long driverId) {
        NewVehicleFragment.newInstance(driverId).show(getSupportFragmentManager(), "new-vehicle");
    }

    @Override public void onEditVehicleClicked(long vehicleId, long driverId) {
        EditVehicleFragment.newInstance(vehicleId, driverId).show(getSupportFragmentManager(), "edit-vehicle");
    }
}
