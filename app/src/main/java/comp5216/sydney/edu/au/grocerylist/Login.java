package comp5216.sydney.edu.au.grocerylist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import comp5216.sydney.edu.au.grocerylist.data.dao.UserDao;
import comp5216.sydney.edu.au.grocerylist.data.database.FreshPalDB;
import comp5216.sydney.edu.au.grocerylist.data.entities.Food;
import comp5216.sydney.edu.au.grocerylist.data.entities.User;

public class Login extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView registerButton, forgetTextLink;
    FirebaseAuth fAuth;
    String userId;
    // 初始化Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FreshPalDB freshPalDB;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // set the status bar to light color (black text)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // 初始化视图和Firebase Auth
        initializeViews();

        // 初始化 Room 数据库和 UserDao
        initializeDatabase();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Register.class));
            }
        });

        forgetTextLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordResetDialog();
            }
        });
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailforsignup);
        passwordEditText = findViewById(R.id.passwordForSignUp);
        loginButton = findViewById(R.id.signUpButton);
        registerButton = findViewById(R.id.createNewAccountHint);
        forgetTextLink = findViewById(R.id.forgetPassword);
        fAuth = FirebaseAuth.getInstance();
    }

    private void initializeDatabase() {
        freshPalDB = FreshPalDB.getDatabase(getApplicationContext());
        userDao = freshPalDB.userDao();
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.email_required));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.password_required));
            return;
        }

        // 登录
        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = fAuth.getCurrentUser();
                    if (user.isEmailVerified()) {
                        handleLoginSuccess(user);
                    } else {
                        Toast.makeText(Login.this, getString(R.string.email_not_verified), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Login.this, getString(R.string.login_error) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void handleLoginSuccess(FirebaseUser user) {
//        Toast.makeText(Login.this, getString(R.string.login_complete), Toast.LENGTH_SHORT).show();

        // 在登录成功后，检查本地数据库中是否已存在具有相同用户ID的用户信息
        // 如果不存在且云端数据中没有，则将用户信息异步插入到本地数据库中
        // 如果不存在且云端数据库中有该用户，则需要将云端数据库与本地数据库同步
        new Thread(new Runnable() {
            @Override
            public void run() {
                //存在但是email未经过加密
                userId = user.getUid();
                if (isUserInLocalDatabase(user.getUid()) && userDao.getUserById(user.getUid()).getEmail().equals(user.getEmail())) {
                    // 更新email为加密后的
                    String encryptedEmail = EmailEncryptor.encryptEmail(user.getEmail(), Login.this);
                    userDao.updateEmail(user.getUid(), encryptedEmail);
                }
                //check firestore
                if (!isUserInLocalDatabase(user.getUid()) ) {
                    db.collection(userId).limit(1).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                syncDataFromFirestoreToLocal(userId);
                                Log.d("LOGIN", "run: success");
                            } else {
                                User newUser = new User();
                                newUser.setUserID(user.getUid());
                                newUser.setUsername(user.getDisplayName());
//                    newUser.setEmail(user.getEmail());
                                // Encrypt the user's email before storing it in the database
                                String encryptedEmail = EmailEncryptor.encryptEmail(user.getEmail(), Login.this);
                                newUser.setEmail(encryptedEmail);
                                newUser.setReminderEnabled(false);
                                newUser.setDefaultReminderTime(3);
                                newUser.setDefaultOpenExpireTime(3);
                                userDao.insertUser(newUser);
                            }
                        } else {
                            Log.d("LOGIN", "run: nothing");
                        }
                    });
                }
            }
        }).start();

        Intent mainIntent = new Intent(getApplicationContext(), MainPage.class);
        mainIntent.putExtra("userID", userId);
        startActivity(mainIntent);
    }

    // 检查本地数据库中是否已存在具有相同用户ID的用户信息
    private boolean isUserInLocalDatabase(String uid) {
        User user = userDao.getUserById(uid);
        return user != null;
    }

    private void showPasswordResetDialog() {

        EditText resetMail = new EditText(this);
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle(getString(R.string.reset_password));
        passwordResetDialog.setMessage(getString(R.string.enter_email_for_reset));
        passwordResetDialog.setView(resetMail);

        passwordResetDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 提交密码重置请求
                String mail = resetMail.getText().toString();
                fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Login.this, getString(R.string.reset_link_sent), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this, getString(R.string.reset_link_not_sent) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        passwordResetDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 关闭对话框
            }
        });
        passwordResetDialog.create().show();
    }

    private void syncDataFromFirestoreToLocal(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userSettingRef = db.collection(userId).document("user_setting");
        DocumentReference foodInformationRef = db.collection(userId).document("food_information");

        // 从Firestore拉取 user_setting 文档
        userSettingRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    // 使用后台线程更新本地数据库中的用户数据
                    new Thread(() -> {
                        user.setUserID(userId);
                        userDao.insertUser(user);
                    }).start();
                }
            }
        }).addOnFailureListener(e -> {
            Log.d("LOGIN", "syncDataFromFirestoreToLocal: " + e);
        });

        // 从Firestore拉取 food_information 文档
        foodInformationRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> foodDataList = (List<Map<String, Object>>) documentSnapshot.get("food_collection");
                if (foodDataList != null) {
                    // 使用foodDataList进行后续的操作，例如转换为Food对象并保存到本地数据库
                    syncFoodDataFromFirestoreToLocal(foodDataList, userId);
                }
            }
        }).addOnFailureListener(e -> {
            // 处理失败情况，例如显示错误消息
        });
    }

    private void syncFoodDataFromFirestoreToLocal(List<Map<String, Object>> foodDataList, String userId) {
        List<Food> foodsToInsert = new ArrayList<>();

        for (Map<String, Object> foodData : foodDataList) {
            Food food = new Food();

            // 设置Food对象的属性
            food.setUserID(userId);
            food.setFoodName((String) foodData.get("foodName"));
            food.setQuantity(((Number) foodData.get("quantity")).doubleValue());
            food.setOpened((Boolean) foodData.get("isOpened"));
            food.setCategory((String) foodData.get("category"));
            food.setAddTime(((Number) foodData.get("addTime")).longValue());
            food.setProductionDate(((Number) foodData.get("productionDate")).longValue());
            food.setExpireTime(((Number) foodData.get("expireTime")).intValue());
            food.setBestBefore(((Number) foodData.get("bestBefore")).longValue());
            food.setImageURL((String) foodData.get("imageURL"));
            food.setStorageCondition((String) foodData.get("storageCondition"));
            food.setStorageLocation((String) foodData.get("storageLocation"));

            foodsToInsert.add(food);
        }

        // 使用后台线程插入到本地数据库中
        new Thread(() -> {
            for (Food food : foodsToInsert) {
                freshPalDB.foodDao().insert(food);
            }
        }).start();
    }



}
