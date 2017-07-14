package tm.fantom.garage;

import javax.inject.Singleton;

import dagger.Component;
import tm.fantom.garage.ui.DriversFragment;
import tm.fantom.garage.ui.EditDriverFragment;
import tm.fantom.garage.ui.EditVehicleFragment;
import tm.fantom.garage.ui.NewDriverFragment;
import tm.fantom.garage.ui.NewVehicleFragment;
import tm.fantom.garage.ui.VehiclesFragment;

/**
 * Created by fantom on 11-Jul-17.
 */
@Singleton
@Component(modules = GarageModule.class)
public interface GarageComponent {
    void inject(DriversFragment fragment);
    void inject(VehiclesFragment fragment);
    void inject(NewVehicleFragment fragment);
    void inject(NewDriverFragment fragment);
    void inject(EditVehicleFragment fragment);
    void inject(EditDriverFragment fragment);
}
