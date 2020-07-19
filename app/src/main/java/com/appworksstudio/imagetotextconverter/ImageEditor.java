package com.appworksstudio.imagetotextconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageEditor extends AppCompatActivity {

    private Button colorBtn, bnwBtn, cropBtn, doneBtn;
    private Bitmap colorBitmap, bnwBitmap;
    private ImageView editedImage;
    private boolean colorOrBnw = true;
    private String filename = "", path = "";

    private LruCache<String, Bitmap> mMemoryCache;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);

        //initialize
        colorBtn = findViewById(R.id.color_btn);
        bnwBtn = findViewById(R.id.bnw_btn);
        cropBtn = findViewById(R.id.crop_btn);
        doneBtn = findViewById(R.id.done_btn);

        editedImage = findViewById(R.id.edited_img);

        filename = getIntent().getStringExtra("filename");
        path = getIntent().getStringExtra("path");
        Toast.makeText(this, "filename: "+filename +"path: "+path,Toast.LENGTH_LONG).show();
        File f = new File(path, filename + ".jpg");
        try {
            colorBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        editedImage.setImageBitmap(colorBitmap);
        if (colorBitmap != null) {
            bnwBitmap = toGrayscale(colorBitmap);
        }

        colorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorOrBnw = true;
                editedImage.setImageBitmap(colorBitmap);
            }
        });
        bnwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorOrBnw = false;
                editedImage.setImageBitmap(bnwBitmap);
            }
        });
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("filename", filename);



//                //---set the data to pass back---
                if (colorOrBnw) {
                    //color bitmap
                    saveToInternalStorage(colorBitmap, filename);
                } else {
                    //bnwbitmap
                    saveToInternalStorage(bnwBitmap, filename);
                }


                setResult(RESULT_OK, data);
                //---close the activity---
                finish();
            }
        });

    }


    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    //    private void cacheMyImage(Bitmap bmp, int tempId) {
//        File cacheDir = getCacheDir();
//
//        File file = new File(cacheDir.getAbsolutePath(), "temp_image" + tempId + ".jpg");
//
//        try {
//            FileOutputStream fos = new FileOutputStream(file);
//
//            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//    }
    public String saveToInternalStorage(Bitmap bitmapImage, String filename) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("Texscan", Context.MODE_PRIVATE);

        Log.d("directory", directory.getAbsolutePath().toString());
        // Create imageDir
        File mypath = new File(directory, filename + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return directory.getAbsolutePath();
    }

}
