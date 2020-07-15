package com.appworksstudio.imagetotextconverter;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


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
        Button btn_c2 = findViewById(R.id.btn_c2);
        img = findViewById(R.id.img);
        img2 = findViewById(R.id.img_2);


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
        btn_c2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // dispatchTakePictureIntent2();
                //texrecd.setText(R.string.texrec_here);
                //final_out = "";
                startActivity(new Intent(MainActivity.this, PdfCreater.class));
//                try {
//                    createPDF();
//                } catch (IOException | DocumentException e) {
//                    e.printStackTrace();
//                }
            }
        });


    }


//
//    private void tessrect() {
//        Tesseract tesseract = new Tesseract();
//        try {
//
//            tesseract.setDatapath("assets/tessdata");
//
//            // the path of your tess data folder
//            // inside the extracted file
//            ContextWrapper cw = new ContextWrapper(getApplicationContext());
//            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
////            File file = new File(directory, "mario" + ".png");
////            imageView.setImageDrawable(Drawable.createFromPath(file.toString()));
//
//            String text
//                    = tesseract.doOCR(new File(directory, "mario" + ".jpg"));
//
//            // path of your image file
//            System.out.print(text);
//        }
//        catch (TesseractException e) {
//            e.printStackTrace();
//        }
//    }

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

    private void dispatchTakePictureIntent2() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE2);
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

            FileOutputStream outStream = null;
            File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); //context.getExternalFilesDir(null);



//            String fileName = String.format("%d.jpg", System.currentTimeMillis());
            String fileName = "omk";
            File file = new File(storageLoc, fileName + ".jpg");
            //File outFile = new File(dir, fileName);
            try {
                outStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                Toast.makeText(this, "Successfully image stored", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Image store failed" +e, Toast.LENGTH_LONG).show();
            }

//            ContextWrapper cw = new ContextWrapper(getApplicationContext());
//            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
//            File file = new File(directory, "mario" + ".jpg");
//            if (!file.exists()) {
//                Log.d("path", file.toString());
//                FileOutputStream fos = null;
//                try {
//                    fos = new FileOutputStream(file);
//                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                    fos.flush();
//                    fos.close();
//                } catch (java.io.IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE2 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            img2.setImageBitmap(imageBitmap);

//            //saving image


        }
    }

    private void createPDF() throws IOException, DocumentException {
        Document document = new Document();

        //String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS); //context.getExternalFilesDir(null);

        PdfWriter.getInstance(document, new FileOutputStream(storageLoc + "/myfirst.pdf")); //  Change pdf's name.

        document.open();
        File imagestorageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); //context.getExternalFilesDir(null);
        Image image = Image.getInstance(imagestorageLoc+"/"+"omk.jpg");  // Change image's name and extension.

        float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
        image.scalePercent(scaler);
        image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

        document.add(image);
        document.close();
        Toast.makeText(this, "completetd", Toast.LENGTH_SHORT).show();

    }
}
