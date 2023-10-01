package comp5216.sydney.edu.au.grocerylist;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {GroceryItem.class}, version = 1, exportSchema = false)
public abstract class GroceryDB extends RoomDatabase {
    private static final String DATABASE_NAME = "grocery_db";
    private static GroceryDB DBINSTANCE;//singleton

    public abstract GroceryDao groceryDao();

    public static GroceryDB getDatabase(Context context) {//
        if (DBINSTANCE == null) {
            synchronized (GroceryDB.class) {
                DBINSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        GroceryDB.class, DATABASE_NAME).build();
            }
        }
        return DBINSTANCE;
    }

    public static void destroyInstance() {
        DBINSTANCE = null;
    }
}
