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
public abstract class DriverItem implements Parcelable{
    public static final String TABLE = "driver_item";

    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String AGE = "age";
    public static final String STATUS = "status";

    public abstract long id();
    public abstract String name();
    public abstract String description();
    public abstract int age();
    public abstract boolean status();

    public static final Function<Cursor, DriverItem> MAPPER = new Function<Cursor, DriverItem>() {
        @Override public DriverItem apply(Cursor cursor) {
            long id = Db.getLong(cursor, ID);
            String name = Db.getString(cursor, NAME);
            String description = Db.getString(cursor, DESCRIPTION);
            int age = Db.getInt(cursor, AGE);
            boolean status = Db.getBoolean(cursor, STATUS);
            return new AutoValue_DriverItem(id, name, description, age, status);
        }
    };

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public DriverItem.Builder id(long id) {
            values.put(ID, id);
            return this;
        }

        public DriverItem.Builder name(String name) {
            values.put(NAME, name);
            return this;
        }

        public DriverItem.Builder description(String description) {
            values.put(DESCRIPTION, description);
            return this;
        }

        public DriverItem.Builder age(int age) {
            values.put(AGE, age);
            return this;
        }

        public DriverItem.Builder status(boolean status) {
            values.put(STATUS, status);
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }
}
