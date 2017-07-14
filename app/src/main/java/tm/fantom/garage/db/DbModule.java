package tm.fantom.garage.db;

import android.app.Application;
import android.database.sqlite.SQLiteOpenHelper;

import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.BuildConfig;
import com.squareup.sqlbrite2.SqlBrite;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by fantom on 11-Jul-17.
 */
@Module
public final class DbModule {
    @Provides @Singleton SQLiteOpenHelper provideOpenHelper(Application application) {
        return new DbOpenHelper(application);
    }

    @Provides @Singleton SqlBrite provideSqlBrite() {
        return new SqlBrite.Builder()
                .logger(message -> Timber.tag("Database").v(message))
                .build();
    }

    @Provides @Singleton BriteDatabase provideDatabase(SqlBrite sqlBrite, SQLiteOpenHelper helper) {
        BriteDatabase db = sqlBrite.wrapDatabaseHelper(helper, Schedulers.io());
        if(BuildConfig.DEBUG)
            db.setLoggingEnabled(true);
        return db;
    }
}
