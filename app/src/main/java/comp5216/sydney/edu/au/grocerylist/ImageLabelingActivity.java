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
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ImageLabelingActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView imageView;
    private TextView resultTextView;
    private Button takePhotoButton;

    private ImageLabeler imageLabeler;

    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_labeling);

        // set the status bar to light color (black text)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);
        takePhotoButton = findViewById(R.id.takePhotoButton);

        // 初始化图像标签器
        imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        // 如果存在相机权限，则直接启动相机应用
        if (marshmallowPermission.checkPermissionForCamera()) {
            dispatchTakePictureIntent();
        }

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
    }

    // 启动相机应用以拍摄照片
    private void dispatchTakePictureIntent() {

        // 如果没有权限（相机权限或者位置权限），则请求权限
        if (!marshmallowPermission.checkPermissionForCamera()) {
            marshmallowPermission.requestPermissionForCamera();
        } else {

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // 处理拍摄的照片
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            // 将拍摄的照片转换为 InputImage
            InputImage image = InputImage.fromBitmap(imageBitmap, 0);

            // 使用图像标签器识别标签
            imageLabeler.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(List<ImageLabel> labels) {
                            // 处理识别结果
                            StringBuilder resultText = new StringBuilder();
                            List<String> topLabels = new ArrayList<>();

                            // 将标签按置信度降序排列
                            Collections.sort(labels, new Comparator<ImageLabel>() {
                                @Override
                                public int compare(ImageLabel label1, ImageLabel label2) {
                                    return Float.compare(label2.getConfidence(), label1.getConfidence());
                                }
                            });

                            // 获取置信度最高的三个标签，并将它们添加到resultText和topLabels列表中
                            for (int i = 0; i < Math.min(3, labels.size()); i++) {
                                ImageLabel label = labels.get(i);
                                String text = label.getText();
                                float confidence = label.getConfidence();
                                resultText.append(text).append(": ").append(confidence).append("\n");
                                topLabels.add(text);
                            }

                            resultTextView.setText(resultText.toString());

                            // 创建一个对话框显示置信度最高的三个标签供用户选择
                            AlertDialog.Builder builder = new AlertDialog.Builder(ImageLabelingActivity.this);
                            builder.setTitle("Choose a category");
                            final CharSequence[] charSequenceLabels = topLabels.toArray(new CharSequence[0]);

                            builder.setItems(charSequenceLabels, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // 用户选择了一个标签，将其填入categoryInput
                                    String selectedLabel = charSequenceLabels[which].toString();
//                                    categoryInput.setText(selectedLabel);
                                    //填入文本框
                                    Intent intent = new Intent();
                                    intent.putExtra("category", selectedLabel);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                    //然后关闭activity，返回上一个activity，即AddFood，然后在AddFood中将选择的文本填入categoryInput
                                }

                            });

                            // 如果没有一个标签被识别出来，则显示一个对话框提示用户，并让用户选择是否重新识别还是手动输入
                            if (labels.size() == 0) {
                                builder.setMessage("No label detected. Do you want to try again or enter manually?");
                                builder.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dispatchTakePictureIntent();
                                    }
                                });
                                builder.setNegativeButton("Enter manually", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // 打开手动输入界面
//                                        categoryInput.setText("");
                                        //填入文本框
                                        Intent intent = new Intent();
                                        intent.putExtra("category", "");
                                        setResult(RESULT_CANCELED, intent);
                                        finish();
                                        //然后关闭activity，返回上一个activity，即AddFood，然后在AddFood中将选择的文本填入categoryInput
                                    }
                                });
                            }

                            // 增加手动输入按钮，点击后打开手动输入界面
                            builder.setNeutralButton("Enter manually", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // 打开手动输入界面
//                                    categoryInput.setText("");
                                    //填入文本框
                                    Intent intent = new Intent();
                                    intent.putExtra("category", "");
                                    setResult(RESULT_CANCELED, intent);
                                    finish();
                                    //然后关闭activity，返回上一个activity，即AddFood，然后在AddFood中将选择的文本填入categoryInput
                                }
                            });

                            // 重新识别按钮，点击后重新识别
                            builder.setNegativeButton("Try again", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dispatchTakePictureIntent();
                                }
                            });

                            builder.create().show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 处理识别失败的情况
                        Toast.makeText(this, "标签识别失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        }
    }
}
