package comp5216.sydney.edu.au.grocerylist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
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
    private String foodName, category, storeLocation, storageCondition;
    private long bestBefore;
    private long currentExpireTime;
    private boolean foodStatus;
    private EditText foodNameEdit;
    private EditText categoryEdit;
    private EditText expiredDateEdit;
    private EditText storeLocationEdit;
    private Spinner storageConditionSpinner;
    private SwitchCompat foodStatusSwitch;
    private FreshPalDB mdb;
    private FoodDao mFoodDao;
    Food foodToUpdate;
    private Executor executor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail_page);

        // set the status bar to light color (black text)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //初始化数据库相关
        mdb = FreshPalDB.getDatabase(this.getApplication().getApplicationContext());
        mFoodDao = mdb.foodDao();
        intent = getIntent();
        userID = intent.getStringExtra("userID");
        Log.i(TAG, "userID " +userID);
        foodID = intent.getIntExtra("foodID", defaultValue);
        foodName = intent.getStringExtra("foodName");
        category = intent.getStringExtra("category");
        bestBefore = intent.getLongExtra("bestBefore", defaultValue);
        storeLocation = intent.getStringExtra("storeLocation");
        storageCondition = intent.getStringExtra("storageCondition");
        foodStatus = intent.getBooleanExtra("foodStatus", defaultBooleanValue);
        //初始化EditText中的值
        foodNameEdit = findViewById(R.id.food_name_input);
        foodNameEdit.setText(foodName);
        categoryEdit = findViewById(R.id.category_input);
        categoryEdit.setText(category);
        expiredDateEdit = findViewById(R.id.expired_date_input);
        // 获取当前时间的时间戳
        Date currentDate = new Date();
        // 转换为UNIX时间戳（毫秒级别）
        long currentTimestamp = currentDate.getTime();
        currentExpireTime = (bestBefore - currentTimestamp)/86400000;
        expiredDateEdit.setText(String.valueOf(currentExpireTime));
        storeLocationEdit = findViewById(R.id.store_location_input);
        storeLocationEdit.setText(storeLocation);
        storageConditionSpinner  = findViewById(R.id.storage_condition_input);
        setupStorageConditionSpinner();
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
    private void setupStorageConditionSpinner() {
        // 创建一个适配器来显示选项
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.storage_conditions, android.R.layout.simple_spinner_item);

        // 设置下拉菜单样式
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 将适配器与spinner绑定
        storageConditionSpinner.setAdapter(adapter);
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = (String) adapter.getItem(i);
            if (item != null && item.equals(storageCondition)) {
                storageConditionSpinner.setSelection(i);
                break;
            }
        }
    }


    private void onBackClick(View view) {
        Intent mainIntent = new Intent(getApplicationContext(), MainPage.class);
        mainIntent.putExtra("userID", userID);
        startActivity(mainIntent);
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
        Intent mainIntent = new Intent(getApplicationContext(), MainPage.class);
        mainIntent.putExtra("userID", userID);
        Log.i(TAG, "onEditClick: "+userID);
        startActivity(mainIntent);
    }

    private void onEditClick(View view) {
        Log.i(TAG, "onEditClick: "+Long.parseLong(expiredDateEdit.getText().toString())+userID);
        foodName = foodNameEdit.getText().toString();
        category = categoryEdit.getText().toString();
        bestBefore = bestBefore + (Long.parseLong(expiredDateEdit.getText().toString()) - currentExpireTime)*86400000;
        storeLocation = storeLocationEdit.getText().toString();
        storageCondition = storageConditionSpinner.getSelectedItem().toString();
        foodStatus = foodStatusSwitch.isChecked();
        Log.i(TAG, foodName + category + bestBefore + storeLocation + storageCondition);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    // 获得Food对象
                    foodToUpdate = mFoodDao.getFoodsByFoodId(foodID, userID);
                    Log.i(TAG, "run: " +foodToUpdate);
                    // 设定新名字
                    foodToUpdate.setFoodName(foodName);
                    foodToUpdate.setCategory(category);
                    foodToUpdate.setBestBefore(bestBefore);
                    foodToUpdate.setStorageLocation(storeLocation);
                    foodToUpdate.setStorageCondition(storageCondition);
                    foodToUpdate.setOpened(foodStatus);
                    // 更新数据库
                    mFoodDao.update(foodToUpdate);
                }
            });
        Toast Toast = null;
        Toast.makeText(FoodDetail.this, "Successfully changed", Toast.LENGTH_SHORT).show();
        Intent mainIntent = new Intent(getApplicationContext(), MainPage.class);
        mainIntent.putExtra("userID", userID);
        Log.i(TAG, "onEditClick: "+userID);
        startActivity(mainIntent);
    }
    private void onProfileClick(View view) {
        Intent intent = new Intent(FoodDetail.this, Profile.class);
        startActivity(intent);
    }

    private void onAddClick(View view) {
        Intent intent = new Intent(FoodDetail.this, AddFood.class);
        startActivity(intent);
    }

    private void onHomeClick(View view) {
        Intent mainIntent = new Intent(getApplicationContext(), MainPage.class);
        mainIntent.putExtra("userID", userID);
        startActivity(mainIntent);
    }
}
