package com.graduate.lookatv2.utils;

import static com.graduate.lookatv2.utils.PrintUtil.printLog;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

public class TransformUtil {
    public static Bitmap rotateBitmapImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static Mat rotateMatImage(Mat src, double angle){
        printLog("rotateImage() Type: " + src.type());
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        Mat rotMat = new Mat(2, 3, CvType.CV_32FC1);
        Point center = new Point(dst.cols() / 2, dst.rows() / 2);
        rotMat = Imgproc.getRotationMatrix2D(center, angle, 1);
        Imgproc.warpAffine(src, dst, rotMat, dst.size());
        return dst;
    }
}
