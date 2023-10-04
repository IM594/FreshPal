package comp5216.sydney.edu.au.grocerylist;

// 导入所需的包

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import comp5216.sydney.edu.au.grocerylist.data.dao.FoodDao;
import comp5216.sydney.edu.au.grocerylist.data.database.FreshPalDB;
import comp5216.sydney.edu.au.grocerylist.data.entities.Food;
import comp5216.sydney.edu.au.grocerylist.data.entities.User;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddFood extends AppCompatActivity {
    EditText productNameInput, categoryInput, expiredDateInput, storeLocationInput;
    Spinner storageConditionSpinner;
    RadioGroup foodSealedStatusGroup;
    Button saveButton;
    ImageButton labelImageButton, dateCameraButton;

    private static final int REQUEST_IMAGE_LABELING = 1;
    private static final int REQUEST_DATE_IMAGE_CAPTURE = 2;


    private FreshPalDB freshPalDB;
    private FoodDao foodDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_page);


        // 初始化各个控件
        productNameInput = findViewById(R.id.product_name_input);
        categoryInput = findViewById(R.id.category_input);
        expiredDateInput = findViewById(R.id.expired_date_input);
        storeLocationInput = findViewById(R.id.store_location_input);

        storageConditionSpinner = findViewById(R.id.storage_condition_spinner);
        foodSealedStatusGroup = findViewById(R.id.food_sealed_status_group);

        labelImageButton = findViewById(R.id.imageButton_category_camera);
        dateCameraButton = findViewById(R.id.imageButton_expired_date_camera);
        saveButton = findViewById(R.id.save_button);

        // 初始化 Room 数据库和 FoodDao
        freshPalDB = FreshPalDB.getDatabase(getApplicationContext());
        foodDao = freshPalDB.foodDao();

        // 各个按钮的点击事件
        labelImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 启动ImageLabelingActivity，并希望接收结果
                Intent intent = new Intent(AddFood.this, ImageLabelingActivity.class);
                startActivityForResult(intent, REQUEST_IMAGE_LABELING); // 1是请求码，可以是任何非负整数
            }
        });

        dateCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 启动相机应用以拍摄日期图像
                Intent intent = new Intent(AddFood.this, OCRImageProcessingActivity.class);
                startActivityForResult(intent, REQUEST_DATE_IMAGE_CAPTURE); // 2是请求码，可以是任何非负整数
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(AddFood.this, "You click the add button", Toast.LENGTH_SHORT).show();

                // 如果有空的输入框，提示用户
                if (productNameInput.getText().toString().isEmpty() || categoryInput.getText().toString().isEmpty() || expiredDateInput.getText().toString().isEmpty() || storeLocationInput.getText().toString().isEmpty()) {
                    Toast.makeText(AddFood.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }


                    String productName = productNameInput.getText().toString().trim();
                    String category = categoryInput.getText().toString().trim();
                    String expiredDate = expiredDateInput.getText().toString().trim();
                    String storeLocation = storeLocationInput.getText().toString().trim();
//                    String storageCondition = storageConditionSpinner.getSelectedItem().toString();
                    Boolean isSealed = foodSealedStatusGroup.getCheckedRadioButtonId() == R.id.radio_sealed;


                    // 检查是否有输入为空，如果有则提示用户
                    if (productName.isEmpty() || category.isEmpty() || expiredDate.isEmpty() || storeLocation.isEmpty() || isSealed == null) {
                        Toast.makeText(AddFood.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 获取当前登录用户的userID
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            String userID = currentUser.getUid();
                            Toast.makeText(AddFood.this, "UID: " + userID, Toast.LENGTH_SHORT).show();

                            // 创建Food对象
                            Food food = new Food();
                            food.setUserID(userID);
                            food.setFoodName(productName);
                            food.setCategory(category);
                            food.setStorageLocation(storeLocation);
//                            food.setStorageCondition(storageCondition);
                            food.setOpened(isSealed);
                            food.setAddTime(System.currentTimeMillis());
                            food.setBestBefore(calculateBestBefore(expiredDate));//如果是0，说明日期格式不对

                            // 异步插入数据库
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    foodDao.insert(food);
                                    finish();
                                }
                            }).start();

                            // 提示用户添加成功
                            Toast.makeText(AddFood.this, "Food added successfully.", Toast.LENGTH_SHORT).show();

                            finish();

                        } else {
                            Toast.makeText(AddFood.this, "User not logged in.", Toast.LENGTH_SHORT).show();
                        }

                    }



            }
        });
    }

    // 处理从ImageLabelingActivity和OCRImageProcessingActivity返回的结果，填充到相应的输入框中
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_LABELING && resultCode == RESULT_OK && data != null) {
            // 从ImageLabelingActivity接收结果
            String selectedCategory = data.getStringExtra("category");
            categoryInput.setText(selectedCategory);
            Toast.makeText(AddFood.this, "Category added successfully: " + selectedCategory, Toast.LENGTH_SHORT).show();
        }

        if (requestCode == REQUEST_DATE_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            // 从OCRImageProcessingActivity接收结果
            String extractedDate = data.getStringExtra("extractedDate");
            expiredDateInput.setText(extractedDate);
            Toast.makeText(AddFood.this, "Date added successfully: " + extractedDate, Toast.LENGTH_SHORT).show();
        }
    }

    // 根据expiredDate计算bestBefore
    private long calculateBestBefore(String expiredDate) {
        // 校验expiredDate是否合法
        if (expiredDate == null || expiredDate.isEmpty()) {
            return 0;
        }

        //把expiredDate转换成long类型，也就是时间戳。例如，19/01/2000的00:00:00，转换成 UNIX 时间戳，UNIX 时间戳是从 1970 年 1 月 1 日 00:00:00（UTC）起经过的秒数。
        try {
            // 创建日期格式化对象，指定输入日期的格式
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            // 将expiredDate字符串解析为Date对象
            Date expiredDateObj = dateFormat.parse(expiredDate);

            // 转换为UNIX时间戳（毫秒级别）
            long timestamp = expiredDateObj.getTime();

            // bestBefore = expiredDate的timestam，直接返回
            return timestamp;
        } catch (ParseException e) {
            // 处理日期解析异常
            e.printStackTrace();
            return 0;
        }


    }

    //

}