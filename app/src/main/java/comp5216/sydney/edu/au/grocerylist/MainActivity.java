package comp5216.sydney.edu.au.grocerylist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addItemEditText = findViewById(R.id.editTextItem);
        recyclerView = findViewById(R.id.recyclerView);
        datePicker = findViewById(R.id.datePicker);

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
}
