package comp5216.sydney.edu.au.grocerylist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends AppCompatActivity {
    private FirebaseAuth fAuth;
    private String email;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_profile);

//        // set the status bar to dark color (white text)
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);
//
//        //设置status bar的颜色
//        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViewParams();

        TextView userEmailTextView = findViewById(R.id.user_email);
//        userEmailTextView.setText(email);
        // Decrypt and set the email
//        email = EmailEncryptor.decryptEmail(encryptedEmail, this);
        userEmailTextView.setText(email);
        Toast.makeText(this, "email: " + email, Toast.LENGTH_SHORT).show();
        CardView changePasswordLayout = findViewById(R.id.change_password_card);
        changePasswordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordResetDialog();
            }
        });
        CardView SettingsLayout = findViewById(R.id.settings_card);
        SettingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Settings.class));
                finish();
            }
        });
        LinearLayout profileAddButton = findViewById(R.id.add_nav);
        LinearLayout profileHomeButton = findViewById(R.id.home_nav);

        profileAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AddFood activity
                Intent intent = new Intent(Profile.this, AddFood.class);
                startActivity(intent);
            }
        });

        profileHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to MainActivity
                Intent intent = new Intent(Profile.this, MainPage.class);
                startActivity(intent);
            }
        });
    }
    private void initViewParams(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();
        fAuth = FirebaseAuth.getInstance();
//        Toast.makeText(this, "encryptedEmail: " + encryptedEmail, Toast.LENGTH_SHORT).show();
    }
    private void showPasswordResetDialog() {

        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle(getString(R.string.reset_password));
        passwordResetDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 提交密码重置请求
                fAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Profile.this, getString(R.string.reset_link_sent), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Profile.this, getString(R.string.reset_link_not_sent) + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    public void logOutforProfile(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }




}
