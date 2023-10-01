package comp5216.sydney.edu.au.grocerylist;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "grocerylist")
public class GroceryItem {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "groceryItemID")
    private int groceryItemID;

    @ColumnInfo(name = "groceryItemName")
    private String groceryItemName;

    @ColumnInfo(name = "groceryItemDate")
    private String groceryItemDate;

    public GroceryItem(String groceryItemName, String groceryItemDate) {
        this.groceryItemName = groceryItemName;
        this.groceryItemDate = groceryItemDate;
    }

    public int getGroceryItemID() {
        return groceryItemID;
    }

    public void setGroceryItemID(int groceryItemID) {
        this.groceryItemID = groceryItemID;
    }

    public String getGroceryItemName() {
        return groceryItemName;
    }

    public void setGroceryItemName(String groceryItemName) {
        this.groceryItemName = groceryItemName;
    }

    public String getGroceryItemDate() {
        return groceryItemDate;
    }

    public void setGroceryItemDate(String groceryItemDate) {
        this.groceryItemDate = groceryItemDate;
    }
}
