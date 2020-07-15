package com.appworksstudio.imagetotextconverter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.appworksstudio.imagetotextconverter.MainActivity.REQUEST_IMAGE_CAPTURE;

public class PdfCreater extends AppCompatActivity {

    private static final int PICK_IMAGE_GALLERY = 897;
    private static final int REQUEST_IMAGE_CAPTURE_PDF_CREATE = 898;
    private LinearLayout linearLayout;
    private int[] ids;
    private int indexOfArray = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_creater);

        //Initialization
        ImageButton addImageButton = findViewById(R.id.btn_add_image);
        Button mergeButton = findViewById(R.id.btn_merge);
        linearLayout = findViewById(R.id.cl_for_image);
        ids = new int[30];


        //setting listener on Add Image Button
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog();
            }

        });

        //create PDF
        mergeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int id : ids) {
                    System.out.println("valPath" + id);
                }
                try {
                    createPDF();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createDialog() {
        String[] items = {"Choose Image from gallery", "Capture Image"};
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(PdfCreater.this);
        builder.setTitle("Pick one action")
                .setIcon(R.drawable.ic_crop_original_black_24dp)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) {
                            pickImageFromGallery();
                        } else {
                            captureImage();
                        }
                    }
                }).show();

    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_PDF_CREATE);
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_GALLERY);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_GALLERY) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = ((Cursor) cursor).getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView childView = new ImageView(this);
            // set view id, else getId() returns -1
            int tempId = View.generateViewId();
            ids[indexOfArray] = tempId;
            childView.setId(tempId);
            File cacheDir = getCacheDir();
            Bitmap bmp = BitmapFactory.decodeFile(picturePath);
            File file = new File(cacheDir.getAbsolutePath(), "temp_image" + tempId + ".jpg");
            Toast.makeText(this, "t: " + tempId, Toast.LENGTH_SHORT).show();
            try {
                FileOutputStream fos = new FileOutputStream(file);

                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            childView.setScaleType(ImageView.ScaleType.FIT_XY);
            childView.setImageBitmap(bmp);
            linearLayout.addView(childView);


        }
        if (requestCode == REQUEST_IMAGE_CAPTURE_PDF_CREATE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            int tempId = View.generateViewId();
            ids[indexOfArray] = tempId;
            ImageView childView = new ImageView(this);
            // set view id, else getId() returns -1
            childView.setId(tempId);
            childView.setImageBitmap(imageBitmap);
            linearLayout.addView(childView);


        }
        indexOfArray++;
    }

    private void createPDF() throws IOException, DocumentException {
        Document document = new Document();

        //String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS); //context.getExternalFilesDir(null);

        PdfWriter.getInstance(document, new FileOutputStream(storageLoc + "/myfirst2.pdf")); //  Change pdf's name.

        document.open();
        File cacheDir = getCacheDir();
        Image image = null;
        File cfile = new File(cacheDir.getAbsolutePath());

        image = Image.getInstance(cfile + "/" + "temp_image1.jpg");


        float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
        image.scalePercent(scaler);
        image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

        Image image2 = Image.getInstance(cfile + "/" + "temp_image2.jpg");


        float scaler2 = ((document.getPageSize().getWidth() - document.leftMargin()
                - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
        image2.scalePercent(scaler2);
        image2.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

        document.add(image);
        document.add(image2);
        document.close();
        Toast.makeText(this, "completetd", Toast.LENGTH_SHORT).show();

    }
}
