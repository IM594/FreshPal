package comp5216.sydney.edu.au.grocerylist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Query;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import comp5216.sydney.edu.au.grocerylist.data.dao.FoodDao;
import comp5216.sydney.edu.au.grocerylist.data.dao.UserDao;
import comp5216.sydney.edu.au.grocerylist.data.database.FreshPalDB;
import comp5216.sydney.edu.au.grocerylist.data.entities.Food;
import comp5216.sydney.edu.au.grocerylist.data.entities.User;

public class MainPage extends AppCompatActivity implements
        View.OnClickListener,
        FilterDialogFragment.FilterListener{
    private static final String TAG = "MainPage";
    //从login调过来当前的userID
    private String userID;
    private TextView productList;
    private ListView mFoodList;
    private TextView mCurrentSearchView;
    private ImageView syncStatus;

    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private FilterDialogFragment mFilterDialog;
    private FreshPalDB mdb;
    private FoodDao mFoodDao;
    private UserDao mUserDao;
    private FoodAdapter foodadapter;
    private MainPageViewModel mViewModel;
    private List<String> categorySpinner;
    private List<String> StorageSpinner;
    private List<String> foodNameSpinner;
    private List<Food> foodsFromDB;
    private int defaultOpenExpireTime;
    private int AutoSyncTime = 1;//day
    private int ManulSyncTime = 20;//min


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        //        // set the status bar to dark color (white text)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);
//
//        //设置status bar的颜色
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));

        productList = findViewById(R.id.product_list_title);
        syncStatus = findViewById(R.id.sync_status);
        // 如果没有联网，尝试从登录状态中获取userID，如果已经联网，则尝试获取userID
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            userID = getIntent().getStringExtra("userID");
        }

        Log.i(TAG, "onCreate: "+userID);

        mCurrentSearchView = findViewById(R.id.text_current_search);
        //初始化数据库相关
        mdb = FreshPalDB.getDatabase(this.getApplication().getApplicationContext());
        mFoodDao = mdb.foodDao();
        mUserDao = mdb.userDao();
        //mUserDao = mdb.userDao();
        //从数据库中读取food数据并显示mlistview
        readFoodFromDatabase();
        //界面click监视器
        findViewById(R.id.filter_bar).setOnClickListener(this::onFilterClick);
        findViewById(R.id.button_clear_filter).setOnClickListener(this::onClearFilterClickedClick);
        findViewById(R.id.sync).setOnClickListener(this::onSyncClicked);
