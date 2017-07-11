package tm.fantom.garage;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import tm.fantom.garage.db.DbModule;

/**
 * Created by fantom on 11-Jul-17.
 */
@Module(
        includes = {
                DbModule.class,
        }
)
public final class GarageModule {
    private final Application application;

    GarageModule(Application application) {
        this.application = application;
    }

    @Provides @Singleton
    Application provideApplication() {
        return application;
    }
}
