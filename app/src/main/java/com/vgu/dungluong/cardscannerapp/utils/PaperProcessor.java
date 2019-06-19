package com.vgu.dungluong.cardscannerapp.utils;

import android.graphics.Bitmap;

import com.vgu.dungluong.cardscannerapp.model.local.Corners;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import kotlin.jvm.internal.Intrinsics;

/**
 * Created by Dung Luong on 19/06/2019
 */
public class PaperProcessor {

    public static final String TAG = "PaperProcessor";

    private PaperProcessor(){

    }

    @Nullable
    public static Corners processPicture(Mat previewFrame){
        return getCorners(findContours(previewFrame), previewFrame.size());
    }

    public static Mat cropPicture(Mat picture, List<Point> pts){

        pts.forEach(point -> AppLogger.i(TAG + " point: " + point.toString()));
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

        Mat croppedPic = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x, tl.y, tr.x, tr.y, br.x, br.y, bl.x, bl.y);
        dst_mat.put(0, 0, 0.0D, 0.0D, dw, 0.0D, dw, dh, 0.0D, dh);

        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);
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

    private static List<MatOfPoint> findContours(Mat src){
        Mat grayImage;
        Mat cannedImage;
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9.0, 9.0));
        Mat dilate;
        Size size = new Size(src.size().width, src.size().height);
        grayImage = new Mat(size, CvType.CV_8UC4);
        cannedImage = new Mat(size, CvType.CV_8UC1);
        dilate = new Mat(size, CvType.CV_8UC1);

        Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5.0, 5.0), 0.0);
        Imgproc.threshold(grayImage, grayImage, 20.0, 255.0, Imgproc.THRESH_TRIANGLE);
        Imgproc.Canny(grayImage, cannedImage, 75.0, 200.0);
        Imgproc.dilate(cannedImage, dilate, kernel);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dilate, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        contours =  contours.stream()
                .sorted((p1, p2) -> Double.compare(Imgproc.contourArea(p2), Imgproc.contourArea(p1)))
                .collect(Collectors.toList());
        hierarchy.release();
        grayImage.release();
        cannedImage.release();
        kernel.release();
        dilate.release();

        return contours;
    }

    @Nullable
    private static Corners getCorners(List<MatOfPoint> contours, Size size){
        int indexTo = 0;
        for (int i = 0; i < contours.size(); i++){
            if(i > 5) indexTo = 4;
            else indexTo = contours.size() - 1;
        }
        for (int i = 0; i < contours.size(); i++){
            if(i > indexTo){
                return null;
            }else {
                MatOfPoint2f c2f = new MatOfPoint2f(contours.get(i));
                double peri = Imgproc.arcLength(c2f, true);
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);
                List<Point> points = Arrays.asList(approx.toArray());
                if(points.size() == 4){
                    List<Point> foundPoints = sortPoints(points);
                    return new Corners(foundPoints, size);
                }
            }
        }
        return null;
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
}
