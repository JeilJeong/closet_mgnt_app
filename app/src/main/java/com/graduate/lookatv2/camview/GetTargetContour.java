package com.graduate.lookatv2.camview;

import com.graduate.lookatv2.SearchActivity;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public final class GetTargetContour {

    private static final int AREA_THRESHOLD = 700;
    private static final int FRAME_SIZE = Constant.FRAME_MAX_WIDTH * Constant.FRAME_MAX_HEIGHT;

    private final List<MatOfPoint> mContours;

    public GetTargetContour(List<MatOfPoint> contours) {
        mContours = contours;
    }

    public Mat target() {
        final MatOfPoint2f approx = new MatOfPoint2f();

        Mat target = null;
        for (MatOfPoint contour : mContours) {
            final MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());

            // Here we approximate the number of contour points
            final double approxDistance = Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, approx, approxDistance * 0.02, true);

            final MatOfPoint points = new MatOfPoint(approx.toArray());
            final int pointsInt = (int) points.total();
            // Calculate the rectangle area to discard small contours
            final double area = Imgproc.contourArea(points);
            final double ratio = area / FRAME_SIZE;
            // Now if the approximated contour has four points, we assume that we have
            // found the document
            if (pointsInt == 4 && ratio > 0.1) {
                target = points;
                break;
            }
        }
        approx.release();

        return target;
    }
}