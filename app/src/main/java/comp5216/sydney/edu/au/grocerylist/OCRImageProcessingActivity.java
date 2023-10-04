package comp5216.sydney.edu.au.grocerylist;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCRImageProcessingActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_OCR_PROCESSING = 2;

    private ImageView imageView;
    private Button captureImageButton;
    private Button processImageButton;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocrimage_processing);

        imageView = findViewById(R.id.imageView);
        captureImageButton = findViewById(R.id.captureImageButton);
        processImageButton = findViewById(R.id.processImageButton);
        resultTextView = findViewById(R.id.resultTextView);

        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        processImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processImageForText();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }

    private void processImageForText() {
        if (imageView.getDrawable() == null) {
            Toast.makeText(this, "Please capture an image first.", Toast.LENGTH_SHORT).show();
            return;
        }

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        String extractedText = visionText.getText();
                        resultTextView.setText(extractedText);

                        // 从提取的文本中提取日期
                        List<String> extractedDates = extractDates(extractedText);

                        // 让用户选择正确的日期
                        showDateOptions(extractedDates);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Text recognition failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 从提取的文本中提取日期
    private List<String> extractDates(String text) {
        List<String> extractedDates = new ArrayList<>();
        Pattern datePattern = Pattern.compile("\\b\\d{1,2}[/ -]\\d{1,2}[/ -]\\d{4}\\b", Pattern.CASE_INSENSITIVE);

        Matcher matcher = datePattern.matcher(text);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        while (matcher.find()) {
            String dateString = matcher.group();
            try {
                // 将不同格式的日期转换为统一的格式
                Date date = parseDate(dateString);
                if (date != null) {
                    extractedDates.add(dateFormat.format(date));
                }
            } catch (ParseException e) {
                // 日期格式无效，忽略
            }
        }

        return extractedDates;
    }

    // 将不同格式的日期转换为统一的格式
    private Date parseDate(String dateString) throws ParseException {
        SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("dd/MM/yyyy"),
                new SimpleDateFormat("dd MMM yyyy"),
                new SimpleDateFormat("MMM dd yyyy"),
                new SimpleDateFormat("MMM-yyyy"),
                new SimpleDateFormat("MMM- yyyy"),
                new SimpleDateFormat("MMM yyyy")
        };

        for (SimpleDateFormat format : dateFormats) {
            try {
                format.setLenient(false); // 禁用宽松模式以提高日期解析的准确性
                return format.parse(dateString);
            } catch (ParseException ignored) {
                // 尝试下一个日期格式
            }
        }

        return null; // 无法解析日期
    }


    // 给用户三个选项，让用户选择正确的日期
    private void showDateOptions(List<String> dates) {
        // 在这里，你可以使用日期选择对话框或自定义对话框来显示日期选项并让用户选择。
        // 这里只是示例代码，你需要根据你的UI设计和需求来实现。
        // 假设你使用 AlertDialog 来实现：

        CharSequence[] dateOptions = dates.toArray(new CharSequence[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Date");
        builder.setItems(dateOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户选择了日期，将其返回到AddFood
                String selectedDate = dates.get(which);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("extractedDate", selectedDate);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        builder.show();
    }


}
