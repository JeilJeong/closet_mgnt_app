package com.graduate.lookatv2.camview;

import static com.graduate.lookatv2.utils.TransformUtil.rotateMatImage;
import static com.graduate.lookatv2.utils.PrintUtil.printLog;

import android.graphics.Bitmap;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreProcessImage {
    public static Map getPreProcessedImage(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//      [value] for return
        Map resultObj = new HashMap<>();

//      [processing] 1) rotate image - need to change by specific environment
//                   2) convert color
//                   3) Gaussian blur
        final Mat rgba = inputFrame.rgba();
        final Mat grayMat = new Mat();
        Mat rotateImg = rotateMatImage(rgba, 270);

        resultObj.put(Constant.PROCESSING_IMAGE_RETURN_FRAME_KEY, rotateImg);

        Imgproc.cvtColor(rotateImg, grayMat, Imgproc.COLOR_RGB2GRAY, 4);
        Imgproc.GaussianBlur(grayMat, grayMat, new Size(3, 3), 0);

//      [processing] 4) Canny edge detection - need to adjust threshold value by heuristic
        final Mat cannedMat = new Mat();
        Imgproc.Canny(grayMat, cannedMat, 75, 200);

//      [processing] 5) get contours - get 5 contours that has maximum area
        final GetContours getContours = new GetContours(cannedMat);
        final List<MatOfPoint> contours = getContours.contours();
//                   5-1) return contours - if there doesn't exist contours
        if (contours.isEmpty()) {
            printLog("Can't find contours");
            return (resultObj);
        }

//      [processing] 6) get target contours
//                      - check contours area has closed area and minimum ratio over the screen size
        final Mat target = new GetTargetContour(contours).target();
//                   6-1) return contours
//                        - if there doesn't exist contours area that enough to square shape condition
        if (target == null) {
            printLog("Can't find @@ target @@ contour, aborting...");
            return (resultObj);
        }

//      [processing] 7) sort points and align position along to fixed width, height
        final Point[] points = new MatOfPoint(target).toArray();
        final Point[] orderedPoints = new SortPointArray(points).sort();
        printLog("Points: " + Arrays.toString(orderedPoints));

//      [processing] 8) apply perspective transform to image
        final TransformPerspective transformPerspective = new TransformPerspective(
                points, rotateImg);
        final Mat transformed = transformPerspective.transform();


//      [processing] 9) With the transformed points, now convert the image to gray scale
//                      and threshold it to give it the paper effect
        Imgproc.cvtColor(transformed, transformed, Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(transformed, transformed, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 21, 10);

        final Size transformedSize = transformed.size();
        final int resultW = (int) transformedSize.width;
        final int resultH = (int) transformedSize.height;

//      [processing] 10) flip gray scaled image - not mandatory
//                       but depending on the situation, it can be helpful to provide a right image
        final Mat result = new Mat(resultH, resultW, CvType.CV_8UC4);
        transformed.convertTo(result, CvType.CV_8UC4);
        Core.flip(result, result, 0);

//      [processing] 11) convert mat to bitmap for the remain processing
        final Bitmap bitmap = Bitmap.createBitmap(resultW, resultH, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, bitmap);

        resultObj.put(Constant.PROCESSING_IMAGE_RETURN_BITMAP_KEY, bitmap);

//      [processing] 12) draw green contours to return image
        final Scalar mScalarGreen = new Scalar(0, 255, 0);
        Imgproc.drawContours(rotateImg, Collections.singletonList(new MatOfPoint(target)),
                -1, mScalarGreen, 3);
        resultObj.remove(Constant.PROCESSING_IMAGE_RETURN_FRAME_KEY);
        resultObj.put(Constant.PROCESSING_IMAGE_RETURN_FRAME_KEY, rotateImg);

        cannedMat.release();
        grayMat.release();

        return (resultObj);
    }
}
