package com.graduate.lookatv2.camview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageIO {
    private static final String TAG = "AndroidOpenCv" + ImageIO.class.getSimpleName();
    private static final String ROOT_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ "/lookat";

    public static boolean saveImagetoText(byte[] picture, String saveImageName) {
        String saveDir = ROOT_DIRECTORY;
        File file = new File(saveDir);
        if (!file.exists()) {
            file.mkdir();
        }

        String fileName = saveImageName + ".txt";
        File tempFile = new File(saveDir, fileName);
        FileOutputStream output = null;
        try {
            if (tempFile.createNewFile()) {
                output = new FileOutputStream(tempFile);
                output.write(picture);
            } else {
                Log.d(TAG, "Same file exists:"+saveImageName);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static boolean saveImage(byte[] picture, String saveImageName) {
        String saveDir = ROOT_DIRECTORY;
        File file = new File(saveDir);
        if (!file.exists()) {
            file.mkdir();
        }

        Bitmap bitmap = byteArrayToBitmap(picture);

        String fileName = saveImageName + ".jpeg";
        File tempFile = new File(saveDir, fileName);
        FileOutputStream output = null;

        try {
            if (tempFile.createNewFile()) {
                output = new FileOutputStream(tempFile);
                Bitmap newBitmap = bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
                newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            } else {
                Log.d(TAG, "Same file exists:"+saveImageName);

                return false;
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "can't find file name");
            return false;

        } catch (IOException e) {
            Log.d(TAG, "Error in IO");
            e.printStackTrace();
            return false;

        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return bitmap;
    }

    public static Bitmap loadImage(String filename) {
        Bitmap myBitmap = null;
        try {
            File filepath = new File(ROOT_DIRECTORY + "/" + filename);
            if (filepath.exists()){
                myBitmap = BitmapFactory.decodeFile(filepath.getAbsolutePath());
            }
            return myBitmap;
        } catch (Exception e) {
            Log.d(TAG,"Can't load image");
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] loadTxt(String path) {
        File file = new File(ROOT_DIRECTORY + "/" + path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    public static File saveImageBitmap(Bitmap bmp) {
        if (bmp == null) {
            Log.d(TAG, "saveImageBitmap: null");
            return null;
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String filename = new SimpleDateFormat("yyyyMMdd_HH_mm_ss").format(new Date());
        File f = new File(ROOT_DIRECTORY + "/" + filename + ".jpeg");
        FileOutputStream fo = null;
        try {
            f.createNewFile();
            fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }
}
