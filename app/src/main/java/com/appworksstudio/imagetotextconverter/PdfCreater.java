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
import androidx.core.content.ContextCompat;
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

    private LinearLayout linearLayout;
    private int generatedId = 0;
    private EditText filenameEditText;

    public String getDirectoryLocation() {
        return directoryLocation;
    }

    public void setDirectoryLocation(String directoryLocation) {
        this.directoryLocation = directoryLocation;
    }

    private String directoryLocation = "";

    List<Integer> list = new ArrayList<>();
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

        //create PDF
        mergeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                            //Pick Image from Gallery
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_EXTERNAL_READ);
                                } else {
                                    pickImageFromGallery();
                                }
                            }

                        } else {
                            //Capture Image From Camera
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
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }

    private void pickImageFromGallery() {

        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE_GALLERY);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                ImageView childView = new ImageView(this);
//
//                childView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                childView.setAdjustViewBounds(true);
//                childView.setPadding(10, 10, 10, 10);
                childView.setImageURI(resultUri);
                BitmapDrawable drawable = (BitmapDrawable) childView.getDrawable();
                Bitmap b = drawable.getBitmap();
//                linearLayout.addView(childView);
                generatedId += 1;
                list.add(generatedId);
                Date currentTime = Calendar.getInstance().getTime();
                String filename = currentTime.toString().replace(":", "_").replace(" ", "_").replace("+", "_");
                //Bitmap b = BitmapFactory.decodeFile(path);

                //String p = saveToInternalStorage(b,filename);
                fileList.add(filename);
//            loadImageFromStorage(p, generatedId);
                //cache the image and get bitmap
//                cacheMyImage(getDirectoryLocation(), filename);
//
                cacheMyBitmap(b, filename);

                Intent intent = new Intent(this, ImageEditor.class);
                intent.putExtra("filename", filename);
                //intent.putExtra("path", getCacheDir());
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
            String path = getPicturePath(data);
            Uri uri = data.getData();
            setDirectoryLocation(path);
            CropImage.activity(uri)
                    .start(this);
//            generatedId += 1;
//            list.add(generatedId);
//            Date currentTime = Calendar.getInstance().getTime();
//            String filename = currentTime.toString().replace(":", "_").replace(" ", "_").replace("+", "_");
//            Bitmap b = BitmapFactory.decodeFile(path);
//
//            //String p = saveToInternalStorage(b,filename);
//            fileList.add(filename);
////            loadImageFromStorage(p, generatedId);
//            //cache the image and get bitmap
//            cacheMyImage(path, filename);
//
//
//            Intent intent = new Intent(this, ImageEditor.class);
//            intent.putExtra("filename", filename);
//            //intent.putExtra("path", getCacheDir());
//            startActivityForResult(intent, EDIT_IMAGE);


        }


        /**
         * Pick Image From Camera
         */
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            Uri outputFileUri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_EXTERNAL_READ);
                } else {

                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_EXTERNAL_WRITE);
                    } else {
                        outputFileUri = getCaptureImageOutputUri();
                        Toast.makeText(this, "ex uri:" + outputFileUri, Toast.LENGTH_SHORT).show();
                    }


                }
            }

            Toast.makeText(this, "o: " + outputFileUri, Toast.LENGTH_SHORT).show();
            ImageView childView = new ImageView(this);

            childView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            childView.setAdjustViewBounds(true);
            childView.setPadding(10, 10, 10, 10);
            childView.setImageURI(outputFileUri);
            linearLayout.addView(childView);
            CropImage.activity(outputFileUri)
                    .start(this);
//            generatedId += 1;
//            int tempId = generatedId;
//            list.add(tempId);
//
//
//            Date currentTime = Calendar.getInstance().getTime();
//            String filename = currentTime.toString().replace(":", "_").replace(" ", "_").replace("+", "_");
//
//            // String p = saveToInternalStorage(photo, filename);
//            cacheMyBitmap(photo, filename);
//            fileList.add(filename);
//            Intent intent = new Intent(PdfCreater.this, ImageEditor.class);
//
//            intent.putExtra("filename", filename);
//            //  intent.putExtra("path", p);
//            startActivityForResult(intent, EDIT_IMAGE);


//            pathList.add(p);
//            loadImageFromStorage(p);

        }

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
//                File cacheDir = getCacheDir();
//                Drawable d = Drawable.createFromPath(cacheDir.getAbsolutePath() + "/" + "temp_image" + tempIdReturn + ".jpg");
                //    loadImageFromStorage(getDirectoryLocation(), edited_filename);
//                ImageView childView = new ImageView(this);
//
//                childView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                childView.setAdjustViewBounds(true);
//                childView.setPadding(10, 10, 10, 10);
//                childView.setImageDrawable(d);
//                linearLayout.addView(childView);
            }
        }


    }

    //
//    public String saveToInternalStorage(Bitmap bitmapImage, String filename) {
//        ContextWrapper cw = new ContextWrapper(getApplicationContext());
//        // path to /data/data/yourapp/app_data/imageDir
//
//        File directory = cw.getDir("Texscan", Context.MODE_PRIVATE);
//
//        if (!directory.exists()) {
//            directory.mkdir();
//        }
//        Log.d("directory", directory.getAbsolutePath().toString());
//        // Create imageDir
//        File mypath = new File(directory, filename + ".jpg");
//
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(mypath);
//            // Use the compress method on the BitMap object to write image to the OutputStream
//            if (bitmapImage.getByteCount() / 1048576 > 4) {
//                Toast.makeText(cw, "large file called", Toast.LENGTH_SHORT).show();
//                bitmapImage.compress(Bitmap.CompressFormat.PNG, 60, fos);
//            } else {
//                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        setDirectoryLocation(directory.getAbsolutePath());
//
//        return directory.getAbsolutePath();
//    }
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {

            outputFileUri = FileProvider.getUriForFile(getApplication(),
                    getApplication().getPackageName() + ".fileProvider", new File(getImage.getPath(), "pickImageResult.jpeg"));
        }
        return outputFileUri;
    }


//    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
//        int width = image.getWidth();
//        int height = image.getHeight();
//
//        float bitmapRatio = (float) width / (float) height;
//        if (bitmapRatio > 1) {
//            width = maxSize;
//            height = (int) (width / bitmapRatio);
//        } else {
//            height = maxSize;
//            width = (int) (height * bitmapRatio);
//        }
//        return Bitmap.createScaledBitmap(image, width, height, true);
//    }
//
//    private void loadImageFromStorage(String path, String filename) {
//
//        try {
//            File f = new File(path, filename + ".jpg");
//            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//            ImageView childView = new ImageView(this);
//            childView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//            childView.setAdjustViewBounds(true);
//            childView.setPadding(10, 10, 10, 10);
//            childView.setImageBitmap(b);
//            linearLayout.addView(childView);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//    }
//

    private void cacheMyImage(String ppath, String filename) {


        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_EXTERNAL_READ);
            } else {
                File cacheDir = getCacheDir();
                Bitmap bmp = BitmapFactory.decodeFile(ppath);
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


//        for (int id = 1; id <= numImages; id++) {
        for (int id = 0; id < list.size(); id++) {
            image = Image.getInstance(cfile + "/" + fileList.get(id) + ".jpg");


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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_EXTERNAL_WRITE);
                            } else {
                                createPDF(v, filename);
                            }
                        }

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

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(PdfCreater.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(PdfCreater.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(PdfCreater.this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(PdfCreater.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_CAMERA);
        }
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


}


