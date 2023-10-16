package comp5216.sydney.edu.au.grocerylist.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import comp5216.sydney.edu.au.grocerylist.data.entities.User;

@Dao
public interface UserDao {
    @Insert
    void insertUser(User user);

    @Query("SELECT * FROM user_table WHERE userID = :userID")
    User getUserById(String userID);

    @Query("SELECT defaultOpenExpireTime FROM user_table WHERE userID = :userID")
    int getDefaultOpenExpireTime(String userID);

    @Update
    void updateUser(User user);
    // 添加其他用户相关的查询和操作方法

    //getUserData
    @Query("SELECT * FROM user_table WHERE userID = :userID")
    User getUserData(String userID);

    //set last sync time
    @Query("UPDATE user_table SET lastSyncTime = :lastSyncTime WHERE userID = :userID")
    void setLastSyncTime(String userID, long lastSyncTime);
}
