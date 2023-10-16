package comp5216.sydney.edu.au.grocerylist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Query;


import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import comp5216.sydney.edu.au.grocerylist.data.dao.FoodDao;
import comp5216.sydney.edu.au.grocerylist.data.dao.UserDao;
import comp5216.sydney.edu.au.grocerylist.data.database.FreshPalDB;
import comp5216.sydney.edu.au.grocerylist.data.entities.Food;

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
    //private UserDao mUserDao;
    private FoodAdapter foodadapter;
    private MainPageViewModel mViewModel;
    private List<String> categorySpinner;
    private List<String> StorageSpinner;
    private List<String> foodNameSpinner;
    private List<Food> foodsFromDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
        productList = findViewById(R.id.product_list_title);
        syncStatus = findViewById(R.id.sync_status);
        userID = "wz7j5k64S1aDg5zlgAxMQH7APBF2";
                //getIntent().getStringExtra("userID");
        Log.i(TAG, "onCreate: "+userID);

        mCurrentSearchView = findViewById(R.id.text_current_search);
        //初始化数据库相关
        mdb = FreshPalDB.getDatabase(this.getApplication().getApplicationContext());
        mFoodDao = mdb.foodDao();
        //mUserDao = mdb.userDao();
        //从数据库中读取food数据并显示mlistview
        readFoodFromDatabase();
        //界面click监视器
        findViewById(R.id.filter_bar).setOnClickListener(this::onFilterClick);
        findViewById(R.id.button_clear_filter).setOnClickListener(this::onClearFilterClickedClick);
        findViewById(R.id.sync).setOnClickListener(this::onSyncClicked);
        findViewById(R.id.imageButton_home).setOnClickListener(this::onHomeClick);
        findViewById(R.id.imageButton_add).setOnClickListener(this::onAddClick);
        findViewById(R.id.imageButton_profile).setOnClickListener(this::onProfileClick);
        // View model
        mViewModel = new ViewModelProvider(this).get(MainPageViewModel.class);
        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();
    }

    private void setupListViewListener() {
        mFoodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.i("MainPage", "clicked");
                // 获取点击的food对象
                Food clickedFood = (Food) mFoodList.getItemAtPosition(position);
                Toast.makeText(MainPage.this,clickedFood.getFoodName(),Toast.LENGTH_SHORT).show();
                // Intent来启动新的Activity
                Intent intent = new Intent(MainPage.this, FoodDetail.class);  //当前的Activity和您想要跳转的目标Activity

                if (intent != null) {
                    // (可选) 将food对象或其属性作为额外数据放入intent中
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
        showTodoToast();

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

    private void onSyncClicked(View view) {
        syncStatus.setImageResource(R.drawable.ok);
    }


    @Override
    public void onClick(View view) {
    }
}