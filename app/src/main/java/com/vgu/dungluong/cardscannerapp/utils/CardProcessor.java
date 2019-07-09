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
import androidx.databinding.ObservableBoolean;
import io.reactivex.Observable;
import kotlin.jvm.internal.Intrinsics;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGRA2GRAY;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;
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
        Size size = new Size(src.size().width, src.size().height);
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
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY, 4);
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);


        Imgproc.threshold(gray, gray, 0, 255,
                (isBlackScan ? Imgproc.THRESH_BINARY_INV : THRESH_BINARY) + Imgproc.THRESH_OTSU);


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

    public static Observable<Boolean> textSkewCorrection(Mat img, boolean isBlackScan){
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(img, img, new Size(3, 3), 0);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(30,30));
        Mat closed = new Mat();
        Imgproc.morphologyEx(img, closed, Imgproc.MORPH_CLOSE, kernel);

        img.convertTo(img, CvType.CV_32F); // divide requires floating-point
        Core.divide(img, closed, img, 1, CvType.CV_32F);
        Core.normalize(img, img, 0, 255, Core.NORM_MINMAX);
        img.convertTo(img, CvType.CV_8UC1); // convert back to unsigned int
        Imgproc.threshold(img, img, 0, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);

        Mat kernel1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
        Mat kernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2));
        Imgproc.morphologyEx(img, img, MORPH_CLOSE, kernel1);
        Imgproc.morphologyEx(img, img, MORPH_OPEN, kernel2);
        //Imgproc.erode(img, img, kernel2, new Point(-1, -1), 1);

        Mat lines = new Mat();
        Imgproc.HoughLinesP(img, lines, 1, Math.PI / 160, 100, 10, 20);
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

        if(!Double.isNaN(angle)){
            Mat white = new Mat(img.size(), CvType.CV_8UC1);
            Core.findNonZero(img, white);
            MatOfPoint points = new MatOfPoint(white);
            MatOfPoint2f points2f = new MatOfPoint2f(points.toArray());
            RotatedRect box = Imgproc.minAreaRect(points2f);
            AppLogger.i(String.valueOf(box.angle));
            if(box.angle != 0.0 && box.angle != -90.0 && box.angle == 90.0){
                return Observable.just(deSkew(img, angle, box));
            }
        }
        return Observable.just(false);
    }

    private static boolean deSkew(Mat img, double angle, RotatedRect box){
        Mat rotMat = Imgproc.getRotationMatrix2D(box.center, angle, 1);
        Imgproc.warpAffine(img, img, rotMat, img.size(), INTER_CUBIC);
        return true;
    }
}
