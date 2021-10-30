package com.graduate.lookatv2.camview;

import android.os.Environment;

public class Constant {
    public static final int FRAME_MAX_WIDTH = 1280;
    public static final int FRAME_MAX_HEIGHT = 720;
    public static final String ROOT_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ "/lookat";
}
