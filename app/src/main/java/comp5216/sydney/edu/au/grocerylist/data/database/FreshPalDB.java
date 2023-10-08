package comp5216.sydney.edu.au.grocerylist.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import comp5216.sydney.edu.au.grocerylist.data.dao.FoodDao;
import comp5216.sydney.edu.au.grocerylist.data.dao.UserDao;
import comp5216.sydney.edu.au.grocerylist.data.entities.Food;
import comp5216.sydney.edu.au.grocerylist.data.entities.User;

@Database(entities = {User.class, Food.class}, version = 2)
public abstract class FreshPalDB extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract FoodDao foodDao();

    private static volatile FreshPalDB INSTANCE;

    public static FreshPalDB getDatabase(final Context context) {
        Migration MIGRATION_1_2 = new Migration(1, 2) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE user_table ADD COLUMN lastSyncTime INTEGER DEFAULT 0 NOT NULL");
            }
        };

        if (INSTANCE == null) {
            synchronized (FreshPalDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FreshPalDB.class, "FreshPal_Database")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    //migration


}
