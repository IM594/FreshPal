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

        productNameInput = findViewById(R.id.product_name_input);
        categoryInput = findViewById(R.id.category_input);
        labelImageButton = findViewById(R.id.imageButton_category_camera);
        expiredDateInput = findViewById(R.id.expired_date_input);
        dateCameraButton = findViewById(R.id.imageButton_expired_date_camera);
        storeLocationInput = findViewById(R.id.store_location_input);
        storageConditionSpinner = findViewById(R.id.storage_condition_spinner);
        foodSealedStatusGroup = findViewById(R.id.food_sealed_status_group);
        saveButton = findViewById(R.id.save_button);

        // 初始化 Room 数据库和 FoodDao
        freshPalDB = FreshPalDB.getDatabase(getApplicationContext());
        foodDao = freshPalDB.foodDao();

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
                // 获取用户输入
                String productName = productNameInput.getText().toString().trim();
                String category = categoryInput.getText().toString().trim();
                String expiredDate = expiredDateInput.getText().toString().trim();
                String storeLocation = storeLocationInput.getText().toString().trim();
                String storageCondition = storageConditionSpinner.getSelectedItem().toString();

                int selectedRadioButtonId = foodSealedStatusGroup.getCheckedRadioButtonId();
                boolean isSealed = selectedRadioButtonId == R.id.radio_sealed;

                // 检查是否有任何字段为空
                if (productName.isEmpty() || category.isEmpty() || expiredDate.isEmpty() || storeLocation.isEmpty()) {
                    Toast.makeText(AddFood.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 获取当前登录用户的userID
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userID = currentUser.getUid();

                    // 创建一个 Food 对象并设置属性
                    Food food = new Food();
                    food.setUserID(userID);
                    food.setFoodName(productName);
                    food.setCategory(category);
                    // TODO: 设置过期日期和其他属性，可以使用 Firebase ML Kit 或日期选择器
                    food.setStorageLocation(storeLocation);
                    food.setStorageCondition(storageCondition);
                    food.setOpened(isSealed);

                    // 将食品保存到数据库
                    foodDao.insert(food);

                    Toast.makeText(AddFood.this, "Food added successfully.", Toast.LENGTH_SHORT).show();
                    finish(); // 关闭当前页面
                } else {
                    Toast.makeText(AddFood.this, "User not logged in.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 处理从ImageLabelingActivity和OCRImageProcessingActivity返回的结果
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

}
