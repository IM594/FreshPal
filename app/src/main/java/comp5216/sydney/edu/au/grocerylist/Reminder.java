package comp5216.sydney.edu.au.grocerylist;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.Toolbar;



public class Reminder extends AppCompatActivity {
    Spinner spinner;
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_reminder);

        //Toolbar toolbar =(Toolbar)findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(ture);
        spinner= findViewById(R.id.spinner);
    }

}
