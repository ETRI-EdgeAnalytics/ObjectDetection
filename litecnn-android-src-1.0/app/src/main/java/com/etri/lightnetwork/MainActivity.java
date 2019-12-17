package com.etri.lightnetwork;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.etri.lightnetwork.tensorflow.Classifier;
import com.etri.lightnetwork.tensorflow_qunt.TensorFlowImageClassifier;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    final int REQ_CAMERA = 0x01;
    final int REQ_PICTURE = 0x02;
    final int PERMISSION_CAMERA = 0x11;
    final int PERMISSION_PICTURE = 0x12;
    final int REQUEST_ID_MULTIPLE_PERMISSIONS =0x13;
    //private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    //private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";
    private static final String MODEL_FILE[] = {"mobilenet_quant_v1_224.tflitemobilenet_quant_v1_224.tflite", "inception_v3.pb"};
    private static final String LABEL_FILE[] = {"labels.txt", "labels.txt"};
    private static final int INPUT_SIZE[] = {224,299};
    private static final int IMAGE_MEAN[] = {0, 0};
    private static final float IMAGE_STD[] = {1, 255};
    private static final String INPUT_NAME[] = {"input", "input"};
    private static final String OUTPUT_NAME[] = {"MobilenetV1/Predictions/Reshape_1", "InceptionV3/Predictions/Reshape_1"};
    private static final boolean QUANT_MODEL[] = {true, false};

    private ImageCache imageCache;
    private Handler handler;
    private HandlerThread handlerThread;

    private Classifier classifier;
    //private com.etri.lightnetwork.tensorflow.Classifier classifier;
    private File mTempCameraPhotoFile = null;
    private int currentSelectedModel = 0;
    //private String mCurrentPhotoPath;
    //File photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        imageCache = ImageCache.getInstance();


        currentSelectedModel = SystemInfo.getInstance(this).getSelectedModel();
        initTensorflow();
        checkReadPermission();
    }

    void initTensorflow() {

        //선택된 모델 정보 로드
        SystemInfo.getInstance(this).setSelectedModel(currentSelectedModel);

        //Quantized model인지 확인 한다.
        if(QUANT_MODEL[currentSelectedModel]) {
            try {
                //Quantized model 초기화
                classifier =
                        TensorFlowImageClassifier.create(
                                getAssets(),
                                MODEL_FILE[currentSelectedModel],
                                LABEL_FILE[currentSelectedModel],
                                INPUT_SIZE[currentSelectedModel]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //Tensorflow mobile model 초기화
            classifier =
                    com.etri.lightnetwork.tensorflow.TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE[currentSelectedModel],
                            LABEL_FILE[currentSelectedModel],
                            INPUT_SIZE[currentSelectedModel],
                            IMAGE_MEAN[currentSelectedModel],
                            IMAGE_STD[currentSelectedModel],
                            INPUT_NAME[currentSelectedModel],
                            OUTPUT_NAME[currentSelectedModel]);

        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    protected void processImage(Bitmap bmp) {
        //카메라, 갤러리에서 들어오 이미지를 딥러닝 모델에 맞게 리사이즈 한다.
        final Bitmap resized = Bitmap.createScaledBitmap(bmp, INPUT_SIZE[currentSelectedModel], INPUT_SIZE[currentSelectedModel], true);

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        //시간 측정을 시작한다.
                        final long startTime = SystemClock.uptimeMillis();
                        //이미지 Predict를 수행한다.
                        final List<Classifier.Recognition> results = classifier.recognizeImage(resized);
                        //수행 시간을 계산한다.
                        long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                        //수행 시간을 기록한다.
                        SystemInfo.getInstance(getApplicationContext()).putPerformanceResult(currentSelectedModel, (int)lastProcessingTimeMs);
                        //수행시간을 저장한다.
                        imageCache.setTime(lastProcessingTimeMs);
                        //수행 결과를 메모리에 저장하여 result activity에서 볼 수 있도록 한다.
                        imageCache.setResult(results);
                        //imageCache.setBitmap(resized);
                        openResult();
                    }
                });
    }

    @OnClick(R.id.btn_select_network)
    void OnClickSeletNetwork() {
        final String items[] = { "MobileNets", "InceptionV3" };
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("모델 선택");
        ab.setSingleChoiceItems(items, SystemInfo.getInstance(this).getSelectedModel(),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        currentSelectedModel = whichButton;
                    }
                }).setPositiveButton("선택",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        initTensorflow();
                    }
                }).setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Cancel 버튼 클릭시
                    }
                });
        ab.show();
    }

    @OnClick(R.id.btn_select_picture)
    void OnClickSelectPicture() {
        getPicture();
    }

    @OnClick(R.id.btn_camera)
    void OnClickCamera() {
        takePicture();
    }

    @OnClick(R.id.btn_report)
    void OnClickReport() {
        Intent intent = new Intent(this, ReportActivity.class);
        startActivity(intent);
    }


    private boolean checkReadPermission() {

        if (Build.VERSION.SDK_INT > 23) {
            int cameraSendMessage = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            List<String> listPermissionsNeeded = new ArrayList<>();
            if (cameraSendMessage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }

            if (readPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            }
        }

        return true;


    }


    public void getPicture() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        openPicture();
    }

    public void takePicture() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        openCamera();
    }

    private void openCamera() {
        int cameraSendMessage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);

        if (cameraSendMessage != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permission Error", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File basePath = getApplicationContext().getFilesDir();

            File exportDir = new File(basePath, "camera");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            } else {
                exportDir.delete();
            }
            mTempCameraPhotoFile = new File(exportDir, "/main_camera.jpg");
            Uri targetUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", mTempCameraPhotoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
            startActivityForResult(takePictureIntent, REQ_CAMERA);
        }
    }

    private void openPicture() {

        Intent intent = new Intent(Intent.ACTION_PICK,
        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, REQ_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("LOG", "ResultCode: "+resultCode);
        if (requestCode == REQ_PICTURE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            try {
                //Bitmap bmp = getBitmapFromUri(selectedImage);
                final InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                final Bitmap bmp = BitmapFactory.decodeStream(imageStream);
                imageCache.setBitmap(bmp);
                processImage(bmp);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else if (requestCode == REQ_CAMERA && resultCode == RESULT_OK) {
            try {


                String filePath = mTempCameraPhotoFile.getPath();

                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mTempCameraPhotoFile.getAbsolutePath(), bounds);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                Bitmap myBitmap = BitmapFactory.decodeFile(mTempCameraPhotoFile.getAbsolutePath(), opts);

                int rotationAngle = getCameraPhotoOrientation(this, FileProvider.getUriForFile(this, getPackageName() + ".provider", mTempCameraPhotoFile), mTempCameraPhotoFile.getAbsolutePath());

                Matrix matrix = new Matrix();
                matrix.postRotate(rotationAngle, (float) myBitmap.getWidth() / 2, (float) myBitmap.getHeight() / 2);
                Bitmap rotatedBitmap = Bitmap.createBitmap(myBitmap, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);

                imageCache.setBitmap(rotatedBitmap);
                processImage(rotatedBitmap);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath){
        int rotate = 0;
        try {
            getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    rotate = 0;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }
    void openResult() {
        Intent intent = new Intent(this, ResultActivity.class);
        startActivity(intent);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    void showNotImplemented() {
        Toast.makeText(this, "Not Implemented", Toast.LENGTH_SHORT).show();
    }

    private Bitmap loadBitmap(Bitmap bmp) {
        return null;
    }
}
