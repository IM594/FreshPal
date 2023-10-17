package comp5216.sydney.edu.au.grocerylist.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table")
public class User {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "userID")
    private String userID; // 将Firebase Authentication的用户ID与数据库中的userID字段统一

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "email")
    private String email;

//    @ColumnInfo(name = "password")
//    private String password; // 这里保存加密后的密码

    @ColumnInfo(name = "reminderEnabled")
    private boolean reminderEnabled;

    @ColumnInfo(name = "defaultReminderTime")
    private long defaultReminderTime;

    @ColumnInfo(name = "defaultOpenExpireTime", defaultValue = "3")
    private int defaultOpenExpireTime;
    // 其他字段
    @ColumnInfo(name = "lastSyncTime")
    private long lastSyncTime;

    // 构造函数、getter和setter方法

    @NonNull
    public String getUserID() {
        return userID;
    }

    public void setUserID(@NonNull String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public long getDefaultReminderTime() {
        return defaultReminderTime;
    }

    public void setDefaultReminderTime(long defaultReminderTime) {
        this.defaultReminderTime = defaultReminderTime;
    }

    public int getDefaultOpenExpireTime() {
        return defaultOpenExpireTime;
    }

    public void setDefaultOpenExpireTime(int defaultOpenExpireTime) {
        this.defaultOpenExpireTime = defaultOpenExpireTime;
    }
    public long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
}
