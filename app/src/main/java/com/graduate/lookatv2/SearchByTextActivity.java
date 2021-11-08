package com.graduate.lookatv2;

import static com.graduate.lookatv2.camview.ImageIO.byteArrayToBitmap;
import static com.graduate.lookatv2.camview.ImageIO.savePath;
import static com.graduate.lookatv2.camview.PreProcessImage.getPreProcessedImage;
import static com.graduate.lookatv2.ocr.ProcessOcr.processOcr;
import static com.graduate.lookatv2.ocr.ProcessingOcrToTts.processOcrToTTS;
import static com.graduate.lookatv2.utils.PrintUtil.printLog;
import static com.graduate.lookatv2.utils.TransformUtil.rotateBitmapImage;
import static com.graduate.lookatv2.utils.ViewUtil.replaceView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.graduate.lookatv2.camview.Constant;
import com.graduate.lookatv2.camview.ImageIO;
import com.graduate.lookatv2.camview.RealTimeCamera;
import com.graduate.lookatv2.config.LayoutSetting;
import com.graduate.lookatv2.config.PermissionSetting;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

public class SearchByTextActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnLongClickListener,
        RealTimeCamera.PictureResult {
    private RealTimeCamera mRealTimeCameraView;
    private Button pictureBtn;
    private Button restart_cam_btn;
    private ImageView processedImgView;
    private ImageView thumView;
    private TextView resultTextView;
    private TextToSpeech tts;

    private boolean processedImgView_touch_flag;

    //  [setting] load opencv_java library
    static {
        System.loadLibrary("opencv_java4");
    }

// =================================================================================================
// =================================================================================================
// ============================ Search Activity Core Function Area Start============================
// =================================================================================================
// =================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        printLog("onCreate() start");

//      [setting] for full screen javacamview, but it doesn't work
        LayoutSetting.setWindowFullScreen(this);

//      [setting] default setting by system, this operation connects layout and java class
        setContentView(R.layout.activity_search_by_text);
//      [setting] version control for getting permission on runtime
        PermissionSetting.setPermissionOnRuntime(this);

//      [setting] bind layout & cam preview setting
        mRealTimeCameraView = (RealTimeCamera) findViewById(R.id.java_camera_view);
        mRealTimeCameraView.setVisibility(SurfaceView.VISIBLE);
        mRealTimeCameraView.setCameraPermissionGranted();
        mRealTimeCameraView.setCvCameraViewListener(this);
        mRealTimeCameraView.setPictureResult(this);
        mRealTimeCameraView.setOnLongClickListener(this);
        mRealTimeCameraView.setCameraIndex(0); // rear = 0, front = 1

//      [setting] bind layout & setOnClickListener
        pictureBtn = (Button) findViewById(R.id.camBtn);
        pictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printLog("pictureBtn onClick()");
                mRealTimeCameraView.takePhoto();
            }
        });

//      [setting] bind layout & setOnTouchListener & override onActivityResult with requestCode
        thumView = findViewById(R.id.thumVeiw);
        thumView.setBackground(new ShapeDrawable(new OvalShape()));
        thumView.setClipToOutline(true);
        thumView.setVisibility(View.GONE);
        thumView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printLog("thumView onTouch()");
//              [ref] from do it android programming 94th lecture
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(intent, 101);
            }
        });

//      [setting] bind layout & set none visible
        processedImgView = findViewById(R.id.processed_img_view);
        processedImgView.setVisibility(View.GONE);
        processedImgView_touch_flag = false;

//      [setting] bind layout
        resultTextView = findViewById(R.id.resultTextView);

