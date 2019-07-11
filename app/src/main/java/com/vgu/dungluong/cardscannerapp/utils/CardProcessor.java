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
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;
import static org.opencv.imgproc.Imgproc.MORPH_GRADIENT;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;
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
    public static Corners processPicture(Mat previewFrame, boolean isBlackScan) {
        return getCorners(findContours(previewFrame, isBlackScan), previewFrame.size(), previewFrame);
    }

    public static Mat cropPicture(Mat picture, List<Point> pts) {
        pts = sortPoints(pts);
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

    private static List<MatOfPoint> findContours(Mat src, boolean isBlackScan) {
        double coeff = 0.25;
        Size size = new Size(src.size().width * coeff, src.size().height * coeff);
        Mat gray = new Mat(size, CvType.CV_8UC1);
        // Edge detection with canny
//        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
//        Imgproc.Canny(gray, gray, 30, 90, 3, true);
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9,9));
//
//        Imgproc.dilate(gray, gray, kernel, new Point(-1, -1), 10);
//        Imgproc.erode(gray, gray, kernel, new Point(-1, -1), 10);

        // Edge detection with threshold
        //   Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
//        CLAHE clahe = Imgproc.createCLAHE();
//        clahe.setClipLimit(6.0);
//        clahe.apply(gray, gray);
        //     Imgproc.blur(gray, gray, new Size(5, 5));

//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(30,30));
//        Mat closed = new Mat();
//        Imgproc.morphologyEx(gray, closed, Imgproc.MORPH_CLOSE, kernel);
//
//        gray.convertTo(gray, CvType.CV_32F); // divide requires floating-point
//        Core.divide(gray, closed, gray, 1, CvType.CV_32F);
//        Core.normalize(gray, gray, 0, 255, Core.NORM_MINMAX);
//        gray.convertTo(gray, CvType.CV_8UC1); // convert back to unsigned int

//        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
        //   Imgproc.adaptiveThreshold(gray, gray, 255.0, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 11, 2);
//        Imgproc.threshold(gray, gray, 0, 255, Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
//        Mat kernel1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
//        Mat kernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2));
//        Imgproc.morphologyEx(gray, gray, MORPH_CLOSE, kernel1);
//        Imgproc.morphologyEx(gray, gray, MORPH_OPEN, kernel2);

        //  double med = median(gray);
//        Imgproc.threshold(gray, gray, 0, 255,
//                (isBlackScan ? Imgproc.THRESH_BINARY_INV : THRESH_BINARY) + THRESH_OTSU);

        //Imgproc.Canny(gray, gray, Math.min(0, (1.0 - AppConstants.CANNY_SIGMA) * med), Math.max(255, 1.0 + AppConstants.CANNY_SIGMA * med));
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2));
//
//        Imgproc.morphologyEx(gray, gray,MORPH_OPEN, kernel);

        // Imgproc.threshold(gray, gray, 0, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);

        Mat morph = src.clone();
        Imgproc.resize(morph, morph, size);
        cvtColor(morph, morph, Imgproc.COLOR_BGRA2GRAY);
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(2.0);
        clahe.apply(morph, morph);
        cvtColor(morph, morph, COLOR_GRAY2BGRA);
        Imgproc.GaussianBlur(morph, morph, new Size(5, 5), 0);
        for (int r = 1; r < 4; r++) {
            Mat kernel = Imgproc.getStructuringElement(MORPH_ELLIPSE, new Size(2 * r + 1, 2 * r + 1));
            Imgproc.morphologyEx(morph, morph, MORPH_CLOSE, kernel);
            Imgproc.morphologyEx(morph, morph, MORPH_OPEN, kernel);
        }

        Mat mgrad = new Mat();
        Mat kernel = Imgproc.getStructuringElement(MORPH_ELLIPSE, new Size(3, 3));
        Imgproc.morphologyEx(morph, mgrad, MORPH_GRADIENT, kernel);

        List<Mat> channels = new ArrayList<>();
        Mat merge = new Mat();
        Core.split(mgrad, channels);
        Imgproc.threshold(channels.get(0), channels.get(0), 0, 255, THRESH_BINARY + THRESH_OTSU);
        Imgproc.threshold(channels.get(1), channels.get(1), 0, 255, THRESH_BINARY + THRESH_OTSU);
        Imgproc.threshold(channels.get(2), channels.get(2), 0, 255, THRESH_BINARY + THRESH_OTSU);
        Core.merge(channels, merge);

        double med = median(merge);
        Imgproc.Canny(merge, gray, Math.min(0, (1.0 - AppConstants.CANNY_SIGMA) * med), Math.max(255, 1.0 + AppConstants.CANNY_SIGMA * med));
        Mat kernel1 = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3));
        Imgproc.morphologyEx(gray, gray, MORPH_CLOSE, kernel1,new Point(-1, -1), 1);

        Mat lines = new Mat();

        List<Pair<Pair<Point, Point>, Double>> longesHorizontal = new ArrayList<>();
        List<Pair<Pair<Point, Point>, Double>> longestVertical = new ArrayList<>();
