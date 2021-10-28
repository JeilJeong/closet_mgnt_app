package com.graduate.lookatv2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.graduate.lookatv2.camview.GetContours;
import com.graduate.lookatv2.camview.GetTargetContour;
import com.graduate.lookatv2.camview.ImageIO;
import com.graduate.lookatv2.camview.ProcessedImageActivity;
import com.graduate.lookatv2.camview.RealTimeCamera;
import com.graduate.lookatv2.camview.RealTimeProcessor;
import com.graduate.lookatv2.camview.SortPointArray;
import com.graduate.lookatv2.camview.TransformPerspective;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class SearchActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnLongClickListener,
        RealTimeCamera.PictureResult {
    private static final String TAG = "AndroidOpenCv";
    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 720;
    //    private static final int FACTOR_MB = 1024 * 1024;
    private RealTimeCamera mRealTimeCameraView;
    private final RealTimeProcessor mRealTimeProcessor = new RealTimeProcessor();
    private int mMode;

    private Button pictureBtn;
    private ImageView thumnail;

//    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);

    static {
        System.loadLibrary("opencv_java4");
//        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mMode = intent.getIntExtra("mode", 2);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_search);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        mRealTimeCameraView = (RealTimeCamera) findViewById(R.id.java_camera_view);
        mRealTimeCameraView.setVisibility(SurfaceView.VISIBLE);
        mRealTimeCameraView.setCameraPermissionGranted();
        mRealTimeCameraView.setCvCameraViewListener(this);
        mRealTimeCameraView.setPictureResult(this);
        mRealTimeCameraView.setOnLongClickListener(this);
        if (mMode == 2) {
            mRealTimeCameraView.setCameraIndex(0);
        } else {
            mRealTimeCameraView.setCameraIndex(mMode); // rear = 0, front = 1
        }

//      take picture button
        pictureBtn = (Button) findViewById(R.id.camBtn);
        pictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Take a photo!");
                mRealTimeCameraView.takePhoto();
            }
        });

//      thumnail imgview
        thumnail = findViewById(R.id.image_thumnail);

//      debugging log
        Log.d(TAG, "onCreate: end line");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRealTimeCameraView != null)
            mRealTimeCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume : OpenCV initialization error");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResume : OpenCV initialization success");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mRealTimeCameraView.enableView();
                    Log.d(TAG, "onManagerConnected: enableView()");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRealTimeCameraView != null) {
            mRealTimeCameraView.disableView();
            Log.d(TAG, "onDestroy: mCameraView not null");
        } else {
            Log.d(TAG, "onDestroy: mCameraView=null");
        }
    }

    // This method is invoked when camera preview has started.
    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "onCameraViewStarted");
    }

    // This method is invoked when camera preview has been stopped for some reason.
    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "onCameraViewStopped");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        final Mat rgba = inputFrame.rgba();
        final Mat res = mRealTimeProcessor.process(rgba);
        return res;
    }

    // permission
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS = {"android.permission.CAMERA"};

    private boolean hasPermissions(String[] permissions) {
        int result;
        for (String perms : permissions) {
            result = ContextCompat.checkSelfPermission(this, perms);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "Take a long_click photo!");
        mRealTimeCameraView.takePhoto();
        return true;
    }

//    @Override
//    public void onPictureTaken(byte[] picture) {
//        Log.d(TAG, "Picture taken!");
//        // Send the image to be precessed
//        final Intent imageBytes = new Intent(this, ProcessedImageActivity.class);
//        imageBytes.putExtra("image", picture);
//        // Image size data
//        final Camera.Size size = mRealTimeCameraView.size();
//        imageBytes.putExtra("width", size.width);
//        imageBytes.putExtra("height", size.height);
//        // Send to precess
//        startActivity(imageBytes);
//    }

