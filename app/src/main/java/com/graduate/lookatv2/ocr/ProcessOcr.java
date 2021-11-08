package com.graduate.lookatv2.ocr;

import static com.graduate.lookatv2.commu.Connect.communicateServer;
import static com.graduate.lookatv2.utils.PrintUtil.printLog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ProcessOcr {
    private static TextToSpeech tts;
    private static TextView resultTextView;
    private static boolean check_after_processing_all_lines;
    private static String serial_num;

    public static String processOcr(Bitmap bitmap, TextView src_resultTextView, Activity activity) {
        printLog("processOcr() start");
        resultTextView = src_resultTextView;
        check_after_processing_all_lines = false;
        serial_num = null;

//      [processing] 1) Text recognition start
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getCloudTextRecognizer();
        FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setLanguageHints(Arrays.asList("ko", "hi"))
                .build();

//      [processing] 2) Async task start
        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
//                              [processing] 2-1) processing for succeed recognition
                                printLog("processOcr() onSuccess() start");

                                List<FirebaseVisionText.TextBlock> textBlocks = firebaseVisionText.getTextBlocks();
                                for (FirebaseVisionText.TextBlock textBlock : textBlocks) {
                                    List<FirebaseVisionText.Line> lines = textBlock.getLines();
                                    for (FirebaseVisionText.Line line : lines) {
//                                      [processing] 2-1-1) parsing serial number from recognized text by using regex
                                        String searching_str = processEachLine(line.getText());
                                        if (check_after_processing_all_lines == true) {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    resultTextView.setText("인식된 상품 번호: " + searching_str);
                                                    //  Custom tab test
                                                    String url = "https://msearch.shopping.naver.com/search/all?query=" + searching_str;
                                                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                                                    CustomTabsIntent customTabsIntent = builder.build();
                                                    customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                                    customTabsIntent.launchUrl(activity.getApplicationContext(), Uri.parse(url));
                                                }
                                            });
                                            break;
                                        }
                                    }
                                    if (check_after_processing_all_lines == true)
                                        break;
                                }
                                if (!check_after_processing_all_lines) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            resultTextView.setText("개별 텍스트의 품번 인식에 실패했습니다.\n다시 시도해주세요.");
                                        }
                                    });
                                }
                                printLog("processOcr() onSuccess() end");
                            }
                        })
                        .addOnFailureListener( new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//                              [processing] 2-2) failed to recognition text
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        resultTextView.setText("품번 인식에 실패했습니다.\n다시 시도해주세요.");
                                    }
                                });
                                printLog("processOcr() onFailure() Text recognition failed");
                            }
                        });
        printLog("processOcr() end");
        return (serial_num);
    }

    private static String processEachLine(String str) {
        if (str == null)
            return (null);
        boolean flag = serialNumFilter(str);
        String substr = null;

        if (flag) {
            check_after_processing_all_lines = true;
            try {
                substr = str.substring(str.indexOf("-") - 6);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (substr);
    }

    private static boolean serialNumFilter(String str) {
        boolean flag = false;
        if (str == null) {
            return (flag);
        }
//      [regex] whitespace & - & any character
        String pattern = "^[\\S|\\s]+-[a-zA-Z0-9]+$";
        flag = Pattern.matches(pattern, str);
        return (flag);
    }
}