//
//        for (int i = 0; i < lines.rows(); i++) {
//            double data[] = lines.get(i, 0);
//            double rho1 = data[0];
//            double theta1 = data[1];
//            AppLogger.i(Arrays.toString(data));
//            double cosTheta = Math.cos(theta1);
//            double sinTheta = Math.sin(theta1);
//            double x0 = cosTheta * rho1;
//            double y0 = sinTheta * rho1;
//            Point pt1 = new Point((x0 + size.width * (-sinTheta)) * 4, (y0 + size.height * cosTheta) * 4);
//            Point pt2 = new Point((x0 - size.width * (-sinTheta)) * 4, (y0 - size.height * cosTheta) * 4);
//            Imgproc.line(src, pt1, pt2, new Scalar(0, 0, 255), 2);
//            if (theta1 > 3) {
//
//                longestVertical.add(new Pair<>(new Pair<>(pt1, pt2), distance2Points(pt1, pt2)));
//            } else {
//
//                longesHorizontal.add(new Pair<>(new Pair<>(pt1, pt2), distance2Points(pt1, pt2)));
//            }
//
//        }

        longestVertical = longestVertical.stream().sorted((p1, p2) -> Double.compare(p1.getSecond(), p2.getSecond())).collect(Collectors.toList());
        longesHorizontal = longesHorizontal.stream().sorted(Comparator.comparing(Pair<Pair<Point, Point>, Double>::getSecond)).collect(Collectors.toList());

        for (int i = 0; i < 2; i++) {
//            Pair<Pair<Point, Point>, Double> pair = longestVertical.get(i);
//            Pair<Point, Point> vertical = pair.getFirst();
//            Imgproc.line(src, vertical.getFirst(), vertical.getSecond(), new Scalar(0, 0, 255), 2);
//            Pair<Pair<Point, Point>, Double> pair2 = longesHorizontal.get(i);
//            Pair<Point, Point> horizontal = pair2.getFirst();
//            Imgproc.line(src, horizontal.getFirst(), horizontal.getSecond(), new Scalar(225, 0, 225), 2);
        }
        // ---------------- find card contour ---------------------
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Mat hierarchy = new Mat();

        Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        Imgproc.HoughLines(gray, lines, 1, Math.PI / 180, 120, 10, 20);

        mergeLine(lines, src, size);

        contours = contours.stream()
                .sorted((p1, p2) -> Double.compare(Imgproc.contourArea(p2), Imgproc.contourArea(p1)))
                .collect(Collectors.toList());

        return contours;
    }


    private static Corners getCorners(List<MatOfPoint> contours, Size size, Mat src) {
        List<Rect> contourRects = new ArrayList<>();
        List<Point> cardEdges = new ArrayList<>();
        boolean isFound = false;
        for (int i = 0; i < contours.size(); i++) {

            MatOfPoint2f c2f = new MatOfPoint2f(contours.get(i).toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx2 = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx2, 0.02 * peri, true);

            if (Arrays.asList(approx2.toArray()).size() == 4 && i < 4 && !isFound) {
                cardEdges.addAll(sortPoints(Arrays.asList(approx2.toArray()))
                        .stream()
                        .distinct()
                        .collect(Collectors.toList()));
                isFound = true;
            }
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, 0.15 * peri, true);
            MatOfPoint points = new MatOfPoint(approx.toArray());
            Rect rect = Imgproc.boundingRect(points);
            contourRects.add(rect);
            Imgproc.rectangle(src, new Point(rect.x * 4, rect.y * 4), new Point((rect.x + rect.width) * 4, (rect.y + rect.height) * 4), new Scalar(255, 0, 255), 3);
        }

        Rect maxRect = contourRects.stream().max(Comparator.comparing(rect -> rect.width * rect.height)).orElse(new Rect());
        AppLogger.i(cardEdges.toString());
        AppLogger.i(new Point(maxRect.x, maxRect.y).toString());
        AppLogger.i(new Point(maxRect.x + maxRect.width, maxRect.y).toString());
        AppLogger.i(new Point(maxRect.x + maxRect.width, maxRect.y + maxRect.height).toString());
        AppLogger.i(new Point(maxRect.x, maxRect.y + maxRect.height).toString());

        return new Corners(Arrays.asList(new Point(maxRect.x * 4, maxRect.y * 4),
                new Point((maxRect.x + maxRect.width) * 4, maxRect.y * 4),
                new Point((maxRect.x + maxRect.width) * 4, (maxRect.y + maxRect.height) * 4),
                new Point(maxRect.x * 4, (maxRect.y + maxRect.height) * 4)), size);
    }

    private static List<Point> sortPoints(List<Point> points) {
        Point p0 = points.stream().min(Comparator.comparing(point -> point.x + point.y)).orElse(new Point());
        Point p1 = points.stream().max(Comparator.comparing(point -> point.x - point.y)).orElse(new Point());
        Point p2 = points.stream().max(Comparator.comparing(point -> point.x + point.y)).orElse(new Point());
        Point p3 = points.stream().min(Comparator.comparing(point -> point.x - point.y)).orElse(new Point());
        return Arrays.asList(p0, p1, p2, p3);
    }

    private static boolean insideArea(List<Point> rp, Size size) {
        int width = (int) size.width;
        int height = (int) size.height;
        int baseHeightMeasure = height / 8;
        int baseWidthMeasure = width / 8;
        int bottomPos = height / 2 + baseHeightMeasure;
        int topPos = height / 2 - baseHeightMeasure;
        int leftPos = width / 2 - baseWidthMeasure;
        int rightPos = width / 2 + baseWidthMeasure;

        return rp.get(0).x <= leftPos && rp.get(0).y <= topPos
                && rp.get(1).x >= rightPos && rp.get(1).y <= topPos
                && rp.get(2).x >= rightPos && rp.get(2).y >= bottomPos
                && rp.get(3).x <= leftPos && rp.get(3).y >= bottomPos;
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

    private static double median(Mat channel) {
        double m = (double) (channel.cols() * channel.rows()) / 2;
        AppLogger.i(String.valueOf(m));
        int bin = 0;
        double med = -1.0;
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt histSize = new MatOfInt(256);
        AppLogger.i(histSize.cols() + " " + histSize.rows() + " " + histSize.channels());
        Mat hist = new Mat();
        Imgproc.calcHist(Collections.singletonList(channel), new MatOfInt(0), new Mat(), hist, histSize, ranges, false);
        AppLogger.i(hist.cols() + " " + hist.rows());
        for (int i = 0; i < hist.rows() && med < 0.0; i++) {
            for (int j = 0; j < hist.cols(); j++) {
                bin += Math.round(hist.get(i, j)[0]);
                if (bin > m && med < 0.0) {
                    med = i;
                }
            }
        }
        AppLogger.i("med:" + med);
        return med;
    }

    private static double distance2Points(Point a, Point b) {
        return Math.sqrt(Math.pow((a.x - b.y), 2.0) + Math.pow((b.x - b.y), 2.0));
    }

    private static void mergeLine(Mat lines, Mat src, Size size1) {
        int rhoThreshold = 30;
        double thetaThreshold = 0.1;
        AppLogger.i(lines.cols() + " " + lines.rows());
        Map<Integer, List<Integer>> similarLine = new HashMap<>();
        List<Boolean> lineFlags = new ArrayList<>();
        for (int i = 0; i < lines.rows(); i++) {
            similarLine.put(i, new ArrayList<>());
            lineFlags.add(true);
            for (int j = 0; j < lines.rows(); j++) {
                if (i == j) continue;
                double rhoI = lines.get(i, 0)[0];
                double thetaI = lines.get(i, 0)[1];
                double rhoJ = lines.get(j, 0)[0];
                double thetaJ = lines.get(j, 0)[1];
                if (Math.abs(rhoI - rhoJ) < rhoThreshold && Math.abs(thetaI - thetaJ) < thetaThreshold) {
                    similarLine.get(i).add(j);
                }
            }
        }
        similarLine.forEach((key, value) -> {
            AppLogger.i(key + " " + value.toString());
        });

        List<List<Integer>> size = new ArrayList<>(similarLine.values());
        List<Integer> indices = size.stream()
                .sorted(Comparator.comparing(List::size))
                .map(list -> getKey(similarLine, list))
                .collect(Collectors.toList());


        for (int i = 0; i < lines.rows() - 1; i++) {
            if (!lineFlags.get(indices.get(i))) continue;
            for (int j = i + 1; j < lines.rows(); j++) {
                if (!lineFlags.get(indices.get(j))) continue;
                double rhoI = lines.get(i, 0)[0];
                double thetaI = lines.get(i, 0)[1];
                double rhoJ = lines.get(j, 0)[0];
                double thetaJ = lines.get(j, 0)[1];
                if (Math.abs(rhoI - rhoJ) < rhoThreshold && Math.abs(thetaI - thetaJ) < thetaThreshold) {
                    lineFlags.set(indices.get(j), false);
                }
            }
        }

        AppLogger.i(String.valueOf(indices.size()));
        AppLogger.i(String.valueOf(lineFlags.size()));
        AppLogger.i("Number of hough lines: " + lines.rows());
        AppLogger.i(lineFlags.toString());
        List<Pair<Pair<Point, Point>, Double>> longest = new ArrayList<>();
        for (int i = 0; i < lines.rows(); i++) {
            if (lineFlags.get(i)) {
                AppLogger.i("true");
                double data[] = lines.get(i, 0);
                double rho1 = data[0];
                double theta1 = data[1];
                AppLogger.i(Arrays.toString(data));
                double cosTheta = Math.cos(theta1);
                double sinTheta = Math.sin(theta1);
                double x0 = cosTheta * rho1;
                double y0 = sinTheta * rho1;
                Point pt1 = new Point((x0 + size1.width * (-sinTheta)) * 4, (y0 + size1.height * cosTheta) * 4);
                Point pt2 = new Point((x0 - size1.width * (-sinTheta)) * 4, (y0 - size1.height * cosTheta) * 4);
                Imgproc.line(src, pt1, pt2, new Scalar(0, 0, 255), 2);
                longest.add(new Pair<>(new Pair<>(pt1, pt2), distance2Points(pt1, pt2)));
            }
        }
        longest = longest.stream().sorted((p1, p2) -> Double.compare(p1.getSecond(), p2.getSecond())).collect(Collectors.toList());
//        for (int i = 0; i < 8; i++) {
//            Pair<Pair<Point, Point>, Double> pair = longest.get(i);
//            Pair<Point, Point> line = pair.getFirst();
//            Imgproc.line(src, line.getFirst(), line.getSecond(), new Scalar(0, 0, 255), 2);
//        }
    }

    public static <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
