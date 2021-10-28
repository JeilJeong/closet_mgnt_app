package com.graduate.lookatv2.camview;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;

public final class RealTimeProcessor {

    private final Scalar mScalarGreen = new Scalar(0, 255, 0);

    /**
     * Transform in real time the given {@link Mat}. Do not call release in this {@link Mat}
     *
     * @param original Current screen Mat
     */
    public Mat process(Mat original) {
        final Mat grayMat = new Mat();
        Mat rotateImg = rotateImg(original, 270);
        Imgproc.cvtColor(rotateImg, grayMat, Imgproc.COLOR_RGB2GRAY, 4);
        Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5), 0);

        final Mat cannedMat = new Mat();
        Imgproc.Canny(grayMat, cannedMat, 75, 200);

        final GetContours getContours = new GetContours(cannedMat);
        final List<MatOfPoint> contours = getContours.contours();
        // Do nothing if contours is empty
        if (contours.isEmpty()) {
            return (original);
        }
        // Get the target contour
        final Mat target = new GetTargetContour(contours).target();
        if (target != null) {
            Imgproc.drawContours(rotateImg, Collections.singletonList(new MatOfPoint(target)),
                    -1, mScalarGreen, 3);
            target.release();
        }

        // release not needed mat
        cannedMat.release();
        grayMat.release();
        return (rotateImg);
    }

    public Mat rotateImg(Mat src, double angle) {
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        Mat rotMat = new Mat(2, 3, CvType.CV_32FC1);
        Point center = new Point(dst.cols() / 2, dst.rows() / 2);
        rotMat = Imgproc.getRotationMatrix2D(center, angle, 1);
        Imgproc.warpAffine(src, dst, rotMat, dst.size());
        return dst;
    }
}