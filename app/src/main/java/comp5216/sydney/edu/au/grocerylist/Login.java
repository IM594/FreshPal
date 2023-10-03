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
    EditText  mEmail, mPasssword;
    Button mLoginBtn;
    TextView mRegisterBtn, forgetTextLink;
    FirebaseAuth fAuth;

    private FreshPalDB freshPalDB;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmail = findViewById(R.id.emailforsignup);
        mPasssword = findViewById(R.id.passwordForSignUp);
        fAuth = FirebaseAuth.getInstance();
        mLoginBtn = findViewById(R.id.signUpButton);
        mRegisterBtn = findViewById(R.id.createNewAccountHint);
        forgetTextLink = findViewById(R.id.forgetPassword);

        // 初始化 Room 数据库和 UserDao
        freshPalDB = FreshPalDB.getDatabase(getApplicationContext());
        userDao = freshPalDB.userDao();

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                String password = mPasssword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mPasssword.setError("Password is Required.");
                    return;
                }

                // 登录
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = fAuth.getCurrentUser();
                            if (user.isEmailVerified()) {
                                Toast.makeText(Login.this, "Login complete.", Toast.LENGTH_SHORT).show();

                                // 在登录成功后，检查本地数据库中是否已存在具有相同用户ID的用户信息
                                // 如果不存在，则将用户信息异步插入到本地数据库中
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isUserInLocalDatabase(user.getUid())) {
                                            User newUser = new User();
                                            newUser.setUserID(user.getUid());
                                            newUser.setUsername(user.getDisplayName());//由于firebase的authentication没有存userName的地方，所以这里会是空
                                            newUser.setEmail(user.getEmail());
                                            newUser.setReminderEnabled(false);
                                            newUser.setDefaultReminderTime(0);
                                            newUser.setDefaultOpenExpireTime(0);
                                            userDao.insertUser(newUser);
                                        }
                                    }
                                }).start();

                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } else {
                                Toast.makeText(Login.this, "Your email is not verified.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Login.this, "Error!: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    // 检查本地数据库中是否已存在具有相同用户ID的用户信息
                    private boolean isUserInLocalDatabase(String uid) {
                        final User[] user = {null};
                        // 在本地数据库中查找具有相同用户ID的用户信，异步
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                user[0] = userDao.getUserById(uid);
                            }
                        }).start();
                        // 如果找到了，则返回true
                        if (user[0] != null) {
                            return true;
                        }
                        // 如果没有找到，则返回false
                        return false;
                    }
                });

            }
        });
        //login
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Register.class));
            }
        });
        //reset password
        forgetTextLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetMail = new EditText(view.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Reset Password ?");
                passwordResetDialog.setMessage("Enter your Email to receive Reset Link");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //extract email
                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Login.this, "Reset Link sent to your email.", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Reset Link is not sent due to "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                         //close the dialog
                    }
                });
                passwordResetDialog.create().show();
            }
        });
    }
}