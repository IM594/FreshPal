package comp5216.sydney.edu.au.grocerylist;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

        // set the status bar to light color (black text)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

        imageView = findViewById(R.id.imageView);
        captureImageButton = findViewById(R.id.captureImageButton);
        processImageButton = findViewById(R.id.processImageButton);
        resultTextView = findViewById(R.id.resultTextView);

        // 如果存在相机权限，则直接启动相机应用
        if (marshmallowPermission.checkPermissionForCamera()) {
            dispatchTakePictureIntent();
        }

        // 拍摄照片
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        // 处理拍摄的照片
        processImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processImageForText();
            }
        });
    }

    // 启动相机应用以拍摄照片
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) { // 检查是否有相机应用
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
            //直接调用图像处理
            processImageForText();
        }
    }

    // 处理拍摄的照片
    private void processImageForText() {
        if (imageView.getDrawable() == null) {
            Toast.makeText(this, "Please capture an image first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取原始图像
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap originalBitmap = drawable.getBitmap();

        InputImage image = InputImage.fromBitmap(originalBitmap, 0);

        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        String extractedText = visionText.getText();
                        resultTextView.setText(extractedText);

                        // 从提取的文本中提取日期
                        List<String> extractedDates = extractDates(extractedText);

                        // 如果没有提取到日期，则提示用户，自动返回上一个activity，返回一个提示码，然后在上一个activity中自动点击日期按钮
                        if (extractedDates.size() == 0) {
                            Toast.makeText(OCRImageProcessingActivity.this, "No date found in the image.", Toast.LENGTH_SHORT).show();
                            //询问用户是否重新拍照，还是自己手动输入日期。如果要手动输入，自动返回上一个activity，返回一个提示码，然后在上一个activity中自动点击日期按钮
                            AlertDialog.Builder builder = new AlertDialog.Builder(OCRImageProcessingActivity.this);
                            builder.setTitle("No date found in the image.");
                            builder.setMessage("Do you want to take another photo or enter the date manually?");
                            builder.setPositiveButton("Take another photo", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dispatchTakePictureIntent();
                                }
                            });
                            builder.setNegativeButton("Enter manually", new DialogInterface.OnClickListener() {//返回一个提示码，然后在上一个activity中自动点击日期按钮
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("extractedDate", "manual");
                                    setResult(RESULT_CANCELED, resultIntent);
                                    finish();
                                }
                            });
                            builder.show();

                        } else if (extractedDates.size() == 1) {
                            // 如果只提取到一个日期，则直接返回该日期
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("extractedDate", extractedDates.get(0));
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            // 让用户选择正确的日期
                            showDateOptions(extractedDates);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Text recognition failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    // 定义日期正则表达式和对应的日期格式
    private static final Map<String, String> DATE_FORMATS = new HashMap<>();

    static {
        DATE_FORMATS.put("\\d{1,2}/\\d{1,2}/\\d{4}", "dd/MM/yyyy"); // 格式：01/02/2023
        DATE_FORMATS.put("\\d{1,2}-\\d{1,2}-\\d{4}", "dd-MM-yyyy"); // 格式：01-02-2023
        DATE_FORMATS.put("\\d{1,2} \\w{3} \\d{4}", "dd MMM yyyy"); // 格式：01 Jan 2023
        DATE_FORMATS.put("\\d{4}-\\d{1,2}-\\d{1,2}", "yyyy-MM-dd"); // 格式：2023-01-02
        DATE_FORMATS.put("\\w{3} \\d{1,2}, \\d{4}", "MMM dd, yyyy"); // 格式：Jan 01, 2023
        DATE_FORMATS.put("\\d{1,2}\\.\\d{1,2}\\.\\d{4}", "dd.MM.yyyy"); // 格式：01.02.2023
        DATE_FORMATS.put("\\w{3} \\d{1,2}-\\d{1,2} \\d{2}", "MMM dd-yyyy HH"); // 格式：Jan 01-02 23
        DATE_FORMATS.put("\\d{4}\\.\\d{2}\\.\\d{2}", "yyyy.MM.dd"); // 格式：2023.01.02
        DATE_FORMATS.put("\\d{4}\\.\\d{1,2}\\.\\d{1,2}", "yyyy.M.d"); // 格式：2023.1.2
        DATE_FORMATS.put("\\d{1,2}/\\w{3}/\\d{4}", "dd/MMM/yyyy"); // 格式：01/Jan/2023
        DATE_FORMATS.put("\\d{4}/\\d{1,2}/\\d{1,2}", "yyyy/M/d"); // 格式：2023/1/2
        DATE_FORMATS.put("\\d{4}/\\d{2}/\\d{1,2}", "yyyy/MM/d"); // 格式：2023/01/2
        DATE_FORMATS.put("\\d{8}", "yyyyMMdd"); // 格式：20200523
        DATE_FORMATS.put("\\d{2}/\\d{2}/\\d{2}", "dd/MM/yy"); // 格式：11/08/24（代表2023年8月11日）
    }

    ;

    // 从提取的文本中提取日期
    private List<String> extractDates(String text) {
        List<String> extractedDates = new ArrayList<>();

        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");

        // 通过正则表达式提取日期
        for (Map.Entry<String, String> entry : DATE_FORMATS.entrySet()) {
            String regex = entry.getKey();
            String dateFormat = entry.getValue();

            Pattern pattern = Pattern.compile("\\b" + regex + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                String dateString = matcher.group();
                try {
                    // 截取日期的有效部分
                    String validDate = getValidDate(dateString);
                    if (validDate != null) {
                        // 将匹配的日期字符串转换为标准的日期对象
                        Date date = parseDate(validDate, dateFormat);
                        if (date != null) {
                            // 如果已经存在了同样的内容，则不添加
                            if (!extractedDates.contains(outputFormat.format(date))) {
                                extractedDates.add(outputFormat.format(date));
                            }
                        }
                    }
                } catch (ParseException e) {
                    // 日期格式无效，忽略
                }
            }
        }

        return extractedDates;
    }

    // 将不同格式的日期转换为统一的格式
    private Date parseDate(String dateString, String format) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false); // 禁用宽松模式以提高日期解析的准确性
        return dateFormat.parse(dateString);
    }

    // 从日期字符串中提取有效日期部分
    private String getValidDate(String dateString) {
        // 此数组包含不同日期正则表达式的格式
        String[] dateFormats = DATE_FORMATS.keySet().toArray(new String[0]);

        for (String format : dateFormats) {
            Pattern pattern = Pattern.compile(format);
            Matcher matcher = pattern.matcher(dateString);

            if (matcher.find()) {
                String validDate = matcher.group();
                return validDate;
            }
        }

        return null; // 未找到有效日期部分
    }


    // 给用户选项，让用户选择正确的日期
    private void showDateOptions(List<String> dates) {
        // 在这里，你可以使用日期选择对话框或自定义对话框来显示日期选项并让用户选择。
        // 这里只是示例代码，你需要根据你的UI设计和需求来实现。
        // 假设你使用 AlertDialog 来实现：

        CharSequence[] dateOptions = dates.toArray(new CharSequence[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please Select the Right Date");
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

        //如果没有一个是正确的，询问用户是否手动输入日期
        builder.setNegativeButton("Enter manually", new DialogInterface.OnClickListener() {//返回一个提示码，然后在上一个activity中自动点击日期按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("extractedDate", "manual");
                setResult(RESULT_CANCELED, resultIntent);
                finish();
            }
        });
        builder.show();
    }


}
