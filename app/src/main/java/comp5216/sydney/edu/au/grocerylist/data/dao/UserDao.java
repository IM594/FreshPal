package comp5216.sydney.edu.au.grocerylist.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import comp5216.sydney.edu.au.grocerylist.data.entities.User;

@Dao
public interface UserDao {
    @Insert
    void insertUser(User user);

    @Query("SELECT * FROM user_table WHERE userID = :userID")
    User getUserById(String userID);

    // 添加其他用户相关的查询和操作方法
}