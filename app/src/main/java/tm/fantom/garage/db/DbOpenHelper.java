/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tm.fantom.garage.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

final class DbOpenHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;

    private static final String CREATE_DRIVER = ""
            + "CREATE TABLE " + DriverItem.TABLE + "("
            + DriverItem.ID + " INTEGER NOT NULL PRIMARY KEY,"
            + DriverItem.NAME + " TEXT NOT NULL,"
            + DriverItem.DESCRIPTION + " TEXT NOT NULL,"
            + DriverItem.AGE + " INTEGER NOT NULL DEFAULT 18,"
            + DriverItem.STATUS + " INTEGER NOT NULL DEFAULT 0"
            + ")";
    private static final String CREATE_VEHICLE = ""
            + "CREATE TABLE " + VehicleItem.TABLE + "("
            + VehicleItem.ID + " INTEGER NOT NULL PRIMARY KEY,"
            + VehicleItem.DRIVER_ID + " INTEGER NOT NULL REFERENCES " + DriverItem.TABLE + "(" + DriverItem.ID + "),"
            + VehicleItem.NUMBER + " TEXT NOT NULL,"
            + VehicleItem.DESCRIPTION + " TEXT NOT NULL,"
            + VehicleItem.IN_USE + " INTEGER NOT NULL DEFAULT 0"
            + ")";
    private static final String CREATE_DRIVER_ID_INDEX =
            "CREATE INDEX vehicle_driver_id ON " + VehicleItem.TABLE + " (" + VehicleItem.DRIVER_ID + ")";

    public DbOpenHelper(Context context) {
        super(context, "aguide.db", null /* factory */, VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DRIVER);
        db.execSQL(CREATE_VEHICLE);
        db.execSQL(CREATE_DRIVER_ID_INDEX);

        long incognitoId = db.insert(DriverItem.TABLE, null, new DriverItem.Builder()
                .name("available")
                .description("")
                .age(18)
                .status(false)
                .build());

        long madMaxId = db.insert(DriverItem.TABLE, null, new DriverItem.Builder()
                .name("Mad Max")
                .description("aggressive")
                .age(12)
                .status(true)
                .build());
        db.insert(VehicleItem.TABLE, null, new VehicleItem.Builder()
                .driverId(madMaxId)
                .number("unrecognized")
                .description("Jeep")
                .build());
        db.insert(VehicleItem.TABLE, null, new VehicleItem.Builder()
                .driverId(madMaxId)
                .number("MAD MAX")
                .description("Tug")
                .build());

        long blondId = db.insert(DriverItem.TABLE, null, new DriverItem.Builder()
                .name("Mary J Pink")
                .description("blond")
                .age(21)
                .status(true)
                .build());
        db.insert(VehicleItem.TABLE, null, new VehicleItem.Builder()
                .driverId(blondId)
                .number("FluFFy")
                .description("VW Beetle")
                .inUse(true)
                .build());
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
