package comp5216.sydney.edu.au.grocerylist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import comp5216.sydney.edu.au.grocerylist.data.dao.UserDao;
import comp5216.sydney.edu.au.grocerylist.data.database.FreshPalDB;
import comp5216.sydney.edu.au.grocerylist.data.entities.User;

public class Login extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView registerButton, forgetTextLink;
    FirebaseAuth fAuth;

    private FreshPalDB freshPalDB;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        Toast.makeText(Login.this, getString(R.string.login_complete), Toast.LENGTH_SHORT).show();

        // 在登录成功后，检查本地数据库中是否已存在具有相同用户ID的用户信息
        // 如果不存在，则将用户信息异步插入到本地数据库中
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isUserInLocalDatabase(user.getUid())) {
                    User newUser = new User();
                    newUser.setUserID(user.getUid());
                    newUser.setUsername(user.getDisplayName());
                    newUser.setEmail(user.getEmail());
                    newUser.setReminderEnabled(false);
                    newUser.setDefaultReminderTime(0);
                    newUser.setDefaultOpenExpireTime(0);
                    userDao.insertUser(newUser);
                }
            }
        }).start();

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
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
}
