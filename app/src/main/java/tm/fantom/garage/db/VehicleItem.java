package tm.fantom.garage.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import io.reactivex.functions.Function;

/**
 * Created by fantom on 11-Jul-17.
 */
@AutoValue
public abstract class VehicleItem implements Parcelable {
    public static final String TABLE = "auto_item";

    public static final String ID = "_id";
    public static final String DRIVER_ID = "driver_id";
    public static final String DESCRIPTION = "description";
    public static final String NUMBER = "number";
    public static final String DAMAGED = "damaged";

    public abstract long id();
    public abstract long driverId();
    public abstract String description();
    public abstract String number();
    public abstract boolean isDamaged();

    public static final Function<Cursor, VehicleItem> MAPPER = cursor -> {
        long id = Db.getLong(cursor, ID);
        long driverId = Db.getLong(cursor, DRIVER_ID);
        String description = Db.getString(cursor, DESCRIPTION);
        String number = Db.getString(cursor, NUMBER);
        boolean isDamaged = Db.getBoolean(cursor, DAMAGED);
        return new AutoValue_VehicleItem(id, driverId, description, number, isDamaged);
    };

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder id(long id) {
            values.put(ID, id);
            return this;
        }

        public Builder driverId(long driverId) {
            values.put(DRIVER_ID, driverId);
            return this;
        }

        public Builder description(String description) {
            values.put(DESCRIPTION, description);
            return this;
        }

        public Builder number(String number) {
            values.put(NUMBER, number);
            return this;
        }

        public Builder isDamaged(boolean isDamaged) {
            values.put(DAMAGED, isDamaged);
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }
}
