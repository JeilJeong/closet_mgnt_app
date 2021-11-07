package com.graduate.lookatv2.ocr;

import static com.graduate.lookatv2.commu.Connect.communicateServer;
import static com.graduate.lookatv2.utils.PrintUtil.printLog;

import android.graphics.Bitmap;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;

import androidx.annotation.NonNull;

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

public class ProcessingOcrToTts {
    private static TextToSpeech tts;
    private static TextView resultTextView;
    private static boolean check_after_processing_all_lines;

    public static void processOcrToTTS(Bitmap bitmap, TextToSpeech src_tts, TextView src_resultTextView) {
        printLog("processOcrToTTS() start");
        tts = src_tts;
        resultTextView = src_resultTextView;
        check_after_processing_all_lines = false;

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
//                              [processing] 3-1) processing for succeed recognition
                                printLog("processOcrToTTS() onSuccess() start");
//                              [processing] 3-1-1) set basic config to TTS
                                setTtsConfig(tts);

                                List<FirebaseVisionText.TextBlock> textBlocks = firebaseVisionText.getTextBlocks();
                                for (FirebaseVisionText.TextBlock textBlock : textBlocks) {
                                    List<FirebaseVisionText.Line> lines = textBlock.getLines();
                                    for (FirebaseVisionText.Line line : lines) {
//                                      [processing] 3-1-2) parsing serial number from recognized text by using regex
                                        String searching_str = processEachLine(line.getText());
//                                      [processing] 3-1-3) send serial number & receive product info
                                        List<String> product_info = communicateServer(searching_str);
//                                      [processing] 3-1-4) make product info to voice by using tts
                                        processMsgFromServer(product_info);
                                    }
                                }
                                if (!check_after_processing_all_lines) {
                                    speechOutMSG("품번 인식에 실패했습니다. 다시 시도해주세요.");
                                }
                                printLog("processOcrToTTS() onSuccess() end");
                            }
                        })
                        .addOnFailureListener( new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//                              [processing] 3-2) failed to recognition text
                                speechOutMSG("품번 인식에 실패했습니다. 다시 시도해주세요.");
                                printLog("processOcrToTTS() onFailure() Text recognition failed");
                            }
                        });
        printLog("processOcrToTTS() end");
    }

    private static void setTtsConfig(TextToSpeech tts) {
        tts.setPitch(1.0f);
        tts.setSpeechRate(1.0f);
    }

    private static String processEachLine(String str) {
        if (str == null)
            return (null);
        boolean flag = serialNumFilter(str);
        String substr = null;

        if (flag) {
            check_after_processing_all_lines = true;
            speechOutMSG("상품 정보를 불러오는 중입니다. 잠시만 기다려주세요.");
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

    private static void speechOutMSG(String msg) {
        tts.speak(msg, TextToSpeech.QUEUE_ADD, null);
    }

    private static void processMsgFromServer(List<String> inputMsg) {
        if (inputMsg == null)
            return ;
        if (inputMsg.isEmpty()) {
//          [Abnormal_process] speech out when product info arrive abnormally
            speechOutMSG("찾으시는 상품이 존재하지 않습니다. 다른 상품을 검색해주세요.");
            printLog("communicateServer() Empty result");
            return ;
        }

        int i = 0;
        while (i < inputMsg.size()) {
            printLog(inputMsg.get(i));
            List<String> product_info = parsingJson(inputMsg.get(i));
            if (product_info != null) {
                int j = 0;
                while (j < product_info.size()) {
//          [Normal_process] speech out when product info arrive normally
                    speechOutMSG(product_info.get(j));
                    j++;
                }
            }
            i++;
        }
    }

    private static List<String> parsingJson(String src) {
        if (src == null)
            return null;
        JSONObject obj = null;
        List<String> keyNameList = new ArrayList<String>();
        List<String> objValueList = new ArrayList<String>();
        try {
            obj = new JSONObject(src);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (obj == null)
            return null;
        Iterator key_list = obj.keys();
        int k = 0;
        while(key_list.hasNext()) {
            String keyName = key_list.next().toString();
            printLog(k + " : " + keyName);
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
        return (objValueList);
    }
}
