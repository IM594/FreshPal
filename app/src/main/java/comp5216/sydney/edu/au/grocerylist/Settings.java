package comp5216.sydney.edu.au.grocerylist;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.sql.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import comp5216.sydney.edu.au.grocerylist.data.database.FreshPalDB;
import comp5216.sydney.edu.au.grocerylist.data.entities.User;

public class Settings extends AppCompatActivity {

    private FreshPalDB db;
    private FirebaseAuth fAuth;
    private String User_id;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);

        // set the status bar to light color (black text)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        db = FreshPalDB.getDatabase(getApplicationContext());

        ImageButton backwardImageView = findViewById(R.id.iv_backward);
        backwardImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, Profile.class);
                startActivity(intent);
            }
        });

        fAuth = FirebaseAuth.getInstance();
        FirebaseUser user_F = fAuth.getCurrentUser();
        User_id = user_F.getUid();

        Spinner timeZoneSpinner = findViewById(R.id.timezonelist);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.australian_time_zones,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeZoneSpinner.setAdapter(adapter);
        LinearLayout imageButtonHome = findViewById(R.id.home_nav);
        LinearLayout imageButtonAdd = findViewById(R.id.add_nav);

        imageButtonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to MainActivity
                Intent intent = new Intent(Settings.this, MainPage.class);
                startActivity(intent);
            }
        });

        imageButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AddFood activity
                Intent intent = new Intent(Settings.this, AddFood.class);
                startActivity(intent);
            }
        });
        initializeUI();
    }

    private void initializeUI() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final User user = db.userDao().getUserById(User_id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SwitchCompat enableReminderSwitch = findViewById(R.id.enable_reminder);
                        EditText defaultReminderTimeEditText = findViewById(R.id.default_reminder_time);
                        EditText defaultOpenExpireTimeEditText = findViewById(R.id.default_Open_Expire_Time);

                        enableReminderSwitch.setChecked(user.isReminderEnabled());
                        defaultReminderTimeEditText.setText(String.valueOf(user.getDefaultReminderTime()));
                        defaultOpenExpireTimeEditText.setText(String.valueOf(user.getDefaultOpenExpireTime()));

                        setupListeners();
                        updateLastSyncTime(user.getLastSyncTime());

                    }
                });
            }
        });
    }

    private void setupListeners() {
        SwitchCompat enableReminderSwitch = findViewById(R.id.enable_reminder);
        EditText defaultReminderTimeEditText = findViewById(R.id.default_reminder_time);
        EditText defaultOpenExpireTimeEditText = findViewById(R.id.default_Open_Expire_Time);

        enableReminderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateUserReminderEnabled(isChecked);
            }
        });

        defaultReminderTimeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    long defaultReminderTime = Long.parseLong(s.toString());
                    updateDefaultReminderTime(defaultReminderTime);
                } catch (NumberFormatException e) {
                    // Handle invalid input
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        defaultOpenExpireTimeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int defaultOpenExpireTime = Integer.parseInt(s.toString());
                    updateDefaultOpenExpireTime(defaultOpenExpireTime);
                } catch (NumberFormatException e) {
                    // Handle invalid input
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public void updateUserReminderEnabled(final boolean reminderEnabled) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                User user = db.userDao().getUserById(User_id);
                user.setReminderEnabled(reminderEnabled);
                db.userDao().updateUser(user);
            }
        });
    }

    public void updateDefaultReminderTime(final long defaultReminderTime) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                User user = db.userDao().getUserById(User_id);
                user.setDefaultReminderTime(defaultReminderTime);
                db.userDao().updateUser(user);
            }
        });
    }

    public void updateDefaultOpenExpireTime(final int defaultOpenExpireTime) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                User user = db.userDao().getUserById(User_id);
                user.setDefaultOpenExpireTime(defaultOpenExpireTime);
                db.userDao().updateUser(user);
            }
        });
    }
    private void updateLastSyncTime(long lastSyncTime) {
        TextView lastSyncTimeTextView = findViewById(R.id.lastsynctime);
        if (lastSyncTime == 0) {
            lastSyncTimeTextView.setText("Last Sync Time: null");
        } else {
            Date date = new Date(lastSyncTime);  // Unix时间戳
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = sdf.format(date);
            lastSyncTimeTextView.setText(String.format(Locale.getDefault(), "Last Sync Time: %s", formattedDate));
        }
    }
}
