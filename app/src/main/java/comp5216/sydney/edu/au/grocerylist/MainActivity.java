package comp5216.sydney.edu.au.grocerylist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import comp5216.sydney.edu.au.grocerylist.data.dao.FoodDao;
import comp5216.sydney.edu.au.grocerylist.data.dao.UserDao;
import comp5216.sydney.edu.au.grocerylist.data.database.FreshPalDB;
import comp5216.sydney.edu.au.grocerylist.data.entities.Food;
import comp5216.sydney.edu.au.grocerylist.data.entities.User;

public class MainActivity extends AppCompatActivity {


    private FreshPalDB freshPalDB;
    private UserDao userDao;
    private FoodDao foodDao;
    FirebaseFirestore dbFire = FirebaseFirestore.getInstance();//

//    FreshPalDB freshPalDB = FreshPalDB.getDatabase(getApplicationContext());
//    UserDao userDao = freshPalDB.userDao();
//    FoodDao foodDao = freshPalDB.foodDao();

    //下面是旧的

    private GroceryDB db;
    private GroceryDao groceryDao;

    private ArrayList<GroceryItem> items;
    private EditText addItemEditText;
    private RecyclerView recyclerView;
    private GroceryListAdapter adapter;

    private DatePicker datePicker;
    private Date selectedDate;
    private String selectedDateStr;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    /* for test reminder page*/
    Button re;
    Button addFoodButton;
    Button mainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addItemEditText = findViewById(R.id.editTextItem);
        recyclerView = findViewById(R.id.recyclerView);
        datePicker = findViewById(R.id.datePicker);
        addFoodButton = findViewById(R.id.addFoodButton);
        mainButton = findViewById(R.id.main_btn);

        //新的 ---

        freshPalDB = FreshPalDB.getDatabase(getApplicationContext());
        userDao = freshPalDB.userDao();
        foodDao = freshPalDB.foodDao();

        //跳转到AddFood，测试用
        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddFood.class));
            }
        });

        findViewById(R.id.syncbtn).setOnClickListener(this::onSyncClicked);


        //新的 ---

        db = GroceryDB.getDatabase(getApplicationContext());
        groceryDao = db.groceryDao();

        items = new ArrayList<>();
        adapter = new GroceryListAdapter(items);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // set the default date to today
        datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    handleDateChange(year, monthOfYear, dayOfMonth);
                });

        // read items from database
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        handleDateChange(year, month, dayOfMonth);

        // initialize the listener for the recycler view
        setupRecyclerViewListener();

        // set the status bar to light color (black text)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // test profile
        re=findViewById(R.id.testButton);
        re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Profile.class));
            }
        });

        // test main page