//        findViewById(R.id.imageButton_home).setOnClickListener(this::onHomeClick);
        findViewById(R.id.add_nav).setOnClickListener(this::onAddClick);
        findViewById(R.id.profile_nav).setOnClickListener(this::onProfileClick);
        // View model
        mViewModel = new ViewModelProvider(this).get(MainPageViewModel.class);
        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();
        //auto sync
        checkAndAutoSync();
        //update icon
        checkSyncStatusAndUpdateIcon();
    }

    private void setupListViewListener() {
        mFoodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.i("MainPage", "clicked");
                // 获取点击的food对象
                Food clickedFood = (Food) mFoodList.getItemAtPosition(position);
//                Toast.makeText(MainPage.this,clickedFood.getFoodName(),Toast.LENGTH_SHORT).show();
                // Intent来启动新的Activity
                Intent intent = new Intent(MainPage.this, FoodDetail.class);  //当前的Activity和您想要跳转的目标Activity

                if (intent != null) {
                    // (可选) 将food对象或其属性作为额外数据放入intent中
                    intent.putExtra("defaultTime", defaultOpenExpireTime);
                    intent.putExtra("userID", userID);
                    intent.putExtra("foodID", clickedFood.getFoodID());
                    intent.putExtra("foodName", clickedFood.getFoodName());
                    intent.putExtra("category", clickedFood.getCategory());
                    intent.putExtra("bestBefore", clickedFood.getBestBefore());
                    intent.putExtra("storeLocation", clickedFood.getStorageLocation());
                    intent.putExtra("storageCondition", clickedFood.getStorageCondition());
                    intent.putExtra("foodStatus", clickedFood.isOpened());
                    // bring up the second activity
                    startActivity(intent);

                }
            }
        });
    }

    private void readFoodFromDatabase() {
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    //从数据库中获取food数据
                    foodsFromDB = mFoodDao.getFoodsByUserId(userID);
                    defaultOpenExpireTime = mUserDao.getDefaultOpenExpireTime(userID);
                    Log.i(TAG, "$$$$$$$$$$$$$$ "+foodsFromDB.size());
                    foodNameSpinner = mFoodDao.getDistinctFoodName(userID);
                    categorySpinner = mFoodDao.getDistinctCategories(userID);
                    StorageSpinner = mFoodDao.getDistinctStorageCondition(userID);
                    Log.d("spinner", foodNameSpinner.toString());
                    Log.d("spinner", categorySpinner.toString());
                    Log.d("spinner", StorageSpinner.toString());
                    //if (foodsFromDB != null & foodsFromDB.size() > 0) {
                        // 初始化Adapter
                        foodadapter = new FoodAdapter(MainPage.this, foodsFromDB);
                        Log.i(TAG, "$$$$$$$$$$$$$$ "+foodadapter);
                        // 设置ListView的Adapter
                        mFoodList = findViewById(R.id.list_view);
                        mFoodList.setAdapter(foodadapter);
                        setupListViewListener();
                    //}
                    Log.i(TAG, "$$$$$$$$$$$$$$ "+foodadapter);
                    Log.i(TAG, "run: read data success");
                }
            });
            future.get();
        }
        catch(Exception ex) {
            Log.e("readFoodFromDatabase", ex.getStackTrace().toString());
        }
    }
    @Override
    public void onStart() {
        super.onStart();


        // Apply filters
        onFilter(mViewModel.getFilters());

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    private void showTodoToast() {
        Toast.makeText(this, "TODO: Implement", Toast.LENGTH_SHORT).show();
    }


    private void onProfileClick(View view) {
        Intent intent = new Intent(MainPage.this, Profile.class);
        startActivity(intent);
    }

    private void onAddClick(View view) {
        Intent intent = new Intent(MainPage.this, AddFood.class);
        startActivity(intent);
    }

    private void onHomeClick(View view) {
        Intent intent = new Intent(MainPage.this, MainPage.class);
        startActivity(intent);
    }

    private void onClearFilterClickedClick(View view) {
        Intent mainIntent = new Intent(getApplicationContext(), MainPage.class);
        mainIntent.putExtra("userID", userID);
        startActivity(mainIntent);
    }

    public void onFilter(Filters filters) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String fCategory = null;
                String fStorage = null;
                long startTime = -30000;
                long endTime = 30000;
                String fName = null;
                // Category
                if (!filters.getCategory().equals("ALL")) {
                    fCategory = filters.getCategory();
                    Log.i(TAG, fCategory);
                }
                // storage condition
                if (!filters.getStorage().equals("ALL")) {
                    fStorage = filters.getStorage();
                }

                // expire days
                startTime = filters.getFrom()-1;
                endTime = filters.getTo()+1;
                if(startTime > endTime) {
                    startTime = -30000;
                    endTime = 30000;
                }
                //food name
                if (!filters.getProductName().equals("ALL")) {
                    fName = filters.getProductName();
                }
                // 获取当前时间的时间戳
                Date currentDate = new Date();
                // 转换为UNIX时间戳（毫秒级别）
                long currentTimestamp = currentDate.getTime();
                long bestBeforeFrom = currentTimestamp + startTime*86400000;
                long bestBeforeTo = currentTimestamp +endTime*86400000;
                //数据库查找
                List<Food> filterFoods = mFoodDao.filterFoods(fCategory, fStorage, bestBeforeFrom, bestBeforeTo, fName, userID);
                foodsFromDB = filterFoods;
            }
        }).start();
        // Set header
        mCurrentSearchView.setText(Html.fromHtml(filters.getSearchDescription(this)));

        // Save filters
        mViewModel.setFilters(filters);
