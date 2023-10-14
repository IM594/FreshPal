package comp5216.sydney.edu.au.grocerylist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import comp5216.sydney.edu.au.grocerylist.data.dao.FoodDao;
import comp5216.sydney.edu.au.grocerylist.data.database.FreshPalDB;
import comp5216.sydney.edu.au.grocerylist.data.entities.Food;
import comp5216.sydney.edu.au.grocerylist.data.entities.User;

public class FoodDetail extends AppCompatActivity {
    private static final String TAG = "FoodDetail";
    Intent intent;
    private String userID;
    private int defaultValue = 0;
    private boolean defaultBooleanValue = false;
    private int foodID;
    private String foodName;
    private String category;
    private int expiredDate;
    private String storeLocation;
    private String storageCondition;
    private boolean foodStatus;

    private TextView mTitle;
    private TextView mFoodName;
    private EditText foodNameEdit;
    private TextView mCategory;
    private EditText categoryEdit;
    private TextView mExpiredDate;
    private EditText expiredDateEdit;
    private TextView mStoreLocation;
    private EditText storeLocationEdit;
    private TextView mStorageCondition;
    private EditText storageConditionEdit;
    private TextView mFoodStatus;
    private SwitchCompat foodStatusSwitch;
    private FreshPalDB mdb;
    private FoodDao mFoodDao;
    Food foodToUpdate;
    private Executor executor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail_page);
        //初始化数据库相关
        mdb = FreshPalDB.getDatabase(this.getApplication().getApplicationContext());
        mFoodDao = mdb.foodDao();
        intent = getIntent();
        userID = intent.getStringExtra("userID");
        foodID = intent.getIntExtra("foodID", defaultValue);
        foodName = intent.getStringExtra("foodName");
        category = intent.getStringExtra("category");
        expiredDate = intent.getIntExtra("expiredDate", defaultValue);
        storeLocation = intent.getStringExtra("storeLocation");
        storageCondition = intent.getStringExtra("storageCondition");
        foodStatus = intent.getBooleanExtra("foodStatus", defaultBooleanValue);
        //初始化EditText中的值
        foodNameEdit = findViewById(R.id.food_name_input);
        foodNameEdit.setText(foodName);
        categoryEdit = findViewById(R.id.category_input);
        categoryEdit.setText(category);
        expiredDateEdit = findViewById(R.id.expired_date_input);
        expiredDateEdit.setText(String.valueOf(expiredDate));
        storeLocationEdit = findViewById(R.id.store_location_input);
        storeLocationEdit.setText(storeLocation);
        storageConditionEdit = findViewById(R.id.storage_condition_input);
        storageConditionEdit.setText(storageCondition);
        foodStatusSwitch = findViewById(R.id.food_sealed_status_input);
        foodStatusSwitch.setChecked(foodStatus);
        executor = Executors.newFixedThreadPool(4);
        //click监视器
        findViewById(R.id.iv_backward).setOnClickListener(this::onBackClick);
        findViewById(R.id.save_button).setOnClickListener(this::onEditClick);
        findViewById(R.id.delete_button).setOnClickListener(this::onDeleteClick);
        findViewById(R.id.imageButton_home).setOnClickListener(this::onHomeClick);
        findViewById(R.id.imageButton_add).setOnClickListener(this::onAddClick);
        findViewById(R.id.imageButton_profile).setOnClickListener(this::onProfileClick);

    }

    private void onBackClick(View view) {
        Intent intent = new Intent(FoodDetail.this, MainPage.class);
        startActivity(intent);
    }

    private void onDeleteClick(View view) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onDeleteClicked: ");
                Food deleteFood = mFoodDao.getFoodsByFoodId(foodID,userID);
                mFoodDao.delete(deleteFood);

            }
        });
        Intent intent = new Intent(FoodDetail.this, MainPage.class);
        startActivity(intent);
    }

    private void onEditClick(View view) {
        Log.i(TAG, "onEditClick: ");
        foodName = foodNameEdit.getText().toString();
        category = categoryEdit.getText().toString();
        Boolean isStringInt = true;
        try {
            expiredDate = Integer.parseInt(expiredDateEdit.getText().toString()); // 尝试将字符串解析为整数
            Log.i(TAG, "run: " + expiredDate);
        } catch (NumberFormatException e) {
            isStringInt = false;
            Toast Toast = null;
            Toast.makeText(FoodDetail.this, "Please enter a valid number for the expired date.", Toast.LENGTH_SHORT).show();
        }
        storeLocation = storeLocationEdit.getText().toString();
        storageCondition = storeLocationEdit.getText().toString();
        foodStatus = foodStatusSwitch.isChecked();
        Log.i(TAG, foodName + category + expiredDate + storeLocation + storageCondition);

        if (isStringInt) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    // 获得Food对象
                    foodToUpdate = mFoodDao.getFoodsByFoodId(foodID, userID);
                    // 设定新名字
                    foodToUpdate.setFoodName(foodName);
                    foodToUpdate.setCategory(category);
                    foodToUpdate.setExpireTime(expiredDate);
                    foodToUpdate.setStorageLocation(storeLocation);
                    foodToUpdate.setStorageCondition(storageCondition);
                    foodToUpdate.setOpened(foodStatus);
                    // 更新数据库
                    mFoodDao.update(foodToUpdate);
                }
            });
        }
        Toast Toast = null;
        Toast.makeText(FoodDetail.this, "Successfully changed", Toast.LENGTH_SHORT).show();
    }
    /*private boolean isChanged(){
        if (foodNameEdit.getText().toString().equals(foodName)
                & categoryEdit.getText().toString().equals(category)
                & expiredDateEdit.getText().toString().equals(expiredDate)
                & storeLocationEdit.getText().toString().equals(storeLocation)
                & storageConditionEdit.getText().toString().equals(storageCondition)
                & foodStatusSwitch.isChecked() == foodStatus) {
            return false;
        } else{
            return true;
        }
    }*/
    private void onProfileClick(View view) {
        Intent intent = new Intent(FoodDetail.this, Profile.class);
        startActivity(intent);
    }

    private void onAddClick(View view) {
        Intent intent = new Intent(FoodDetail.this, AddFood.class);
        startActivity(intent);
    }

    private void onHomeClick(View view) {
        Intent intent = new Intent(FoodDetail.this, MainPage.class);
        startActivity(intent);
    }
}
