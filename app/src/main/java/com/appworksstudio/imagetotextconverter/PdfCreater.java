package com.appworksstudio.imagetotextconverter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PdfCreater extends AppCompatActivity {

    private static final int PICK_IMAGE_GALLERY = 897;
    private static final int EDIT_IMAGE = 898;
    private static final int CAMERA_REQUEST = 999;

    private static final int PERMISSION_REQUEST_CODE_EXTERNAL_READ = 1007;
    private static final int PERMISSION_REQUEST_CODE_EXTERNAL_WRITE = 1008;
    private static final int PERMISSION_REQUEST_CODE_CAMERA = 1008;

    private Uri currentUri;
    private LinearLayout linearLayout;
    private EditText filenameEditText;

    String currentImagePath = null;


    List<String> fileList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_creater);

        //Initialization
        ImageButton addImageButton = findViewById(R.id.btn_add_image);
        Button mergeButton = findViewById(R.id.crop_btn);
        linearLayout = findViewById(R.id.cl_for_image);

        //setting listener on Add Image Button
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog();
            }

        });

        /**
         * create PDF
         */
        mergeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_EXTERNAL_WRITE);
                    } else {
                        createFilenameAsk(v);
                    }
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

                            /**
                             * Pick Image from Gallery
                             */

                            pickImageFromGallery();


                        } else {
                            /**
                             * Capture Image From Camera
                             */
                            captureImage();

                        }
                    }
                }).show();

    }

    private void captureImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE_CAMERA);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File imageFile = null;
                    try {
                        imageFile = getImageFile();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (imageFile != null) {
                        Uri uri = FileProvider.getUriForFile(this, "com.appworksstudio.imagetotextconverter.fileProvider", imageFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }

                }
            }
        }
    }

    private void pickImageFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PdfCreater.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_EXTERNAL_READ);

            } else {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, PICK_IMAGE_GALLERY);
            }
        }


    }

    private File getImageFile() throws IOException {
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_" + time + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        currentImagePath = imageFile.getAbsolutePath();

        return imageFile;


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                ImageView childView = new ImageView(this);
                childView.setImageURI(resultUri);
                BitmapDrawable drawable = (BitmapDrawable) childView.getDrawable();
                Bitmap b = drawable.getBitmap();
                Date currentTime = Calendar.getInstance().getTime();
                String filename = currentTime.toString().replace(":", "_").replace(" ", "_").replace("+", "_");

                fileList.add(filename);

                cacheMyBitmap(b, filename);

                Intent intent = new Intent(this, ImageEditor.class);
                intent.putExtra("filename", filename);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setData(getCurrentUri());

                startActivityForResult(intent, EDIT_IMAGE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        /**
         * Pick Image from Gallery
         **/

        if (requestCode == PICK_IMAGE_GALLERY && data != null) {
            //getFilePath
//            String path = getPicturePath(data);
            Uri uri = data.getData();
            setCurrentUri(uri);
            CropImage.activity(uri)
                    .start(this);

        }


        /**
         * Pick Image From Camera
         */
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            setCurrentUri(Uri.fromFile(new File(currentImagePath)));
            CropImage.activity(Uri.fromFile(new File(currentImagePath)))
                    .start(this);

        }
        /**
         * Pick Image From Camera
         */
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == EDIT_IMAGE) {

                String edited_filename = data.getStringExtra("filename");
                File cacheDir = getCacheDir();

                File f = new File(cacheDir, edited_filename + ".jpg");

                try {
                    Bitmap colorBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                    ImageView childView = new ImageView(this);

                    childView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    childView.setAdjustViewBounds(true);
                    childView.setPadding(10, 10, 10, 10);
                    childView.setImageBitmap(colorBitmap);
                    linearLayout.addView(childView);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }


    }

    private void cacheMyBitmap(Bitmap bitmap, String filename) {


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


    private void createPDF(View v, String filename) throws IOException, DocumentException {

        Document document = new Document();

        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS); //context.getExternalFilesDir(null);
        Date currentTime = Calendar.getInstance().getTime();

        PdfWriter.getInstance(document, new FileOutputStream(storageLoc + "/" + filename + "_" +
                currentTime.toString().replace(":", "_").replace(" ", "_")
                + ".pdf")); //  Change pdf's name.

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_EXTERNAL_WRITE);
//            } else {
//                Date currentTime = Calendar.getInstance().getTime();
//                File file = new File(getApplicationContext().getFilesDir()+File.separator+"texscan/"+
//                        currentTime.toString().replace(":", "_").replace(" ", "_")+".pdf");
//                if (!file.exists()) {
//                    file.mkdirs();
//                }
//                FileOutputStream fos =new FileOutputStream(file);
//                PdfWriter.getInstance(document, fos);
//
//            }
//        }

        document.open();
        File cacheDir = getCacheDir();
        Image image = null;
        File cfile = new File(cacheDir.getAbsolutePath());

        for (int id = 0; id < fileList.size(); id++) {
            image = Image.getInstance(cfile + "/" + fileList.get(id) + ".jpg");
            document.setMargins(10, 10, 10, 10);


            float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                    - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
            image.scalePercent(scaler);
            image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);


            document.add(image);

            document.newPage();

        }
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
        Button saveFilenameButton = dialogView.findViewById(R.id.btn_save_filename);
        Button cancelFilenameButton = dialogView.findViewById(R.id.btn_cancel_filename);
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

    private String getPicturePath(Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        Toast.makeText(this, "imagepath" + picturePath, Toast.LENGTH_SHORT).show();
        return picturePath;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use Camera .");
            } else {
                Log.e("value", "Permission Denied, You cannot use Camera .");
            }
        }
        if (requestCode == PERMISSION_REQUEST_CODE_EXTERNAL_READ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use External read .");
            } else {
                Log.e("value", "Permission Denied, You cannot use External read .");
            }
        }
        if (requestCode == PERMISSION_REQUEST_CODE_EXTERNAL_WRITE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use External write .");
            } else {
                Log.e("value", "Permission Denied, You cannot use External write .");
            }
        }

    }


    public Uri getCurrentUri() {
        return currentUri;
    }

    public void setCurrentUri(Uri currentUri) {
        this.currentUri = currentUri;
    }
}


