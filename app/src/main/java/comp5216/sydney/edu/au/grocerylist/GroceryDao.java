package comp5216.sydney.edu.au.grocerylist;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface GroceryDao {

    @Query("SELECT * FROM grocerylist")
    List<GroceryItem> listAll();

    @Insert
    void insert(GroceryItem groceryItem);

    @Update
    void update(GroceryItem groceryItem);

    @Delete
    void delete(GroceryItem groceryItem);

    //delete by id
    @Query("DELETE FROM grocerylist WHERE groceryItemID = :id")
    void deleteById(int id);

    //update by id
    @Query("UPDATE grocerylist SET groceryItemName = :name WHERE groceryItemID = :id")
    void updateById(int id, String name);

    @Query("DELETE FROM grocerylist")
    void deleteAll();

    @Query("SELECT * FROM grocerylist WHERE groceryItemDate = :selectedDate")
    List<GroceryItem> listByDate(String selectedDate);
}

