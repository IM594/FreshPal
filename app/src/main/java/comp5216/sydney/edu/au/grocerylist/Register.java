package comp5216.sydney.edu.au.grocerylist;

// 导入必要的包
import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import comp5216.sydney.edu.au.grocerylist.data.database.FreshPalDB;
import comp5216.sydney.edu.au.grocerylist.data.entities.User;
import comp5216.sydney.edu.au.grocerylist.data.dao.UserDao;

public class Register extends AppCompatActivity {
    EditText mFullName, mEmail, mPasssword, mPasswordCheck;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    private FreshPalDB freshPalDB;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.emailforsignup);
        mPasssword = findViewById(R.id.passwordForSignUp);
        mPasswordCheck = findViewById(R.id.passwordCheck);
        mRegisterBtn = findViewById(R.id.signUpButton);
        mLoginBtn = findViewById(R.id.tapToSignup);
        fAuth = FirebaseAuth.getInstance();

        // 初始化 Room 数据库和 UserDao
        freshPalDB = FreshPalDB.getDatabase(getApplicationContext());
        userDao = freshPalDB.userDao();

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mRegisterBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPasssword.getText().toString().trim();
                String passwordCheck = mPasswordCheck.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }
                if(!password.equals(passwordCheck)){
                    mPasswordCheck.setError("Your password is not same with before.");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mPasssword.setError("Password is Required.");
                    return;
                }
                if(password.length() < 6){
                    mPasssword.setError("Password must be more than 6 characters");
                    return;
                }

                // 注册用户到 Firebase Authentication
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // 发送验证链接
                            FirebaseUser user = fAuth.getCurrentUser();
                            user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(Register.this, "Verification Email has been Sent.",Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Email not sent "+e.getMessage());
                                }
                            });
                            Toast.makeText(Register.this, "Sign up complete.", Toast.LENGTH_SHORT).show();

                            // 创建 User 对象并插入到本地数据库
                            User localUser = new User();
                            localUser.setUserID(user.getUid()); // 使用 Firebase 用户ID
                            localUser.setUsername(mFullName.getText().toString()); // 使用 mFullName 中的文本作为用户名
                            localUser.setEmail(email);
                            localUser.setReminderEnabled(false); // 设置其他字段


                            // 异步插入 User 对象到本地数据库
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    userDao.insertUser(localUser);
                                }
                            }).start();

                            startActivity(new Intent(getApplicationContext(), Login.class));
                        } else {
                            Toast.makeText(Register.this, "Error!: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });
    }
}
