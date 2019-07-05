package com.vgu.dungluong.cardscannerapp.utils;

import android.graphics.Bitmap;

import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import kotlin.jvm.internal.Intrinsics;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGRA2GRAY;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * Created by Dung Luong on 19/06/2019
 */
public class CardProcessor {

    public static final String TAG = "CardProcessor";

    private CardProcessor(){

    }

    @Nullable
    public static Corners processPicture(Mat previewFrame, boolean isBlackScan){
        return getCorners(findContours(previewFrame, isBlackScan), previewFrame.size(), previewFrame);
    }

    public static Mat cropPicture(Mat picture, List<Point> pts){
        pts = sortPoints(pts);
        Point tl = pts.get(0);
        Point tr = pts.get(1);
        Point br = pts.get(2);
        Point bl = pts.get(3);

        double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2.0D) + Math.pow(br.y - bl.y, 2.0D));
        double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2.0D) + Math.pow(tr.y - tl.y, 2.0D));

        double dw = Math.max(widthA, widthB);
        int maxWidth = (int)dw;

        double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2.0D) + Math.pow(tr.y - br.y, 2.0D));
        double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2.0D) + Math.pow(tl.y - bl.y, 2.0D));

        double dh = Math.max(heightA, heightB);
        int maxHeight = (int)dh;

        Mat croppedPic = new Mat(maxHeight, maxWidth, CvType.CV_8U);

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
    public static final Bitmap enhancePicture(@Nullable Bitmap src) {
        Mat src_mat = new Mat();
        Utils.bitmapToMat(src, src_mat);
        Imgproc.cvtColor(src_mat, src_mat, 11);
        Imgproc.adaptiveThreshold(src_mat, src_mat, 255.0D, 0, 0, 15, 15.0D);
        Bitmap result = Bitmap.createBitmap(src != null ? src.getWidth() : 1080, src != null ? src.getHeight() : 1920, Bitmap.Config.RGB_565);
        Utils.matToBitmap(src_mat, result, true);
        src_mat.release();
        Intrinsics.checkExpressionValueIsNotNull(result, "result");
        return result;
    }

    private static List<MatOfPoint> findContours(Mat src, boolean isBlackScan) {
        Size size = new Size(src.size().width, src.size().height);
        Mat gray = new Mat(size, CvType.CV_8U);

        // Edge detection with canny
//        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
//        Imgproc.Canny(gray, gray, 30, 90, 3, true);
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9,9));
//
//        Imgproc.dilate(gray, gray, kernel, new Point(-1, -1), 10);
//        Imgproc.erode(gray, gray, kernel, new Point(-1, -1), 10);

        // Edge detection with threshold
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY, 4);
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
        Imgproc.threshold(gray, gray, 0, 255,
                (isBlackScan ? Imgproc.THRESH_BINARY_INV  : THRESH_BINARY) + Imgproc.THRESH_OTSU);


        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9,9));
        Imgproc.erode(gray, gray, kernel, new Point(-1, -1), 1);
        // ---------------- find card contour ---------------------
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Mat hierarchy = new Mat();

        Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        contours = contours.stream()
                .sorted((p1, p2) -> Double.compare(Imgproc.contourArea(p2), Imgproc.contourArea(p1)))
                .collect(Collectors.toList());

        return contours;
    }

    private static Corners getCorners(List<MatOfPoint> contours, Size size, Mat src){
        List<Rect> contourRects = new ArrayList<>();
        List<Point> cardEdges= new ArrayList<>();
        boolean isFound = false;
        for (int i = 0; i < contours.size(); i++) {

            MatOfPoint2f c2f = new MatOfPoint2f(contours.get(i).toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx2 = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx2, 0.02 * peri, true);

            if(Arrays.asList(approx2.toArray()).size() == 4 && i < 4 && !isFound){
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
        }

        Rect maxRect = contourRects.stream().max(Comparator.comparing(rect -> rect.width * rect.height)).orElse(new Rect());
        //Imgproc.rectangle(src, new Point(maxRect.x,maxRect.y), new Point(maxRect.x+maxRect.width,maxRect.y+maxRect.height), new Scalar(255,0,255), 3);
        AppLogger.i(cardEdges.toString());
        AppLogger.i(new Point(maxRect.x, maxRect.y).toString());
        AppLogger.i(new Point(maxRect.x + maxRect.width, maxRect.y).toString());
        AppLogger.i(new Point(maxRect.x + maxRect.width, maxRect.y + maxRect.height).toString());
        AppLogger.i(new Point(maxRect.x, maxRect.y + maxRect.height).toString());

        return new Corners(cardEdges.size() == 4
                ? cardEdges
                : Arrays.asList(new Point(maxRect.x, maxRect.y),
                new Point(maxRect.x + maxRect.width, maxRect.y),
                new Point(maxRect.x + maxRect.width, maxRect.y + maxRect.height),
                new Point(maxRect.x, maxRect.y + maxRect.height)) , size);
    }

    private static List<Point> sortPoints(List<Point> points){
        Point p0 = points.stream().min(Comparator.comparing(point -> point.x + point.y)).orElse(new Point());
        Point p1 = points.stream().max(Comparator.comparing(point -> point.x - point.y )).orElse(new Point());
        Point p2 = points.stream().max(Comparator.comparing(point -> point.x + point.y)).orElse(new Point());
        Point p3 = points.stream().min(Comparator.comparing(point -> point.x - point.y)).orElse(new Point());
        return Arrays.asList(p0, p1, p2, p3);
    }

    private static boolean insideArea(List<Point> rp, Size size) {
        int width = (int)size.width;
        int height = (int)size.height;
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

    public static void textSkewCorrection(Mat img, boolean isBlackScan){
        Mat gray = new Mat();
        cvtColor(img, gray, Imgproc.COLOR_BGRA2GRAY);
        Core.bitwise_not(gray, gray);
        Imgproc.threshold(gray, gray, 0, 255, (isBlackScan ? Imgproc.THRESH_BINARY_INV  : THRESH_BINARY) + Imgproc.THRESH_OTSU);
        Mat lines = new Mat();
        Imgproc.HoughLinesP(gray, lines, 1, Math.PI / 180, 100, gray.width() / 2.f, 20);
        double angle = 0.;
        AppLogger.i(lines.height() + " " + lines.width() + " " + lines.rows() + " " + lines.cols());
        for(int i = 0; i<lines.height(); i++){
            for(int j = 0; j<lines.width();j++){
                angle += Math.atan2(lines.get(i, j)[3] - lines.get(i, j)[1], lines.get(i, j)[2] - lines.get(i, j)[0]);
            }
        }
        angle /= lines.size().area();
        angle = angle * 180 / Math.PI;
        AppLogger.i(String.valueOf(angle));
    }
}
