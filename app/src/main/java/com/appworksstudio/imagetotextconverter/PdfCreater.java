package com.appworksstudio.imagetotextconverter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class PdfCreater extends AppCompatActivity {

    private static final int PICK_IMAGE_GALLERY = 897;
    private static final int REQUEST_IMAGE_CAPTURE_PDF_CREATE = 898;
    private LinearLayout linearLayout;
    private int[] ids;
    private int indexOfArray = 0;
    private int numImages = 0;
    private EditText filenameEditText;
    private Button saveFilenameButton, cancelFilenameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_creater);

        //Initialization
        ImageButton addImageButton = findViewById(R.id.btn_add_image);
        Button mergeButton = findViewById(R.id.btn_save);
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

                createFilenameAsk(v);


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


        //Pick Image from Gallery
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
            numImages += 1;
            Toast.makeText(this, "t: " + tempId, Toast.LENGTH_SHORT).show();

            try {
                FileOutputStream fos = new FileOutputStream(file);

                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            childView.setScaleType(ImageView.ScaleType.FIT_XY);
            childView.setPadding(10, 10, 10, 10);
            childView.setImageBitmap(bmp);
            linearLayout.addView(childView);


        }

        //Pick Image From Camera
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

    private void createPDF(View v, String filename) throws IOException, DocumentException {

        Document document = new Document();

        //String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS); //context.getExternalFilesDir(null);
        Date currentTime = Calendar.getInstance().getTime();

        PdfWriter.getInstance(document, new FileOutputStream(storageLoc + "/" + filename + "_" +
                currentTime.toString().replace(":", "_").replace(" ", "_")
                + ".pdf")); //  Change pdf's name.

        document.open();
        File cacheDir = getCacheDir();
        Image image = null;
        File cfile = new File(cacheDir.getAbsolutePath());


        for (int id = 1; id <= numImages; id++) {
            image = Image.getInstance(cfile + "/" + "temp_image" + id + ".jpg");


            float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                    - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
            image.scalePercent(scaler);
            image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);


            document.add(image);
            document.newPage();

        }

//        Image image2 = Image.getInstance(cfile + "/" + "temp_image2.jpg");
//
//
//        float scaler2 = ((document.getPageSize().getWidth() - document.leftMargin()
//                - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
//        image2.scalePercent(scaler2);
//        image2.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
//
//        document.add(image2);
        document.close();
        Toast.makeText(this, "completed", Toast.LENGTH_SHORT).show();
        deleteCache(this);
        finish();


    }

    private void createFilenameAsk(final View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PdfCreater.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        final View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.ask_filename_custom_dialog, viewGroup, false);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
        saveFilenameButton = dialogView.findViewById(R.id.btn_save_filename);
        cancelFilenameButton = dialogView.findViewById(R.id.btn_cancel_filename);
        filenameEditText = dialogView.findViewById(R.id.et_filename);

        saveFilenameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String filename = filenameEditText.getText().toString().trim().toLowerCase();
                    if (TextUtils.isEmpty(filename)) {
                        filenameEditText.setError("Enter valid filename");
                        return;
                    } else {
                        createPDF(v, filename);
                    }


                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                }
                alertDialog.dismiss();

            }
        });

        cancelFilenameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }


}