//        mainButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this,Main.class));
//            }
//        });
    }

    public void onAddItemClick(View view) {
        String itemName = addItemEditText.getText().toString();
        if (!itemName.isEmpty()) {
            GroceryItem newItem = new GroceryItem(itemName, selectedDateStr);
            //if the item already exists, do not add it
            for (GroceryItem item : items) {
                if (item.getGroceryItemName().equals(newItem.getGroceryItemName())) {
                    // pop up a dialog: item already exists
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Item already exists");
                    builder.setMessage("The item you entered already exists, please re-enter");
                    builder.setPositiveButton("OK", (dialog, which) -> {
                        // do nothing
                    });
                    builder.create().show();
                    addItemEditText.setText("");
                    return;
                }
            }
            items.add(newItem);
            addItemEditText.setText("");
            insertNewItemsToDatabase();
            adapter.notifyDataSetChanged();
        }
    }

    private void readItemsFromDatabase(String selectedDate) {
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                List<GroceryItem> itemsFromDB = groceryDao.listByDate(selectedDate);
                items.clear();
                if (itemsFromDB != null && itemsFromDB.size() > 0) {
                    items.addAll(itemsFromDB);
                }
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });
                Log.i("SQLite", "Read all items from database.");
            });

            future.get();

        } catch (Exception ex) {
            Log.e("readItemsFromDatabase", ex.getStackTrace().toString());
        }
    }

    private void insertNewItemsToDatabase() {
        try {
            //CompletableFuture: to run the task in a separate thread
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {

                // first, check if the item already exists in the database
                List<GroceryItem> itemsFromDB = groceryDao.listByDate(selectedDateStr);
                boolean isExist = false;
                for (GroceryItem item : itemsFromDB) {
                    if (item.getGroceryItemName().equals(items.get(items.size() - 1).getGroceryItemName())) {
                        isExist = true;
                        break;
                    }
                }

                // if not exist, insert the new item to database
                if (!isExist) {
                    groceryDao.insert(items.get(items.size() - 1));
                    Log.i("SQLite", "insert new item to database.");
                }

            });
            future.get();//use to block until the future has completed the task
        } catch (Exception ex) {
            Log.e("saveNewItemsToDatabase", ex.getStackTrace().toString());
        }
    }


    private void updateItemsToDatabase(int groceryID, String text) {
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                groceryDao.updateById(groceryID, text);
                Log.i("SQLite", "update item from database.");
            });

            future.get();//block until the future has completed the task

        } catch (Exception ex) {
            Log.e("saveItemsToDatabase", ex.getStackTrace().toString());
        }
    }

    private void deleteItemsToDatabase(int groceryID) {
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                groceryDao.deleteById(groceryID);
                Log.i("SQLite", "delete item from database.");
            });

            future.get();//block, wait for the future to complete the task

        } catch (Exception ex) {
            Log.e("saveItemsToDatabase", ex.getStackTrace().toString());
        }
    }

    //set up the listener for the recycler view
    private void setupRecyclerViewListener() {
        adapter.setOnItemClickListener(new GroceryListAdapter.OnItemClickListener() {

            //click -> edit
            @Override
            public void onItemClick(int position) {
                GroceryItem item = items.get(position);

                // LayoutInflater: to instantiate layout XML file into its corresponding View objects
                View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_edit_item, null);

                // get the EditText of the dialog
                EditText editText = dialogView.findViewById(R.id.editTextItem);
                editText.setText(item.getGroceryItemName());

                // create an alert dialog and show it
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Edit Item");
                builder.setView(dialogView);
                builder.setPositiveButton("OK", (dialog, which) -> {
                    String newItemName = editText.getText().toString();
                    if (!newItemName.isEmpty()) {
                        item.setGroceryItemName(newItemName);
                        adapter.notifyDataSetChanged();
                        updateItemsToDatabase(item.getGroceryItemID(), newItemName);
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.create().show();
            }

            //long click -> delete
            @Override
            public void onItemLongClick(int position) {
                // 处理长按事件逻辑:弹出对话框，确认是否删除
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Delete Item");
                builder.setMessage("Are you sure you want to delete this item?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    //according to the position, get the item id! First!!
                    int groceryID = items.get(position).getGroceryItemID();
                    items.remove(position);
                    adapter.notifyDataSetChanged();
                    // delete the item according to the id
                    deleteItemsToDatabase(groceryID);

                });
                builder.setNegativeButton("No", (dialog, which) -> {
                    // do nothing
                });
                builder.create().show();
            }
        });
    }

    //handle the date change.
    private void handleDateChange(int year, int monthOfYear, int dayOfMonth) {
        selectedDateStr = formatDate(year, monthOfYear, dayOfMonth);
        readItemsFromDatabase(selectedDateStr);
    }

    // format the date to string
    private String formatDate(int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();//get the date of today
        calendar.set(year, monthOfYear, dayOfMonth);//set the date
        selectedDate = calendar.getTime();//selectedDate is the date we choose
        return dateFormat.format(selectedDate);//dateFormate.format() : format the date to string
    }
    //log out
    public void logOut(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }
    //profile
    public void profileOnclick(View view){
        startActivity(new Intent(getApplicationContext(), Profile.class));
        finish();
    }



    //---------------------同步（start）


    // 点击同步按钮
    private void onSyncClicked(View view) {
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
                                            Toast.makeText(MainActivity.this, "Sync completed.", Toast.LENGTH_SHORT).show();
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        // 处理同步失败
                                        runOnUiThread(() -> {
                                            Toast.makeText(MainActivity.this, "Sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                                    });
                        })
                        .addOnFailureListener(e -> {
                            // 处理同步失败
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        });
            }
        }).start();
    }


    // 获取本地数据库中的用户数据
    private User getLocalUserData() {
        User[] user = new User[1];
        Thread thread = new Thread(() -> {
            UserDao userDao = freshPalDB.userDao();
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
            FoodDao foodDao = freshPalDB.foodDao();
            localFoods.addAll(foodDao.getAllFood());
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
            UserDao userDao = freshPalDB.userDao();
            userDao.setLastSyncTime(FirebaseAuth.getInstance().getUid(), timestamp);
        }).start();
    }





    //---------------------同步（end）

}
