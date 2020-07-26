package com.appworksstudio.imagetotextconverter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;


public class MainActivity extends AppCompatActivity {
    String final_out = "";
    static final int REQUEST_IMAGE_CAPTURE = 671;
    static final int REQUEST_IMAGE_CAPTURE2 = 6721;
    private static final String LOG_TAG = "trec";
    private ImageView img, img2;
    TextView texrecd;

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
                texrecd.setText(R.string.texrec_here);
                final_out = "";
                dispatchTakePictureIntent();


            }
        });
        imgDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.invalidate();
                BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                detection(bitmap);

            }
        });

        FloatingActionButton copyText =
                findViewById(R.id.copy_btn);
        copyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("TexRecd data", texrecd.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Text copied to clipboard!!", Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void detection(Bitmap img) {
        TextRecognizer recognizer = TextRecognition.getClient();

        InputImage image = InputImage.fromBitmap(img, 0);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    String blockText = block.getText();
                                    final_out += blockText + "\n";
                                }
                                Toast.makeText(MainActivity.this, "Success" + final_out, Toast.LENGTH_SHORT).show();
                                texrecd.setText(final_out);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Toast.makeText(MainActivity.this, "failure" + e, Toast.LENGTH_LONG).show();
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
            //to get the image from the ImageView (say iv)
            BitmapDrawable draw = (BitmapDrawable) img.getDrawable();
            Bitmap bitmap = draw.getBitmap();

//            FileOutputStream outStream = null;
//            File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); //context.getExternalFilesDir(null);
//
//
//
////            String fileName = String.format("%d.jpg", System.currentTimeMillis());
//            String fileName = "omk";
//            File file = new File(storageLoc, fileName + ".jpg");
//            //File outFile = new File(dir, fileName);
//            try {
//                outStream = new FileOutputStream(file);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
//                outStream.flush();
//                outStream.close();
//                Toast.makeText(this, "Successfully image stored", Toast.LENGTH_SHORT).show();
//            } catch (IOException e) {
//                e.printStackTrace();
//                Toast.makeText(this, "Image store failed" +e, Toast.LENGTH_LONG).show();
//            }


        }
    }

}
