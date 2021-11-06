package com.graduate.lookatv2;

import static com.graduate.lookatv2.camview.ImageIO.byteArrayToBitmap;
import static com.graduate.lookatv2.camview.ImageIO.savePath;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.graduate.lookatv2.camview.GetContours;
import com.graduate.lookatv2.camview.GetTargetContour;
import com.graduate.lookatv2.camview.ImageIO;
import com.graduate.lookatv2.camview.RealTimeCamera;
import com.graduate.lookatv2.camview.RealTimeProcessor;
import com.graduate.lookatv2.camview.SortPointArray;
import com.graduate.lookatv2.camview.TransformPerspective;
import com.graduate.lookatv2.commu.Connect;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.speech.tts.TextToSpeech;


public class SearchActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnLongClickListener,
        RealTimeCamera.PictureResult {
    private static final String TAG = "AndroidOpenCv";
    private RealTimeCamera mRealTimeCameraView;
    private final RealTimeProcessor mRealTimeProcessor = new RealTimeProcessor();
    private int mMode;

    private Button pictureBtn;
    private ImageView processedImgView;
    private ImageView thumView;
    private TextView resultTextView;
    private List<String> serialNum;

//  TTS
    private TextToSpeech tts;

//    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);

    static {
        System.loadLibrary("opencv_java4");
//        System.loadLibrary("native-lib");
    }

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

//      thumnail imageview
        thumView = findViewById(R.id.thumVeiw);
        thumView.setBackground(new ShapeDrawable(new OvalShape()));
        thumView.setClipToOutline(true);
        thumView.setVisibility(View.GONE);
        thumView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//              from do it android programming 94th lecture
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(intent, 101);
                return true;
            }
        });



//      processed imgview
        processedImgView = findViewById(R.id.processed_img_view);
        processedImgView.setVisibility(View.GONE);

//      result TextView
        resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setVisibility(View.GONE);


//      debugging log
        Log.d(TAG, "onCreate: end line");

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=android.speech.tts.TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
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

        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
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
//        final Mat res = mRealTimeProcessor.process(rgba);
        final Mat grayMat = new Mat();
        Mat rotateImg = rotateImg(rgba, 270);
        Imgproc.cvtColor(rotateImg, grayMat, Imgproc.COLOR_RGB2GRAY, 4);
        Imgproc.GaussianBlur(grayMat, grayMat, new Size(3, 3), 0);

        final Mat cannedMat = new Mat();
        Imgproc.Canny(grayMat, cannedMat, 75, 200);

        final GetContours getContours = new GetContours(cannedMat);
        final List<MatOfPoint> contours = getContours.contours();
            // Do nothing if contours is empty
            if (contours.isEmpty()) {
                return (rgba);
        }
        // Get the target contour
        final Scalar mScalarGreen = new Scalar(0, 255, 0);
        final Mat target = new GetTargetContour(contours).target();
