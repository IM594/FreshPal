package comp5216.sydney.edu.au.grocerylist.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import comp5216.sydney.edu.au.grocerylist.data.dao.FoodDao;
import comp5216.sydney.edu.au.grocerylist.data.dao.UserDao;
import comp5216.sydney.edu.au.grocerylist.data.entities.Food;
import comp5216.sydney.edu.au.grocerylist.data.entities.User;

@Database(entities = {User.class, Food.class}, version = 1)
public abstract class FreshPalDB extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract FoodDao foodDao();

    private static volatile FreshPalDB INSTANCE;

    public static FreshPalDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FreshPalDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FreshPalDB.class, "FreshPal_Database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
