package com.appworksstudio.imagetotextconverter;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageEditor extends AppCompatActivity {

    private static final int PIC_CROP = 9696;
    private Button colorBtn, bnwBtn, cropBtn, doneBtn;
    private static final int PERMISSION_REQUEST_CODE_EXTERNAL_READ = 1007;
    private Bitmap colorBitmap, bnwBitmap;
    private ImageView editedImage;
    private ImageButton rotateP90, rotateN90;
    private SeekBar tilt;
    private Uri uriForCrop;

    private boolean colorOrBnw = true;
    private String filename = "", path = "";


    public static Bitmap rotateImage(Bitmap sourceImage, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap bm = Bitmap.createBitmap(sourceImage, 0, 0, sourceImage.getWidth(),
                sourceImage.getHeight(), matrix, true);
        bm.setHasAlpha(true);
        bm.eraseColor(Color.TRANSPARENT);

        return bm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);

        /**
         * initializing buttons
         */
        colorBtn = findViewById(R.id.color_btn);
        bnwBtn = findViewById(R.id.bnw_btn);
//        cropBtn = findViewById(R.id.crop_btn);
        doneBtn = findViewById(R.id.done_btn);
        rotateN90 = findViewById(R.id.rotate_n90);
        rotateP90 = findViewById(R.id.rotate_p90);

        /**
         * initializing other components
         */
        editedImage = findViewById(R.id.edited_img);
        tilt = findViewById(R.id.tilt);


        /**
         * Getting data from intent
         */
        filename = getIntent().getStringExtra("filename");
        uriForCrop = getIntent().getData();


        File cacheDir = getCacheDir();
        File f = new File(cacheDir, filename + ".jpg");

        try {
            colorBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        editedImage.setImageBitmap(colorBitmap);
        if (colorBitmap != null) {
            bnwBitmap = toGrayscale(colorBitmap);
        }


        /**
         * Setting Listener on Color  Button
         */

        colorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorOrBnw = true;
                editedImage.setImageBitmap(colorBitmap);
            }
        });


        /**
         * Setting listener on Black and White button
         */
        bnwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorOrBnw = false;
                editedImage.setImageBitmap(bnwBitmap);
            }
        });


        /**
         * Setting Listener on confirm and save (done) button
         */
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("filename", filename);

                BitmapDrawable drawable = (BitmapDrawable) editedImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                cacheMyImage(bitmap, filename);
                setResult(RESULT_OK, data);
                finish();

            }
        });


        /**
         * Setting listener for Crop button
         */
       /* cropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCrop(uriForCrop);
            }
        });
*/

        /**
         * Setting listener for Rotate anti-clockwise image by 90
         */
        rotateN90.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Matrix matrix = new Matrix();

                matrix.postRotate(-90);
                BitmapDrawable d = (BitmapDrawable) editedImage.getDrawable();
                Bitmap bitmap = d.getBitmap();
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                editedImage.setImageBitmap(scaledBitmap);


            }
        });


        /**
         * Setting listener for Rotate clockwise image by 90
         */
        rotateP90.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Matrix matrix = new Matrix();

                matrix.postRotate(90);
                BitmapDrawable d = (BitmapDrawable) editedImage.getDrawable();
                Bitmap bitmap = d.getBitmap();
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                editedImage.setImageBitmap(scaledBitmap);


            }
        });

        /**
         * Setting listener for rotating image based on seekbar
         */

        tilt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress - 50;

                float angle = progress;
                editedImage.buildDrawingCache();
                Bitmap source = editedImage.getDrawingCache();
                Bitmap rotatedImage = rotateImage(source, angle);


                editedImage.setImageBitmap(rotatedImage);


                editedImage.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PIC_CROP) {
            if (data != null) {
                // get the returned data
                Bundle extras = data.getExtras();
                // get the cropped bitmap
                Bitmap selectedBitmap = extras.getParcelable("data");

                editedImage.setImageBitmap(selectedBitmap);
            }
        }
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


    private void cacheMyImage(Bitmap bitmap, String filename) {


        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_EXTERNAL_READ);
            } else {
                File cacheDir = getCacheDir();
                Bitmap bmp = bitmap;
                File file = new File(cacheDir, filename + ".jpg");

                try {
                    FileOutputStream fos = new FileOutputStream(file);

                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

    }

    private void performCrop(Uri picUri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 128);
            cropIntent.putExtra("outputY", 128);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
