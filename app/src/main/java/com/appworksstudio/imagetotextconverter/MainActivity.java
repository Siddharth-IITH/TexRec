package com.appworksstudio.imagetotextconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;



public class MainActivity extends AppCompatActivity {
    String final_out= "";
    static final int REQUEST_IMAGE_CAPTURE = 671;
    private static final String LOG_TAG = "trec";
    private ImageView img;
    TextView texrecd ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        texrecd = findViewById(R.id.texrec_tv);
        Button imgCapture = findViewById(R.id.img_capture);
        Button imgDetect = findViewById(R.id.img_detect);
        img = findViewById(R.id.img);


        imgCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              dispatchTakePictureIntent();


            }
        });




    }

    private void detection(Bitmap img){
        TextRecognizer recognizer = TextRecognition.getClient();

        InputImage image = InputImage.fromBitmap(img,0);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    String blockText = block.getText();
                                    final_out+=blockText;
                                }
                                Toast.makeText(MainActivity.this, "Success"+ final_out, Toast.LENGTH_SHORT).show();
                                texrecd.setText(final_out);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Toast.makeText(MainActivity.this, "ailure"+e, Toast.LENGTH_LONG).show();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            img.setImageBitmap(imageBitmap);
            texrecd.setText("");
            detection(imageBitmap);
        }
    }
}
