package com.graduate.lookatv2.commu;

import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Connect {
    private Handler mHandler;
    private Socket socket;
    private List<String> msgList = new ArrayList<String>();
    private Socket client;
    private DataOutputStream dataOutput;
    private DataInputStream dataInput;
    private static String SERVER_IP = "172.30.16.172";
    private static String CONNECT_MSG = "connect";
    private static String STOP_MSG = "stop";
    private static int PORT = 8080;

    private static int BUF_SIZE = 1024;

    private static final String TAG = "AndroidOpenCv: " + Connect.class.getSimpleName();

    public List<String> connect(String outputMSG){
        mHandler = new Handler();
        Log.d(TAG,"connecting...");
        Thread conn = new Thread() {
            public void run() {
                try {
                    socket = new Socket(SERVER_IP, PORT);
                    Log.d(TAG, "connected with server...");
                } catch (IOException e1) {
                    Log.d(TAG, "can't connect...");
                    e1.printStackTrace();
                }

                try {
                    dataOutput = new DataOutputStream(socket.getOutputStream());
                    dataInput = new DataInputStream(socket.getInputStream());
                    dataOutput.writeUTF(outputMSG);
                    dataOutput.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                while (true){
                    try {
                    byte[] buf = new byte[BUF_SIZE];
                    int read_Byte  = dataInput.read(buf);
                    String input_message = new String(buf, 0, read_Byte, "UTF-8");
                    if (!input_message.equals(STOP_MSG)){
                        Log.d(TAG, input_message);
                        msgList.add(input_message);
                    }
                    else{
                        Log.d(TAG, input_message);
                        break;
                    }
                    Thread.sleep(2);
                } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Log.d(TAG, "before starting");
        conn.start();
        try {
            conn.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "after starting");
        return (msgList);
    }

    public static List<String> communicateServer(String outputMSG) {
        if (outputMSG == null)
            return (null);
        List<String> inputMSG;
        Connect con = new Connect();
        inputMSG = con.connect(outputMSG);
        return (inputMSG);
    }
}