//    @Override
//    public void onPictureTaken(byte[] picture) {
//        Log.d(TAG, "Picture taken!");
//        final Camera.Size size = mRealTimeCameraView.size();
//
////      resize
//        final Size originalSize = new Size(size.width, size.height);
//        final Size targetSize = new Size(MAX_WIDTH, MAX_HEIGHT);
//        final Mat matImage = new Mat(originalSize, CvType.CV_8U);
//        final Mat targetImage = new Mat(targetSize, matImage.type());
//
//        matImage.put(0, 0, picture);
//
////        Imgproc.resize(matImage, targetImage, targetSize, 0, 0, Imgproc.INTER_AREA);
//
////      back to byte array
//        final Bitmap bitmap = Bitmap.createBitmap((int) size.width, (int) size.height, Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(matImage, bitmap);
//
//
//        thumnail.setImageBitmap(bitmap);
//
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        byte[] byteArray = stream.toByteArray();
////        bitmap.recycle();
//
//        // Send the image to be precessed
//        final Intent imageBytes = new Intent(this, ProcessedImageActivity.class);
//        String filename = new SimpleDateFormat("yyyyMMdd_HH_mm_ss").format(new Date());
//        ImageIO.saveImage(picture, filename);
//
////        List<byte[]> imgDivList = new ArrayList<>();
////        final int divisionLevel = divisionFactor(picture.length);
////        for (int n = divisionLevel; n > 0; n--) {
////            imgDivList.add(divisionFactor)
////        }
//        imageBytes.putExtra("image", byteArray);
////        // Image size data
////        final Camera.Size size = mRealTimeCameraView.size();
//        Log.d(TAG, "width: " + targetSize.width);
//        Log.d(TAG, "height: " + targetSize.height);
//        imageBytes.putExtra("width", MAX_WIDTH);
//        imageBytes.putExtra("height", MAX_HEIGHT);
////        imageBytes.putExtra("filename", filename + ".jpeg");
//        // Send to precess
////        startActivity(imageBytes);

    @Override
    public void onPictureTaken(byte[] picture) {
        Log.d(TAG, "Picture taken!");

        Bitmap bitmap = pictureProcesss(picture);
        thumnail.setImageBitmap(bitmap);
        ImageIO.saveImageBitmap(bitmap);
    }

    protected Bitmap pictureProcesss(byte[] imageArray) {

        final int width = (int)mRealTimeCameraView.size().width;
        final int height = (int)mRealTimeCameraView.size().height;
        if (width == 0 || height == 0) {
            Log.w(TAG, "Can't determine image size");
            return null;
        }
        Log.d(TAG, "Received image (" + imageArray.length + " bytes, w="
                + width + ", h=" + height + ")");

        // Transform the byte array to Mat object
        final Size size = new Size(width, height);
        final Mat matImage = new Mat(size, CvType.CV_8U);
        matImage.put(0, 0, imageArray);
        Log.d(TAG, "Converted image byte array to Mat object");

        // Apply filters
        final Mat grayMat = new Mat();
        Imgproc.cvtColor(matImage, grayMat, Imgproc.COLOR_BayerBG2GRAY);
        Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5), 0);

        final Mat cannedMat = new Mat();
        Imgproc.Canny(grayMat, cannedMat, 75, 200);

        // Find contours from the image
        final GetContours getContours = new GetContours(cannedMat);
        final List<MatOfPoint> contours = getContours.contours();
        if (contours.isEmpty()) {
            Log.w(TAG, "No contours found!");
            return null;
        }
        // Get the large contour
        final Mat target = new GetTargetContour(contours).target();
        if (target == null) {
            Log.w(TAG, "Can't find target contour, aborting...");
            return null;
        }
        Log.d(TAG, "Target contour found!");

        // Sort points
        final Point[] points = new MatOfPoint(target).toArray();
        final Point[] orderedPoints = new SortPointArray(points).sort();
        Log.d(TAG, "Points: " + Arrays.toString(orderedPoints));

        // Now apply perspective transformation
        final TransformPerspective transformPerspective = new TransformPerspective(
                points, matImage);
        final Mat transformed = transformPerspective.transform();

        // With the transformed points, now convert the image to gray scale
        // and threshold it to give it the paper effect
        Imgproc.cvtColor(transformed, transformed, Imgproc.COLOR_BayerBG2GRAY);
        Imgproc.adaptiveThreshold(transformed, transformed, 251,
                Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 15);

        final Size transformedSize = transformed.size();
        final int resultW = (int) transformedSize.width;
        final int resultH = (int) transformedSize.height;

        final Mat result = new Mat(resultH, resultW, CvType.CV_8UC4);
        transformed.convertTo(result, CvType.CV_8UC4);

        final Bitmap bitmap = Bitmap.createBitmap(resultW, resultH, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, bitmap);
        // Release
        transformed.release();
        result.release();
        target.release();

        return bitmap;
    }
}

