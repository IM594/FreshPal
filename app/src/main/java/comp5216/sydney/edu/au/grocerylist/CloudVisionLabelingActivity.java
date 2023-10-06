//package comp5216.sydney.edu.au.grocerylist;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.provider.MediaStore;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.gms.tasks.Tasks;
//import com.google.firebase.functions.FirebaseFunctions;
//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonPrimitive;
//import com.google.gson.JsonParser;
//import com.google.gson.JsonElement;
//import java.io.ByteArrayOutputStream;
//import java.util.Objects;
//import java.util.concurrent.Callable;
//import com.google.android.gms.tasks.Task;
//
//public class CloudVisionLabelingActivity extends AppCompatActivity {
//    private static final int REQUEST_IMAGE_CAPTURE = 1;
//    private ImageView imageView;
//    private TextView resultTextView;
//    private Button takePhotoButton;
//    private FirebaseFunctions functions;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_cloud_vision_labeling);
//
//        imageView = findViewById(R.id.imageView);
//        resultTextView = findViewById(R.id.resultTextView);
//        takePhotoButton = findViewById(R.id.takePhotoButton);
//
//        // 初始化 Firebase Functions 实例
//        functions = FirebaseFunctions.getInstance();
//
//        takePhotoButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dispatchTakePictureIntent();
//            }
//        });
//    }
//
//    // 启动相机应用以拍摄照片
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//    }
//
//    // 处理拍摄的照片
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = Objects.requireNonNull(data).getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            imageView.setImageBitmap(imageBitmap);
//
//            // 将拍摄的照片转换为 base64 编码字符串
//            String base64encoded = bitmapToBase64(imageBitmap);
//
//            // 创建 JSON 请求并调用 Cloud Vision API
//            JsonObject request = new JsonObject();
//            JsonObject image = new JsonObject();
//            image.add("content", new JsonPrimitive(base64encoded));
//            request.add("image", image);
//            JsonArray features = new JsonArray();
//            JsonObject feature = new JsonObject();
//            feature.add("maxResults", new JsonPrimitive(5));
//            feature.add("type", new JsonPrimitive("LABEL_DETECTION"));
//            features.add(feature);
//            request.add("features", features);
//
//            annotateImage(request.toString()).addOnCompleteListener(task -> {
//                if (!task.isSuccessful()) {
//                    // 处理任务失败
//                    Toast.makeText(this, "标签识别失败：" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                } else {
//                    // 处理任务成功
//                    JsonElement result = task.getResult();
//                    processImageLabels(result);
//                }
//            });
//        }
//    }
//
//    // 将位图转换为 base64 编码字符串
//    private String bitmapToBase64(Bitmap bitmap) {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//        byte[] imageBytes = byteArrayOutputStream.toByteArray();
//        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP);
//    }
//
////    // 调用 Cloud Vision API 的 Callable 函数
////    private Task<JsonElement> annotateImage(String requestJson) {
////        Callable<JsonElement> callable = () -> {
////            // 这里将要调用的 Cloud Functions 函数名称替换成你的函数名称
////        };
////
////        return Tasks.call(callable);
////    }
//
//    // 处理 Cloud Vision API 的标签识别结果
//    private void processImageLabels(JsonElement result) {
//        // 处理标签识别结果
//        StringBuilder resultText = new StringBuilder();
//        JsonArray labelAnnotations = result.getAsJsonArray().get(0).getAsJsonObject().get("labelAnnotations").getAsJsonArray();
//
//        for (JsonElement labelElement : labelAnnotations) {
//            JsonObject labelObject = labelElement.getAsJsonObject();
//            String text = labelObject.get("description").getAsString();
//            float confidence = labelObject.get("score").getAsFloat();
//            resultText.append(text).append(": ").append(confidence).append("\n");
//        }
//
//        resultTextView.setText(resultText.toString());
//    }
//}
