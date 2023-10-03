package comp5216.sydney.edu.au.grocerylist.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "food_table")
public class Food {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "foodID")
    @NonNull
    private int foodID;

    @ColumnInfo(name = "userID")
    private String userID; // 数据类型更改为String，关联到User表的userID

    @ColumnInfo(name = "foodName")
    private String foodName;

    @ColumnInfo(name = "quantity")
    private double quantity;

    @ColumnInfo(name = "isOpened")
    private boolean isOpened;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "addTime")
    private long addTime;

    @ColumnInfo(name = "productionDate")
    private long productionDate;

    @ColumnInfo(name = "expireTime")
    private int expireTime;

    @ColumnInfo(name = "bestBefore")
    private long bestBefore;

    @ColumnInfo(name = "imageURL")
    private String imageURL;

    @ColumnInfo(name = "storageCondition")
    private String storageCondition;

    @ColumnInfo(name = "storageLocation")
    private String storageLocation;

    public Food() {
    }
    // 其他字段

    // 构造函数、getter和setter方法


    public int getFoodID() {
        return foodID;
    }

    public void setFoodID(int foodID) {
        this.foodID = foodID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public long getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(long productionDate) {
        this.productionDate = productionDate;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public long getBestBefore() {
        return bestBefore;
    }

    public void setBestBefore(long bestBefore) {
        this.bestBefore = bestBefore;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStorageCondition() {
        return storageCondition;
    }

    public void setStorageCondition(String storageCondition) {
        this.storageCondition = storageCondition;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }
}