//        if (target != null) {
//            Imgproc.drawContours(rotateImg, Collections.singletonList(new MatOfPoint(target)),
//                    -1, mScalarGreen, 3);
//          ######## Here is starting point ########
//
//            target.release();
//        }
        //          ######## Here is starting point ########
        if (target == null) {
            Log.w(TAG, "Can't find target contour, aborting...");
            return (rotateImg);
        }

        // Sort points
        final Point[] points = new MatOfPoint(target).toArray();
        final Point[] orderedPoints = new SortPointArray(points).sort();
        Log.d(TAG, "Points: " + Arrays.toString(orderedPoints));

        // Now apply perspective transformation
        final TransformPerspective transformPerspective = new TransformPerspective(
                points, rotateImg);
        final Mat transformed = transformPerspective.transform();

        // With the transformed points, now convert the image to gray scale
        // and threshold it to give it the paper effect
        Imgproc.cvtColor(transformed, transformed, Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(transformed, transformed, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 21, 10);

        final Size transformedSize = transformed.size();
        final int resultW = (int) transformedSize.width;
        final int resultH = (int) transformedSize.height;

        final Mat result = new Mat(resultH, resultW, CvType.CV_8UC4);
        transformed.convertTo(result, CvType.CV_8UC4);
        Core.flip(result, result, 0);

        final Bitmap bitmap = Bitmap.createBitmap(resultW, resultH, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, bitmap);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String filepath = ImageIO.savePath();
                ImageIO.saveImageBitmap(bitmap, filepath);
                processedImgView.setVisibility(View.VISIBLE);
                processedImgView.setImageBitmap(bitmap);
                replaceView(mRealTimeCameraView, processedImgView);

//              OCR process
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
                FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                        .getCloudTextRecognizer();
                FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
                        .setLanguageHints(Arrays.asList("ko", "hi"))
                        .build();
                Task<FirebaseVisionText> result =
                        detector.processImage(image)
                                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                    @Override
                                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                                    Log.d(TAG, "OCR: Success");
                                                    pictureBtn.setVisibility(View.GONE);
                                                    resultTextView.setVisibility(View.VISIBLE);
                                                    List<FirebaseVisionText.TextBlock> textBlocks = firebaseVisionText.getTextBlocks();
                                                    for (FirebaseVisionText.TextBlock textBlock : textBlocks) {
                                                        List<FirebaseVisionText.Line> lines = textBlock.getLines();
                                                        for (FirebaseVisionText.Line line : lines) {
                                                            String str = line.getText();
                                                            resultTextView.append(str);
                                                            boolean flag = serialNumFilter(str);
                                                            if (flag) {
                                                                resultTextView.append("\nthis is succeed project: " + str);
                                                                String substr = "";
                                                                try {
                                                                    substr = str.substring(str.indexOf("-") - 6);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                resultTextView.append("\nparsed string: " + substr + "\n");
                                                                List<String> inputMsg = communicateServer(substr);
                                                                if (inputMsg.isEmpty())
                                                                    Log.d(TAG + "--Result", "Empty result");
                                                                else {
                                                                    Log.d(TAG + "--Result", String.valueOf(inputMsg.size()));
                                                                    tts.setPitch(1.0f);
                                                                    tts.setSpeechRate(0.85f);
                                                                    int i = 0;
                                                                    while (i < inputMsg.size()) {
                                                                        Log.d(TAG + "--Result", inputMsg.get(i));
                                                                        parsingJson(inputMsg.get(i));
                                                                        i++;
                                                                    }
                                                                }
                                                            } else {
                                                                resultTextView.append("\nfail");
                                                            }
                                                        }
                                                    }
                                                }
                                            })
                                .addOnFailureListener(
                    new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "OCR: Fail");
                        }
                    });
            }
        });
        // release not needed mat
        cannedMat.release();
        grayMat.release();
        Imgproc.drawContours(rotateImg, Collections.singletonList(new MatOfPoint(target)),
                -1, mScalarGreen, 3);
        return (rotateImg);
    }

    private void parsingJson(String src) {
        if (src == null)
            return ;
        JSONObject obj = null;
        List<String> keyNameList = new ArrayList<String>();
        List<String> objValueList = new ArrayList<String>();
        try {
            obj = new JSONObject(src);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (obj == null)
            return ;
        Iterator key_list = obj.keys();
        int k = 0;
        while(key_list.hasNext()) {
            String keyName = key_list.next().toString();
            Log.d(TAG, String.valueOf(k) + " : " + keyName);
            keyNameList.add(keyName);
            k++;
        }
        int i = 0;
        while (i < keyNameList.size()) {
            try {
                objValueList.add(obj.getString(keyNameList.get(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                i++;
            }
        }
        int j = 0;
        while (j < objValueList.size()) {
            tts.speak(objValueList.get(j), TextToSpeech.QUEUE_ADD, null);
            j++;
        }
    }

    private static boolean serialNumFilter(String str) {
        boolean flag = false;
        if (str == null) {
            return (flag);
        }
//        String pattern = "^[a-zA-Z0-9]+-[a-zA-Z0-9]+$";
        String pattern = "^[\\S|\\s]+-[a-zA-Z0-9]+$";
        flag = Pattern.matches(pattern, str);
        return (flag);
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
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

    @Override
    public void onPictureTaken(byte[] picture) {
        Bitmap bitmap = byteArrayToBitmap(picture);
        bitmap = rotateImage(bitmap, 90);
        ImageIO.saveImageBitmap(bitmap, savePath());
        thumView.setVisibility(View.VISIBLE);
        thumView.setImageBitmap(bitmap);

    }

    public Mat rotateImg(Mat src, double angle){
        Log.d("Test_log", String.valueOf(src.type()));
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        Mat rotMat = new Mat(2, 3, CvType.CV_32FC1);
        Point center = new Point(dst.cols() / 2, dst.rows() / 2);
        rotMat = Imgproc.getRotationMatrix2D(center, angle, 1);
        Imgproc.warpAffine(src, dst, rotMat, dst.size());
        return dst;
    }

    public static ViewGroup getParent(View view) {
        return (ViewGroup)view.getParent();
    }

    public static void removeView(View view) {
        ViewGroup parent = getParent(view);
        if(parent != null) {
            parent.removeView(view);
        }
    }

    public static void replaceView(View currentView, View newView) {
        ViewGroup parent = getParent(currentView);
        if(parent == null) {
            return;
        }
        final int index = parent.indexOfChild(currentView);
        removeView(currentView);
        removeView(newView);
        parent.addView(newView, index);
    }

    public List<String> communicateServer(String outputMSG) {
        List<String> inputMSG;
        Connect con = new Connect();
        inputMSG = con.connect(outputMSG);
        return (inputMSG);
    }
}

