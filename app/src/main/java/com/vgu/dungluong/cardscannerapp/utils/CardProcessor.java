package com.vgu.dungluong.cardscannerapp.utils;

import android.graphics.Bitmap;

import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import io.reactivex.Observable;
import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2Lab;
import static org.opencv.imgproc.Imgproc.COLOR_BGRA2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGRA;
import static org.opencv.imgproc.Imgproc.COLOR_Lab2BGR;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;
import static org.opencv.imgproc.Imgproc.MORPH_GRADIENT;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.line;
import static org.opencv.imgproc.Imgproc.warpPerspective;

/**
 * Created by Dung Luong on 19/06/2019
 */
public class CardProcessor {

    public static final String TAG = "CardProcessor";

    private CardProcessor() {

    }

    @Nullable
    public static Corners processPicture(Mat previewFrame) {
        return findContours(previewFrame);
    }

    // Crop picture based on 4 corners
    public static Mat cropPicture(Mat picture, List<Point> pts) {
        //pts = sortPoints(pts);
        Point tl = pts.get(0);
        Point tr = pts.get(1);
        Point br = pts.get(2);
        Point bl = pts.get(3);

        double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2.0D) + Math.pow(br.y - bl.y, 2.0D));
        double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2.0D) + Math.pow(tr.y - tl.y, 2.0D));

        double dw = Math.max(widthA, widthB);
        int maxWidth = (int) dw;

        double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2.0D) + Math.pow(tr.y - br.y, 2.0D));
        double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2.0D) + Math.pow(tl.y - bl.y, 2.0D));

        double dh = Math.max(heightA, heightB);
        int maxHeight = (int) dh;

        Mat croppedPic = new Mat(maxHeight, maxWidth, CvType.CV_8UC1);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x, tl.y, tr.x, tr.y, br.x, br.y, bl.x, bl.y);
        dst_mat.put(0, 0, 0.0D, 0.0D, dw, 0.0D, dw, dh, 0.0D, dh);

        // Calculate a perspective transform from four pairs of the corresponding points
        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        // Applies a perspective transformation to an image
        Imgproc.warpPerspective(picture, croppedPic, m, croppedPic.size());

        m.release();
        src_mat.release();
        dst_mat.release();
        AppLogger.i(TAG + " crop finish");
        return croppedPic;
    }

    @NotNull
    public static final Bitmap enhancePicture(Bitmap src) {
        Mat src_mat = new Mat();
        Utils.bitmapToMat(src, src_mat);
        Imgproc.cvtColor(src_mat, src_mat, 11);
        Imgproc.adaptiveThreshold(src_mat, src_mat, 255.0D, 0, 0, 15, 15.0D);
        Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(src_mat, result, true);
        src_mat.release();
        Intrinsics.checkExpressionValueIsNotNull(result, "result");
        return result;
    }

    private static Corners findContours(Mat src) {
        double coeff = 0.1;
        // Down sample
        Size croppedSize = new Size(src.size().width * coeff, src.size().height * coeff);
        Mat resizeMat = src.clone();
        Imgproc.resize(resizeMat, resizeMat, croppedSize);
        Mat canny = new Mat(croppedSize, CvType.CV_8UC1);

        // Do contour detection
        Imgproc.Canny(resizeMat, canny, 30, 90, 3);
        Mat lines = new Mat();

        // Do hough transform
        Imgproc.HoughLinesP(canny, lines, 1, Math.PI / 180, 70, 10, 1);
        AppLogger.i("Number of lines: " + lines.rows());

        return findEdges(lines, croppedSize, src, 1 / coeff);
    }

    private static List<Point> sortPoints(List<Point> points, Mat img) {
        AppLogger.i(String.valueOf(points.size()));
        Point center = new Point(img.size().width / 2, img.size().height / 2);
        List<Point> maxTl = points.stream().sorted(Comparator.comparing(tl -> tl.x + tl.y)).collect(Collectors.toList());
        List<Point> maxTr = points.stream().sorted((tr1, tr2) -> Double.compare(tr2.x - tr2.y, tr1.x - tr1.y)).collect(Collectors.toList());
        List<Point> maxBr = points.stream().sorted((br1, br2) -> Double.compare(br2.x + br2.y, br1.x + br1.y)).collect(Collectors.toList());
        List<Point> maxBl = points.stream().sorted(Comparator.comparing(bl -> bl.x - bl.y)).collect(Collectors.toList());

        Point p0 = points.stream().min(Comparator.comparing(point -> point.x + point.y)).orElse(new Point());
        Point p1 = points.stream().max(Comparator.comparing(point -> point.x - point.y)).orElse(new Point());
        Point p2 = points.stream().max(Comparator.comparing(point -> point.x + point.y)).orElse(new Point());
        Point p3 = points.stream().min(Comparator.comparing(point -> point.x - point.y)).orElse(new Point());

        int iteration = points.size() / 2;
        outerLoop:
        for(int i = 0; i < iteration; i++) {
            AppLogger.i(String.valueOf(i));
            for (int j = 0; j < iteration; j++) {
                for (int k = 0; k < iteration; k++) {
                    if (!isRightAngle(angle(maxTr.get(j), maxTl.get(i), maxBr.get(k))))
                        continue ;
                    for (int l = 0; l < iteration; l++) {
                        if (!isRightAngle(angle(maxBr.get(k), maxTr.get(j), maxBl.get(l))))
                            continue;
                        if (!isRightAngle(angle(maxBl.get(l), maxBr.get(k), maxTl.get(i))))
                            continue;
                        if (!isRightAngle(angle(maxTl.get(i), maxBl.get(l), maxTr.get(j))))
                            continue;
                        double height = distance2Points(maxTr.get(j), maxBr.get(k));
                        double width = distance2Points(maxTl.get(i), maxTr.get(j));
                        double ratio = height / width;
                        if (ratio > 1.62 && ratio < 1.65) {
                            AppLogger.i("found");
                            AppLogger.i(height + " " + width + " " + (img.size().area() / 4));
                            if(height * width > img.size().area() / 4) {
                                AppLogger.i(String.valueOf(ratio));
                                p0 = maxTl.get(i);
                                p1 = maxTr.get(j);
                                p2 = maxBr.get(k);
                                p3 = maxBl.get(l);
                                break outerLoop;
                            }
                        }
                    }
                }
            }
        }
        return Arrays.asList(p0, p1, p2, p3);
    }

    public static Observable<Boolean> textSkewCorrection(Mat img, boolean isBlackScan) {
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(img, img, new Size(3, 3), 0);
        Mat kernel = Imgproc.getStructuringElement(MORPH_ELLIPSE, new Size(30, 30));
        Mat closed = new Mat();
        Imgproc.morphologyEx(img, closed, Imgproc.MORPH_CLOSE, kernel);

        img.convertTo(img, CvType.CV_32F); // divide requires floating-point
        Core.divide(img, closed, img, 1, CvType.CV_32F);
        Core.normalize(img, img, 0, 255, Core.NORM_MINMAX);
        img.convertTo(img, CvType.CV_8UC1); // convert back to unsigned int
        Imgproc.threshold(img, img, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        Mat kernel1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Mat kernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Imgproc.morphologyEx(img, img, MORPH_CLOSE, kernel1);
        Imgproc.morphologyEx(img, img, MORPH_OPEN, kernel2);
        //Imgproc.erode(img, img, kernel2, new Point(-1, -1), 1);

        Mat lines = new Mat();
        Imgproc.HoughLinesP(img, lines, 1, Math.PI / 160, 100, 10, 20);
        double angle = 0.;
        AppLogger.i(lines.height() + " " + lines.width() + " " + lines.rows() + " " + lines.cols());
        for (int i = 0; i < lines.height(); i++) {
            for (int j = 0; j < lines.width(); j++) {
                angle += Math.atan2(lines.get(i, j)[3] - lines.get(i, j)[1], lines.get(i, j)[2] - lines.get(i, j)[0]);
            }
        }

        angle /= lines.size().area();
        angle = angle * 180 / Math.PI;
        AppLogger.i(String.valueOf(angle));

        if (!Double.isNaN(angle)) {
            Mat white = new Mat(img.size(), CvType.CV_8UC1);
            Core.findNonZero(img, white);
            MatOfPoint points = new MatOfPoint(white);
            MatOfPoint2f points2f = new MatOfPoint2f(points.toArray());
            RotatedRect box = Imgproc.minAreaRect(points2f);
            AppLogger.i(String.valueOf(box.angle));
            if (box.angle != 0.0 && box.angle != -90.0 && box.angle == 90.0) {
                return Observable.just(deSkew(img, angle, box));
            }
        }
        return Observable.just(false);
    }

    private static boolean deSkew(Mat img, double angle, RotatedRect box) {
        Mat rotMat = Imgproc.getRotationMatrix2D(box.center, angle, 1);
        Imgproc.warpAffine(img, img, rotMat, img.size(), INTER_CUBIC);
        return true;
    }

    // Find 4 edges based on the line of hough transform
    private static Corners findEdges(Mat lines, Size croppedSize, Mat img, double cropScale) {
        List<Point> points = new ArrayList<>();
        List<Point> intersections = new ArrayList<>();
        for (int i = 0; i < lines.rows(); i++) {
            double[] val = lines.get(i, 0);
//            Imgproc.line(img, new Point(val[0] * cropScale, val[1] * cropScale), new Point(val[2] * cropScale, val[3] * cropScale), new Scalar(0, 0, 255), 3);
            // find y = a x + c
            double a;
            double c;
            double D = val[0] - val[2];
            double Da = val[1] - val[3];
            double Dc = val[0]*val[3] - val[1]*val[2];
            // Extend the line
            if(D != 0) {
                a = Da / D;
                c = Dc / D;
                double x1 = -croppedSize.width;
                double x2 = croppedSize.width;
                double y1 = a * x1 + c;
                double y2 = a * x2 + c;
                points.add(new Point(x1, y1));
                points.add(new Point(x2, y2));
                Imgproc.line(img, new Point(x1 * cropScale, y1 * cropScale), new Point(x2 * cropScale, y2 * cropScale), new Scalar(0, 0, 255), 2);
            }else{
                double y1 = -croppedSize.height;
                double y2 = croppedSize.height;
                points.add(new Point(val[0], y1));
                points.add(new Point(val[2], y2));
                Imgproc.line(img, new Point(val[0] * cropScale, y1 * cropScale), new Point(val[2] * cropScale, y2 * cropScale), new Scalar(0, 0, 255), 2);
            }
        }

        // Get all the intersection points
        for(int i = 0; i < points.size() - 2; i += 2){
            for(int j = i; j < points.size(); j += 2){
                Point intersection = intersection(points.get(i), points.get(i+1), points.get(j), points.get(j+1), croppedSize, cropScale);
                if(intersection != null){
                    intersections.add(intersection);
                }
            }
        }

        AppLogger.i("number of intersections:" + String.valueOf(intersections.size()));
        // Select 4 edges
        return new Corners(sortPoints(intersections, img), new Size(croppedSize.width * cropScale, croppedSize.height * cropScale));
    }

    // Find intersection point between lines
    private static Point intersection(Point o1, Point p1, Point o2, Point p2, Size size, double cropScale) {
        Point x = new Point(o2.x - o1.x, o2.y - o1.y);
        Point d1 = new Point(p1.x - o1.x, p1.y - o1.y);
        Point d2 = new Point(p2.x - o2.x, p2.y - o2.y);
        double cross = d1.x * d2.y - d1.y * d2.x;

        // Parallel line
        if (Math.abs(cross) < AppConstants.EPISILON)
            return null;

        double t1 = (x.x * d2.y - x.y * d2.x) / cross;
        Point intersect = new Point(o1.x + d1.x * t1, o1.y + d1.y * t1);
        // Intersect over the area
        if (intersect.x > 0 && intersect.y > 0 && intersect.x <= size.width && intersect.y <= size.height) {
            double angle = angle(intersect, o1, o2);
            // Choose intersection that has value near 90 degree
            if (isRightAngle(angle)) return new Point((Math.floor(intersect.x * 1000)) * cropScale / 1000, (Math.floor(intersect.y * 1000)) * cropScale / 1000);
            else return null;
        } else {
            return null;
        }
    }

    private static double distance2Points(Point a, Point b) {
        return Math.sqrt(Math.pow((a.x - b.x), 2.0) + Math.pow((a.y - b.y), 2.0));
    }

    private static boolean isRightAngle(double angle){
        return (angle > 88.9 && angle < 91.1);
    }

    // Find angle of two lines that are intersected
    private static double angle(Point p1, Point p2, Point p3){
        double dx21 = p2.x - p1.x;
        double dx31 = p3.x - p1.x;
        double dy21 = p2.y - p1.y;
        double dy31 = p3.y - p1.y;
        double m12 = Math.sqrt(dx21 * dx21 + dy21 * dy21);
        double m13 = Math.sqrt(dx31 * dx31 + dy31 * dy31);
        return (Math.acos((dx21*dx31 + dy21*dy31) / (m12 * m13)) * 180.0) / Math.PI;
    }
}