//      [setting] bind layout & set none visible
        restart_cam_btn = findViewById(R.id.restart_cam_btn);
        restart_cam_btn.setVisibility(View.GONE);
        restart_cam_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceView(processedImgView, mRealTimeCameraView);
                restart_cam_btn.setVisibility(View.GONE);
                resultTextView.setText("");
            }
        });

        printLog("onCreate() end");
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            printLog("onResume : OpenCV initialization error");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            printLog("onResume : OpenCV initialization success");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRealTimeCameraView != null)
            mRealTimeCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRealTimeCameraView != null) {
            mRealTimeCameraView.disableView();
            printLog("onDestroy: mCameraView not null");
        } else {
            printLog("onDestroy: mCameraView=null");
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    //  [processing] processing for startForActivity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                Uri fileUri = data.getData();

                ContentResolver resolver = getContentResolver();
                try {
                    InputStream inputStream = resolver.openInputStream(fileUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                    thumView.setImageBitmap(bitmap);
                    inputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

// =================================================================================================
// =================================================================================================
// ============================ Search Activity Core Function Area End==============================
// =================================================================================================
// =================================================================================================


// =================================================================================================
// =================================================================================================
// ================================ OpenCV Method Area Start========================================
// =================================================================================================
// =================================================================================================

    //  [function] This is callback method for initializing status in load opencv
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mRealTimeCameraView.enableView();
                    printLog("onManagerConnected: enableView()");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    //  [function] This method is invoked when camera preview has started.
    @Override
    public void onCameraViewStarted(int width, int height) {
        printLog("onCameraViewStarted");
    }

    //  [function] This method is invoked when camera preview has been stopped for some reason.
    @Override
    public void onCameraViewStopped() {
        printLog("onCameraViewStopped");
    }


    //  [function] Core function for processing image from input frame
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Map obj = getPreProcessedImage(inputFrame);

//      [processing] 1) get mat object from the result map
        Mat return_frame = (Mat) obj.get(Constant.PROCESSING_IMAGE_RETURN_FRAME_KEY);

//      [processing] 2-1) return when bitmap does not exist in result map
//                      , this means that couldn't find proper contours
        if (!obj.containsKey(Constant.PROCESSING_IMAGE_RETURN_BITMAP_KEY))
            return (return_frame);

//      [processing] 2-2) get bitmap object from the result map
        Bitmap bitmap = (Bitmap) obj.get(Constant.PROCESSING_IMAGE_RETURN_BITMAP_KEY);

//      [processing] 3) make green contour bitmap image for onTouchListener in runOnUiThread
        Bitmap green_contour_bitmap = Bitmap.createBitmap(return_frame.width(), return_frame.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(return_frame, green_contour_bitmap);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//              [processing] 4) change cam preview to processed Image view
                processedImgView.setVisibility(View.VISIBLE);
//              [processing] 5) enroll onTouchListener here,
//                              because in onCreate() scope, there doesn't exist mat and bitmap
                processedImgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (processedImgView_touch_flag) {
                            processedImgView.setImageBitmap(bitmap);
                            processedImgView_touch_flag = false;
                        } else {
                            processedImgView.setImageBitmap(green_contour_bitmap);
                            processedImgView_touch_flag = true;
                        }
                    }
                });
                processedImgView.setImageBitmap(green_contour_bitmap);
                replaceView(mRealTimeCameraView, processedImgView);

                restart_cam_btn.setVisibility(View.VISIBLE);
            }
        });

//      [processing] 6) get to process text_recognition & open custom browser
        processOcr(bitmap, resultTextView, this);

        return (return_frame);
    }

    @Override
    public boolean onLongClick(View v) {
        printLog("Take a long_click photo!");
        mRealTimeCameraView.takePhoto();
        return true;
    }

    @Override
    public void onPictureTaken(byte[] picture) {
        printLog("onPictureTaken() saves image");
        Bitmap bitmap = byteArrayToBitmap(picture);
        bitmap = rotateBitmapImage(bitmap, 90);
        ImageIO.saveImageBitmap(bitmap, savePath());
        thumView.setVisibility(View.VISIBLE);
        thumView.setImageBitmap(bitmap);
    }

// =================================================================================================
// =================================================================================================
// ================================ OpenCV Method Area End =========================================
// =================================================================================================
// =================================================================================================
}