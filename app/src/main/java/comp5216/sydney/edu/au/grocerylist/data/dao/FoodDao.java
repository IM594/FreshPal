package comp5216.sydney.edu.au.grocerylist.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import comp5216.sydney.edu.au.grocerylist.data.entities.Food;

@Dao
public interface FoodDao {

    @Insert
    void insert(Food food);
    @Update
    void update(Food food);
    @Delete
    void delete(Food food);


    @Query("SELECT * FROM food_table WHERE category = :category")
    List<Food> findByCategory(String category);
    @Query("SELECT * FROM food_table WHERE storageCondition = :storage")
    List<Food> findByStorage(String storage);
    @Query("SELECT * FROM food_table WHERE foodName = :name")
    List<Food> findByFoodName(String name);
    @Query("SELECT * FROM food_table WHERE bestBefore BETWEEN :startDate AND :endDate")
    List<Food> getFoodsBetweenDates(long startDate, long endDate);

    @Query("SELECT * FROM food_table WHERE userID = :userID")
    List<Food> getFoodsByUserId(String userID);
    @Query("SELECT * FROM food_table WHERE (:foodName IS NULL OR foodName = :foodName) AND " +
            "(expireTime BETWEEN :startTime AND :endTime) AND " +
            "(:category IS NULL OR category = :category) AND " +
            "(:storageCondition IS NULL OR storageCondition = :storageCondition)")
    List<Food> filterFoods(String category, String storageCondition,  Long startTime, Long endTime, String foodName);



    @Query("SELECT DISTINCT category FROM food_table WHERE userID = :userID")
    List<String> getDistinctCategories(String userID);
    @Query("SELECT DISTINCT foodName FROM food_table WHERE userID = :userID")
    List<String> getDistinctFoodName(String userID);
    @Query("SELECT DISTINCT storageCondition FROM food_table WHERE userID = :userID")
    List<String> getDistinctStorageCondition(String userID);
    // 添加其他食物相关的查询和操作方法
    @Query("SELECT * FROM food_table WHERE foodID = :foodID AND userID = :userID")
    Food getFoodsByFoodId(int foodID, String userID);
}