package tm.fantom.garage.ui;

import android.database.Cursor;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import java.util.Arrays;
import java.util.Collection;

import io.reactivex.functions.Function;
import tm.fantom.garage.db.Db;
import tm.fantom.garage.db.DriverItem;
import tm.fantom.garage.db.VehicleItem;

/**
 * Created by fantom on 12-Jul-17.
 */
@AutoValue
abstract class DriversItem implements Parcelable {
    private static String ALIAS_DRIVER = "driver";
    private static String ALIAS_VEHICLE = "vehicle";
    private static String DRIVER_ID = ALIAS_DRIVER + "." + DriverItem.ID;
    private static String DRIVER_NAME = ALIAS_DRIVER + "." + DriverItem.NAME;
    private static String VEHICLE_COUNT = "item_count";
    private static String VEHICLE_ID = ALIAS_VEHICLE+ "." + VehicleItem.ID;
    private static String VEHICLE_DRIVER_ID = ALIAS_VEHICLE + "." + VehicleItem.DRIVER_ID;

    public static Collection<String> TABLES = Arrays.asList(DriverItem.TABLE, VehicleItem.TABLE);
    public static String QUERY = ""
            + "SELECT " + DRIVER_ID + ", " + DRIVER_NAME + ", COUNT(" + VEHICLE_ID + ") as " + VEHICLE_COUNT
            + " FROM " + DriverItem.TABLE + " AS " + ALIAS_DRIVER
            + " LEFT OUTER JOIN " + VehicleItem.TABLE + " AS " + ALIAS_VEHICLE + " ON " + DRIVER_ID + " = " + VEHICLE_DRIVER_ID
            + " GROUP BY " + DRIVER_ID;



    abstract long id();
    abstract String name();
    abstract int itemCount();

    static Function<Cursor, DriversItem> MAPPER = new Function<Cursor, DriversItem>() {
        @Override public DriversItem apply(Cursor cursor) {
            long id = Db.getLong(cursor, DriverItem.ID);
            String name = Db.getString(cursor, DriverItem.NAME);
            int itemCount = Db.getInt(cursor, VEHICLE_COUNT);
            return new AutoValue_DriversItem(id, name, itemCount);
        }
    };
}
