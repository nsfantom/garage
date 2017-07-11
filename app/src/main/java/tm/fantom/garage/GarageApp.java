package tm.fantom.garage;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;

/**
 * Created by fantom on 11-Jul-17.
 */

public final class GarageApp extends Application {

    private GarageComponent mainComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        mainComponent = DaggerGarageComponent.builder().garageModule(new GarageModule(this)).build();
    }

    public static GarageComponent getComponent(Context context) {
        return ((GarageApp) context.getApplicationContext()).mainComponent;
    }
}
