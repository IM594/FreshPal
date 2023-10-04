package comp5216.sydney.edu.au.grocerylist.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import comp5216.sydney.edu.au.grocerylist.data.entities.Food;

@Dao
public interface FoodDao {

    @Insert
    void insert(Food food);

    @Query("SELECT * FROM food_table WHERE userID = :userID")
    List<Food> getFoodsByUserId(String userID);

    // 添加其他食物相关的查询和操作方法
}