//        showTodoToast();

        // Set header
        mCurrentSearchView.setText(Html.fromHtml(filters.getSearchDescription(this)));

        // Save filters
        mViewModel.setFilters(filters);

        foodadapter = new FoodAdapter(MainPage.this, foodsFromDB);
        mFoodList = findViewById(R.id.list_view);
        mFoodList.setAdapter(foodadapter);
        setupListViewListener();
    }

    private void onFilterClick(View view) {
        // Show the dialog containing filter options
        //给DialogFragment的spinner传值
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("foodName", (ArrayList<String>) foodNameSpinner);
        bundle.putStringArrayList("category", (ArrayList<String>) categorySpinner);
        bundle.putStringArrayList("condition", (ArrayList<String>) StorageSpinner);
        // Filter Dialog
        mFilterDialog = new FilterDialogFragment();
        //通过setArguments(bundle)方法传递数据
        mFilterDialog.setArguments(bundle);
        mFilterDialog.show(getSupportFragmentManager(), FilterDialogFragment.TAG);
    }


    //---------------------同步（start）
    private void Syncoperation(){
        new Thread(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = FirebaseAuth.getInstance().getUid();

            if (userId != null) {
                // 1. 检查云端是否已经存在 userId 的数据
                DocumentReference userSettingRef = db.collection(userId).document("user_setting");

                userSettingRef.get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // 2. 如果文档存在，删除云端的 user_setting 和 food_information 文档
                                db.collection(userId).document("user_setting").delete();
                                db.collection(userId).document("food_information").delete();
                            }

                            // 3. 继续执行上传操作
                            WriteBatch batch = db.batch();

                            DocumentReference newUserSettingRef = db.collection(userId).document("user_setting");
                            User localUser = getLocalUserData();
                            Map<String, Object> userSettingData = new HashMap<>();
                            userSettingData.put("username", localUser.getUsername());
                            userSettingData.put("email", localUser.getEmail());
                            userSettingData.put("reminderEnabled", localUser.isReminderEnabled());
                            userSettingData.put("defaultReminderTime", localUser.getDefaultReminderTime());
                            userSettingData.put("defaultOpenExpireTime", localUser.getDefaultOpenExpireTime());
                            userSettingData.put("lastSyncTime", System.currentTimeMillis());
                            batch.set(newUserSettingRef, userSettingData);

                            DocumentReference newFoodInformationRef = db.collection(userId).document("food_information");
                            List<Food> localFoods = getLocalFoodData();
                            List<Map<String, Object>> foodDataList = new ArrayList<>();
                            for (Food food : localFoods) {
                                Map<String, Object> foodData = new HashMap<>();
                                foodData.put("foodName", food.getFoodName());
                                foodData.put("quantity", food.getQuantity());
                                foodData.put("isOpened", food.isOpened());
                                foodData.put("category", food.getCategory());
                                foodData.put("addTime", food.getAddTime());
                                foodData.put("productionDate", food.getProductionDate());
                                foodData.put("expireTime", food.getExpireTime());
                                foodData.put("bestBefore", food.getBestBefore());
                                foodData.put("imageURL", food.getImageURL());
                                foodData.put("storageCondition", food.getStorageCondition());
                                foodData.put("storageLocation", food.getStorageLocation());
                                foodDataList.add(foodData);
                            }
                            batch.set(newFoodInformationRef, Collections.singletonMap("food_collection", foodDataList));

                            // 4. 提交批处理操作
                            batch.commit()
                                    .addOnSuccessListener(aVoid -> {
                                        // 同步成功后，更新本地数据库的 lastSyncTime
                                        updateLocalLastSyncTime(System.currentTimeMillis());

                                        // 更新UI或显示消息
                                        runOnUiThread(() -> {
                                            Toast.makeText(MainPage.this, "Sync completed.", Toast.LENGTH_SHORT).show();
                                            findViewById(R.id.sync).setVisibility(View.GONE);
                                            findViewById(R.id.sync_status).setVisibility(View.VISIBLE);
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        // 处理同步失败
                                        runOnUiThread(() -> {
                                            Toast.makeText(MainPage.this, "Sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            findViewById(R.id.sync).setVisibility(View.VISIBLE);
                                            findViewById(R.id.sync_status).setVisibility(View.GONE);
                                        });
                                    });
                        })
                        .addOnFailureListener(e -> {
                            // 处理同步失败
                            runOnUiThread(() -> {
                                Toast.makeText(MainPage.this, "Sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        });
            }
        }).start();
    }

    // 点击同步按钮
    private void onSyncClicked(View view) {
        Syncoperation();
    }


    // 获取本地数据库中的用户数据
    private User getLocalUserData() {
        User[] user = new User[1];
        Thread thread = new Thread(() -> {
            UserDao userDao = mdb.userDao();
            user[0] = userDao.getUserData(FirebaseAuth.getInstance().getUid());
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user[0];
    }


    // 获取本地数据库中的食物数据
    private List<Food> getLocalFoodData() {
        List<Food> localFoods = new ArrayList<>();
        Thread thread = new Thread(() -> {
            FoodDao foodDao = mdb.foodDao();
            localFoods.addAll(foodDao.getFoodsByUserId(userID));
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return localFoods;
    }


    // 更新本地数据库的 lastSyncTime
    private void updateLocalLastSyncTime(long timestamp) {
        // 更新本地数据库的 lastSyncTime 字段
        new Thread(() -> {
            UserDao userDao = mdb.userDao();
            userDao.setLastSyncTime(FirebaseAuth.getInstance().getUid(), timestamp);
            syncStatus.setImageResource(R.drawable.ok);
        }).start();
    }

    //---------------------同步（end）

    @Override
    public void onClick(View view) {
    }

    // 加入icon的出现与消失
    private void checkSyncStatusAndUpdateIcon() {
        new Thread(() -> {
            User currentUser = mUserDao.getUserData(userID);
            if(currentUser == null){
                return;
            }
            Long lastSyncTime = currentUser.getLastSyncTime();
            if (lastSyncTime == null||lastSyncTime == 0) {
                // 如果lastSyncTime为null
                updateIconVisibility(true);
            } else {
                long currentTimeMillis = System.currentTimeMillis();
                long daysDifference = (currentTimeMillis - lastSyncTime) / (1000 * 60 * 60 * 24);
                long minDirrerence = (currentTimeMillis - lastSyncTime) / (1000 * 60);
                if (minDirrerence > ManulSyncTime) {
                    // 如果lastSyncTime距离今天超过20min
                    updateIconVisibility(true);
                } else {
                    updateIconVisibility(false);
                }
            }
        }).start();
    }

    private void updateIconVisibility(boolean showSyncIcon) {
        runOnUiThread(() -> {
            if (showSyncIcon) {
                findViewById(R.id.sync).setVisibility(View.VISIBLE);
                findViewById(R.id.sync_status).setVisibility(View.GONE);
            } else {
                findViewById(R.id.sync).setVisibility(View.GONE);
                findViewById(R.id.sync_status).setVisibility(View.VISIBLE);
            }
        });
    }

    // 新增方法: 检查条件并决定是否自动同步
    private void checkAndAutoSync() {
        new Thread(() -> {
            User currentUser = mUserDao.getUserData(userID);
            if(currentUser==null){
                return;
            }
            Long lastSyncTime = currentUser.getLastSyncTime();
            if (lastSyncTime != null) {
                long currentTimeMillis = System.currentTimeMillis();
                long daysDifference = (currentTimeMillis - lastSyncTime) / (1000 * 60 * 60 * 24);
                long minDirrerence = (currentTimeMillis - lastSyncTime) / (1000 * 60);
                if (daysDifference > AutoSyncTime && isBatteryAbove80() && isConnectedToWiFi()) {
                    // 如果满足条件，自动调用同步操作
                    Syncoperation();
                }else{
                    runOnUiThread(() -> {
                        if (!isConnectedToWiFi()) {
                            Toast.makeText(MainPage.this, "Auto Sync failed: Not connect WIFI", Toast.LENGTH_SHORT).show();
                        } else if (!isBatteryAbove80()) {
                            Toast.makeText(MainPage.this, "Auto Sync failed: Battery lower 80%", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    // 新增方法: 检查电池电量是否大于80%
    private boolean isBatteryAbove80() {
        BatteryManager batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            return battery > 80;
        }
        return false;
    }

    // 新增方法: 检查是否连接到WiFi
    private boolean isConnectedToWiFi() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI && activeNetwork.isConnected();
    }


}