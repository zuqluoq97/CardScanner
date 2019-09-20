package com.vgu.dungluong.cardscannerapp.utils;

import android.graphics.Bitmap;
import android.util.Pair;

import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;

import org.jetbrains.annotations.NotNull;
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
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import io.reactivex.Observable;
import kotlin.jvm.internal.Intrinsics;

import static org.opencv.core.Core.BORDER_CONSTANT;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.core.CvType.CV_8UC4;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2Lab;
import static org.opencv.imgproc.Imgproc.COLOR_BGRA2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_Lab2BGR;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.INTER_AREA;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
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

        Mat croppedPic = new Mat(maxHeight, maxWidth, CV_8UC1);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x, tl.y, tr.x, tr.y, br.x, br.y, bl.x, bl.y);
        dst_mat.put(0, 0, 0.0D, 0.0D, dw, 0.0D, dw, dh, 0.0D, dh);
        AppLogger.i(tl.x+" "+ tl.y+" "+ tr.x+" "+ tr.y+" "+ br.x+" "+ br.y+" "+ bl.x+" "+ bl.y);
        AppLogger.i(0.0D+" "+ 0.0D+" "+ dw+" "+ 0.0D+" "+dw+" "+ dh+" "+ 0.0D+" "+ dh);
        // Calculate a perspective transform from four pairs of the corresponding points
        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);
        AppLogger.i(m.rows() + " " + m.cols());
        for(int i = 0; i < m.rows(); i++){
            for(int j = 0; j < m.cols(); j++){
                AppLogger.i(Arrays.toString(m.get(i, j)));
            }
        }
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
//        double coeff = 0.1;
        double scale = src.size().width/350;
        Size croppedSize = new Size(350, src.size().height / scale);
        AppLogger.i(src.width() + " " + src.height());
        // Down sample
        Mat resizeMat = src.clone();
        if(scale > 1) Imgproc.resize(resizeMat, resizeMat, croppedSize, INTER_AREA);
        Mat kernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(7, 7));
        Imgproc.morphologyEx(resizeMat, resizeMat, MORPH_OPEN, kernel2);
//        Imgproc.erode(src, src, kernel, new Point(-1,-1), 5);
        Mat canny = new Mat(croppedSize, CV_8UC1);

        // Do contour detection
        Imgproc.Canny(resizeMat, canny, 30, 90);
        Mat lines = new Mat();
        // Do hough transform
//        Imgproc.HoughLines(canny, lines, 1, Math.PI / 360.0, 70);
//
//        for (int i = 0; i < lines.rows(); i++) {
//            double data[] = lines.get(i, 0);
//            double rho1 = data[0];
//            double theta1 = data[1];
//            AppLogger.i(rho1 + " " + theta1);
//            double cosTheta = Math.cos(theta1);
//            double sinTheta = Math.sin(theta1);
//            double x0 = cosTheta * rho1;
//            double y0 = sinTheta * rho1;
//            Point pt1 = new Point((x0 + 10000 * (-sinTheta)) , (y0 + 10000 * cosTheta));
//            Point pt2 = new Point((x0 - 10000 * (-sinTheta)) , (y0 - 10000 * cosTheta));
//            Imgproc.line(src, pt1, pt2, new Scalar(0, 0, 255), 2);
//        }
//
//        AppLogger.i("Number of lines " + lines.rows());
        Imgproc.HoughLinesP(canny, lines, 1, Math.PI / 360.0, 70, 15, 1);
        AppLogger.i("Number of linesP: " + lines.rows());
        return findEdges(lines, scale < 1 ? src.size() : croppedSize, src, scale < 1 ? 1 : scale);
    }

    private static List<Point> sortPoints(List<Point> points, Mat img) {
        AppLogger.i(String.valueOf(points.size()));
        Size imgSize = img.size();
        //Point center = new Point(imgSize.width / 2, imgSize.height / 2);

        List<Point> maxTl = new ArrayList<>();
        List<Point> maxTr = new ArrayList<>();
        List<Point> maxBr = new ArrayList<>();
        List<Point> maxBl = new ArrayList<>();

        AtomicLong x = new AtomicLong(0);
        AtomicLong y = new AtomicLong(0);


        points.forEach(p -> {
//            if(p.x < center.x && p.y < center.y) maxTl.add(p);
//            if(p.x > center.x && p.y < center.y) maxTr.add(p);
//            if(p.x > center.x && p.y > center.y) maxBr.add(p);
//            if(p.x < center.x && p.y > center.y) maxBl.add(p);
            x.addAndGet((long) p.x);
            y.addAndGet((long) p.y);
        });

        Point center = new Point((double)x.get()/points.size(), (double)y.get()/points.size());
        //Imgproc.circle(img, center, 10, new Scalar(0, 0, 255), 3);

//        points.forEach(p -> {
//            if(p.x < center.x && p.y < center.y) maxTl.add(p);
//            if(p.x > center.x && p.y < center.y) maxTr.add(p);
//            if(p.x > center.x && p.y > center.y) maxBr.add(p);
//            if(p.x < center.x && p.y > center.y) maxBl.add(p);
//        });
        maxTl = points.stream().sorted(Comparator.comparing( point -> point.x + point.y)).collect(Collectors.toList());
        maxTr = points.stream().sorted((p1,p2) -> Double.compare(p2.x - p2.y, p1.x - p1.y)).collect(Collectors.toList());
        maxBr = points.stream().sorted((p1,p2) -> Double.compare(p2.x + p2.y, p1.x + p1.y)).collect(Collectors.toList());
        maxBl = points.stream().sorted(Comparator.comparing(  point -> point.x - point.y)).collect(Collectors.toList());

//
//        maxTl = points.stream()
//                .sorted(Comparator.comparing(point -> center.x - point.x))
//                .filter(point -> center.x - point.x > 0)
//                .sorted(Comparator.comparing(point -> center.y - point.y))
//                .filter(point -> point.y - center.y < 0)
//                .collect(Collectors.toList());
//
//        maxTr = points.stream()
//                .sorted(Comparator.comparing(point -> point.x - center.x))
//                .filter(point -> point.x - center.x > 0)
//                .sorted(Comparator.comparing(point -> center.y - point.y))
//                .filter(point -> point.y - center.y < 0)
//                .collect(Collectors.toList());
//
//        maxBr = points.stream()
//                .sorted(Comparator.comparing(point -> point.x - center.x))
//                .filter(point -> point.x - center.x > 0)
//                .sorted(Comparator.comparing(point -> point.y - center.y))
//                .filter(point -> point.y - center.y > 0)
//                .collect(Collectors.toList());
//
//        maxBl = points.stream()
//                .sorted(Comparator.comparing(point -> center.x - point.x))
//                .filter(point -> center.x - point.x > 0)
//                .sorted(Comparator.comparing(point -> point.y - center.y))
//                .filter(point -> point.y - center.y > 0)
//                .collect(Collectors.toList());

//        for(int i =0; i < maxTl.size()/2; i++){
//            Imgproc.circle(img, maxTl.get(i), 5, new Scalar(0, 0, 255), 2);
//        }
//
//        for(int i =0; i <maxTr.size()/2; i++){
//            Imgproc.circle(img, maxTr.get(i), 5, new Scalar(0, 255, 0), 2);
//        }
//
//        for(int i =0; i <maxBr.size()/2; i++){
//            Imgproc.circle(img, maxBr.get(i), 5, new Scalar(255, 0, 0), 2);
//        }
//
//        for(int i =0; i <maxBl.size()/2; i++){
//            Imgproc.circle(img, maxBl.get(i), 5, new Scalar(30, 30, 30), 2);
//        }

//        maxTr = points.stream().sorted(Comparator.comparing(point -> point.x - point.y)).collect(Collectors.toList());
//        maxBr = points.stream().sorted(Comparator.comparing(point -> point.x + point.y)).collect(Collectors.toList());
//        maxBl = points.stream().sorted((p1,p2) -> Double.compare(p2.x - p2.y, p1.x - p1.y)).collect(Collectors.toList());

        int step = 0;
        for(int i = 0; i < maxTl.size(); i++) {
            AppLogger.i("tl: "+ String.valueOf(i));
            step+=1;
            for (int j = 0; j < maxTr.size(); j++) {
                step+=1;
                for (int k = 0; k < maxBr.size(); k++) {
                    step+=1;
                    if (!isRightAngle(angle(maxTr.get(j), maxTl.get(i), maxBr.get(k))))
                        continue ;
                    for (int l = 0; l < maxBl.size(); l++) {
                        step+=1;
                        if (!isRightAngle(angle(maxBr.get(k), maxTr.get(j), maxBl.get(l))))
                            continue;
                        if (!isRightAngle(angle(maxBl.get(l), maxBr.get(k), maxTl.get(i))))
                            continue;
                        if (!isRightAngle(angle(maxTl.get(i), maxBl.get(l), maxTr.get(j))))
                            continue;
                        double height = distance2Points(maxTr.get(j), maxBr.get(k));
                        double width = distance2Points(maxTl.get(i), maxTr.get(j));
                        double ratio;
                        if(height > width) ratio = height / width;
                        else ratio = width / height;
                        if (ratio > 1.5 && ratio < 1.8) {
                            AppLogger.i("found");
                            AppLogger.i(height + " " + width + " " + (img.size().area() / 4));
                            if(height * width > img.size().area() / 4) {
                                AppLogger.i(String.valueOf(ratio));
//                                Imgproc.circle(img, maxTl.get(i), 15, new Scalar(0, 0, 255), 5);
//                                Imgproc.circle(img, maxTr.get(j), 15, new Scalar(0, 0, 255), 5);
//                                Imgproc.circle(img, maxBr.get(k), 15, new Scalar(0, 0, 255), 5);
//                                Imgproc.circle(img, maxBl.get(l), 15, new Scalar(0, 0, 255), 5);
                                AppLogger.i("complexity: " + points.size() + " " + step);
                                return Arrays.asList(maxTl.get(i),
                                        maxTr.get(j),
                                        maxBr.get(k),
                                        maxBl.get(l));
                            }
                        }
                    }
                }
            }
        }
//        AppLogger.i("complexity: " + points.size() + " " + step);
//        for(int i = 0; i < points.size(); i++) {
//            AppLogger.i("tl: " + String.valueOf(i));
//            if(i >= maxTl.size() || i >= maxTr.size() || i >= maxBr.size() || i >= maxBl.size()) break;
//            Point tl = maxTl.get(i);
//            Point tr = maxTr.get(i);
//            Point br = maxBr.get(i);
//            Point bl = maxBl.get(i);
//            if (!isRightAngle(angle(tr, tl, br)))
//                continue;
//            if (!isRightAngle(angle(br, tr, bl)))
//                continue;
//            if (!isRightAngle(angle(bl, br, tl)))
//                continue;
//            if (!isRightAngle(angle(tl, bl, tr)))
//                continue;
//            double height = distance2Points(tr, br);
//            double width = distance2Points(tl, tr);
//            double ratio;
//            if (height > width) ratio = height / width;
//            else ratio = width / height;
//            if (ratio > 1.5 && ratio < 1.8) {
//                AppLogger.i("found");
//                AppLogger.i(height + " " + width + " " + (img.size().area() / 4));
//                if (height * width > img.size().area() / 4) {
//                    AppLogger.i(String.valueOf(ratio));
////                                Imgproc.circle(img, maxTl.get(i), 15, new Scalar(0, 0, 255), 5);
////                                Imgproc.circle(img, maxTr.get(j), 15, new Scalar(0, 0, 255), 5);
////                                Imgproc.circle(img, maxBr.get(k), 15, new Scalar(0, 0, 255), 5);
////                                Imgproc.circle(img, maxBl.get(l), 15, new Scalar(0, 0, 255), 5);
//                    return Arrays.asList(tl,
//                            tr,
//                            br,
//                            bl);
//                }
//            }
//
//        }
        return Arrays.asList(new Point(imgSize.width / 10, imgSize.height / 10),
                new Point(imgSize.width * 9/10, imgSize.height / 10),
                new Point(imgSize.width * 9/10, imgSize.height * 9/10),
                new Point(imgSize.width / 10, imgSize.height * 9/10));
    }

    public static Observable<List<Bitmap>> cropTextArea(Mat img, List<Corners> textRects) {

        Mat img2 = img.clone();
        List<Bitmap> bms = new ArrayList<>();
        for(int i = 0; i < textRects.size(); i++){
            AppLogger.i(String.valueOf(i));
            Corners textBoxCorners = textRects.get(i);
            Mat clone = img2.clone();
            Mat crop = cropPicture(clone, textBoxCorners.getCorners());
            cvtColor(crop, crop, Imgproc.COLOR_BGRA2BGR);
            performGammaCorrection(0.6, crop);
            crop = brightnessAndConstraintAuto(crop, 1);
            CardExtract ce = new CardExtract(crop);
            crop = ce.run();
            textSkewCorrection(crop);
//            Imgproc.cvtColor(crop, crop, Imgproc.COLOR_BGR2GRAY);
            Bitmap bitmap = Bitmap.createBitmap(crop.width(), crop.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(crop, bitmap, true);
            bms.add(bitmap);
        }

        return Observable.just(bms);
    }

    public static Mat updateCropRectsOnImage(Mat src, List<Corners> textRects){
        Mat img = src.clone();
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2RGB);
        for(int i=0; i < textRects.size(); i++){
            Corners textBoxCorners = textRects.get(i);
            Imgproc.line(img, textBoxCorners.getCorners().get(0), textBoxCorners.getCorners().get(1), new Scalar(0, 0, 255), 3);
            Imgproc.line(img, textBoxCorners.getCorners().get(1), textBoxCorners.getCorners().get(2), new Scalar(0, 0, 255), 3);
            Imgproc.line(img, textBoxCorners.getCorners().get(2), textBoxCorners.getCorners().get(3), new Scalar(0, 0, 255), 3);
            Imgproc.line(img, textBoxCorners.getCorners().get(3), textBoxCorners.getCorners().get(0), new Scalar(0, 0, 255), 3);
        }
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2BGR);
        return img;
    }

    public static Observable<Boolean> textSkewCorrection(Mat image){
        Mat img = image.clone();
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(img, img, new Size(3, 3), 0);
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(30,30));
//        Mat closed = new Mat();
//        Imgproc.morphologyEx(img, closed, MORPH_CLOSE, kernel);

//        img.convertTo(img, CvType.CV_32F); // divide requires floating-point
//        Core.divide(img, closed, img, 1, CvType.CV_32F);
//        Core.normalize(img, img, 0, 255, Core.NORM_MINMAX);
//        img.convertTo(img, CvType.CV_8UC1); // convert back to unsigned int
        Imgproc.threshold(img, img, 0, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);

//        Mat kernel1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,1));
//        //Mat kernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2));
//        //Imgproc.morphologyEx(img, img, MORPH_CLOSE, kernel1);
//        //Imgproc.morphologyEx(img, image, MORPH_OPEN, kernel2);
//        Imgproc.erode(img, img, kernel1);

        Mat lines = new Mat();
        Imgproc.HoughLinesP(img, lines, 1, Math.PI / 360, 30);
        double angle = 0.;
        AppLogger.i(lines.height() + " " + lines.width() + " " + lines.rows() + " " + lines.cols());
//        for (int i = 0; i < lines.rows(); i++) {
//            double[] val = lines.get(i, 0);
//            Imgproc.line(image, new Point(val[0], val[1]), new Point(val[2] , val[3]), new Scalar(140, 180, 33), 1);
//        }
        for(int i = 0; i<lines.height(); i++){
            for(int j = 0; j<lines.width();j++){
                angle += Math.atan2(lines.get(i, j)[3] - lines.get(i, j)[1], lines.get(i, j)[2] - lines.get(i, j)[0]);
            }
        }

        angle /= lines.size().area();
        angle = angle * 180 / Math.PI;
        AppLogger.i("angle: " + String.valueOf(angle));

        if(!Double.isNaN(angle)){
            Mat white = new Mat(img.size(), CvType.CV_8UC1);
            Core.findNonZero(img, white);
            MatOfPoint points = new MatOfPoint(white);
            MatOfPoint2f points2f = new MatOfPoint2f(points.toArray());
            RotatedRect box = Imgproc.minAreaRect(points2f);
            AppLogger.i("angle2: " + String.valueOf(box.angle));
            if(box.angle != 0.0 && box.angle != -0.0 && box.angle != -90.0 && box.angle != 90.0){
//                Point[] ps = new Point[4];
//                box.points(ps);
//                for(int i = 0; i < 4; i++){
//                    Imgproc.line(image, ps[i], ps[(i+1)%4], new Scalar(0,255,0),2);
//                }

                return Observable.just(deSkew(image, angle, box));
            }
        }
        return Observable.just(false);
    }

    private static boolean deSkew(Mat img, double angle, RotatedRect box) {
        Mat rotMat = Imgproc.getRotationMatrix2D(box.center, angle, 1);
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2RGB);
        Imgproc.warpAffine(img, img, rotMat, img.size(), INTER_CUBIC, Core.BORDER_CONSTANT, new Scalar(255, 255, 255));
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2BGR);
        return true;
    }

    // Find 4 edges based on the line of hough transform
    private static Corners findEdges(Mat lines, Size croppedSize, Mat img, double cropScale) {
        List<Point> points = new ArrayList<>();
        List<Point> intersections = new ArrayList<>();

        for (int i = 0; i < lines.rows(); i++) {
            double[] val = lines.get(i, 0);
            points.add(new Point(val[0], val[1]));
            points.add(new Point(val[2], val[3]));
            //Imgproc.line(img, new Point(val[0] * cropScale, val[1] * cropScale), new Point(val[2] * cropScale, val[3] * cropScale), new Scalar(0, 0, 255), 1);
            // find y = m x + b
            double slope;
            double b;
            double changeInX = val[0] - val[2];
            double changeInY = val[1] - val[3];
            // Extend the line
            if(changeInX != 0) {
                slope = changeInY / changeInX;
                b = val[1] - slope * val[0];
                double x1 = -croppedSize.width;
                double x2 = croppedSize.width;
                double y1 = slope * x1 + b;
                double y2 = slope * x2 + b;

                //Imgproc.line(img, new Point(x1 * cropScale, y1 * cropScale), new Point(x2 * cropScale, y2 * cropScale), new Scalar(0, 255, 0), 1);
            }else{
                double y1 = -croppedSize.height;
                double y2 = croppedSize.height;

                //Imgproc.line(img, new Point(val[0] * cropScale, y1 * cropScale), new Point(val[2] * cropScale, y2 * cropScale), new Scalar(0, 255, 0), 1);
            }
        }

        // Get all the intersection points
        for(int i = 0; i < points.size(); i += 2){
            for(int j = i+2; j < points.size()-2; j += 2){
                Point intersection = intersection(points.get(i), points.get(i+1), points.get(j), points.get(j+1), croppedSize, cropScale);
                if(intersection != null){
                    intersections.add(intersection);
                }
            }
        }

        AppLogger.i("number of intersections:" + String.valueOf(intersections.size()));
        List<Point> uniquePoints = new ArrayList<>(intersections);

        for(int i = 0; i < intersections.size()-1; i++){
            for(int j = i+1; j<intersections.size();j++){
                if (Math.abs(intersections.get(i).x - intersections.get(j).x) < 5
                        && Math.abs(intersections.get(i).y - intersections.get(j).y) < 5) {
                    uniquePoints.remove(intersections.get(i));
                    break;
                }
            }
        }

        uniquePoints.forEach(p ->{
            uniquePoints.set(uniquePoints.indexOf(p), new Point(p.x*cropScale, p.y*cropScale));
            //Imgproc.circle(img, new Point(p.x*cropScale, p.y*cropScale), 10, new Scalar(0, 0, 255), 3);
        });
        AppLogger.i("number of intersections after reduplicate" + String.valueOf(uniquePoints.size()));
        // Select 4 edges
        return new Corners(sortPoints(uniquePoints, img), new Size(croppedSize.width * cropScale, croppedSize.height * cropScale));
    }

    private static Point intersection(Point a1, Point b1, Point a2, Point b2, Size size, double cropScale) {
        double A1 = b1.y - a1.y;
        double B1 = a1.x - b1.x;
        double C1 = (a1.x * A1) + (a1.y * B1);

        double A2 = b2.y - a2.y;
        double B2 = a2.x - b2.x;
        double C2 = (a2.x * A2) + (a2.y * B2);

        double det = (A1 * B2) - (A2 * B1);
        // Parallel line
        if (Math.abs(det) < AppConstants.EPISILON)
            return null;

        Point intersect = new Point(((C1 * B2) - (C2 * B1)) / det,
                ((C2 * A1) - (C1 * A2)) / det);
        // Intersect over the area
        if (intersect.x > 0 && intersect.y > 0 && intersect.x <= size.width && intersect.y <= size.height) {
            double angle = angle(intersect, a1, a2);
            if(angle < 0) angle += 180;
            AppLogger.i(String.valueOf(angle));
            // Choose intersection that has value near 90 degree
            if (isRightAngle(angle)) return new Point((Math.floor(intersect.x * 1000)) * 1 / 1000, (Math.floor(intersect.y * 1000)) * 1 / 1000);
            else return null;
        } else {
            return null;
        }
    }

    private static double distance2Points(Point a, Point b) {
        return Math.sqrt(Math.pow((a.x - b.x), 2.0) + Math.pow((a.y - b.y), 2.0));
    }

    private static boolean isRightAngle(double angle){
        return (angle > 86 && angle < 94);
    }

    // Find angle of two lines that are intersected
    private static double angle(Point p1, Point p2, Point p3){
        double dx21 = p2.x - p1.x;
        double dx31 = p3.x - p1.x;
        double dy21 = p2.y - p1.y;
        double dy31 = p3.y - p1.y;
        double dx32 = p3.x - p2.x;
        double dy32 = p3.y - p2.y;

        double m12 = Math.sqrt(dx21 * dx21 + dy21 * dy21);
        double m13 = Math.sqrt(dx31 * dx31 + dy31 * dy31);
        double m23 = Math.sqrt(dx32 * dx32 + dy32 * dy32);
        return (Math.acos((m12 * m12 + m13 * m13 - m23 * m23) / (2 * m12 * m13)) * 180.0) / Math.PI;
    }


    public static Mat brightnessAndConstraintAuto(Mat img, float clipHistPercent){
        Mat dst = new Mat();
        float alpha = 0f;
        float beta = 0f;
        int minGray = 0;
        int maxGray = 0;

        // To calculate grayscale histogram
        Mat gray = new Mat();

        if(img.type() == CV_8UC1) gray = img.clone();
        else if(img.type() == CV_8UC3) Imgproc.cvtColor(img, gray, COLOR_BGR2GRAY);
        else if(img.type() == CV_8UC4) {
            Imgproc.cvtColor(img, gray, COLOR_BGRA2GRAY);
        }

        if(clipHistPercent == 0){
            // Keep full available range
            Core.minMaxLoc(gray);
        }else{
            Mat hist = new Mat(); // The grayscale histogram
            MatOfFloat range = new MatOfFloat(0f, 256f);
            MatOfInt histSize = new MatOfInt(256);
            boolean accumulate = true;
            Imgproc.calcHist(Arrays.asList(gray), new MatOfInt(0),
                    new Mat(), hist, histSize, range, accumulate);

            // calculate cumulative distribution from the histogram
            List<Double> accumulator = new ArrayList<>();
            AppLogger.i(accumulator.toString());

            for(int i = 0; i < 256; i ++){
                if(i == 0) accumulator.add(hist.get(i, 0)[0]);
                else accumulator.add(accumulator.get(i - 1) + hist.get(i, 0)[0]);
            }

            // locate points that cuts at required value
            double max = accumulator.get(255);
            clipHistPercent *= (max / 100.0); //make percent as absolute
            clipHistPercent /= 2.0; // left and right wings
            // locate left cut
            minGray = 0;
            while (accumulator.get(minGray) < clipHistPercent)
                minGray++;

            // locate right cut
            maxGray = 255;
            while (accumulator.get(maxGray) >= (max - clipHistPercent))
                maxGray--;

            // current range
            float inputRange = maxGray - minGray;

            alpha = 255 / inputRange;   // alpha expands current range to histsize range
            beta = -minGray * alpha;             // beta shifts current range so that minGray will go to 0

            // Apply brightness and contrast normalization
            // convertTo operates with saurate_cast
            img.convertTo(dst, -1, alpha, beta);

            // restore alpha channel from source
            if (dst.type() == CV_8UC4) {
                AppLogger.i("8uc4");
                MatOfInt fromTo = new MatOfInt(3, 3);
                Core.mixChannels(Collections.singletonList(img), Collections.singletonList(dst), fromTo);
            }
            return dst;
        }

        return gray;
    }


    public static void performGammaCorrection(double gamma, Mat img) {
        //! [changing-contrast-brightness-gamma-correction]
        Mat lookUpTable = new Mat(1, 256, CV_8U);
        byte[] lookUpTableData = new byte[(int) (lookUpTable.total()*lookUpTable.channels())];
        for (int i = 0; i < lookUpTable.cols(); i++) {
            lookUpTableData[i] = saturate(Math.pow(i / 255.0, gamma) * 255.0);
        }
        lookUpTable.put(0, 0, lookUpTableData);

        Core.LUT(img, lookUpTable, img);
    }

    private static byte saturate(double val) {
        int iVal = (int) Math.round(val);
        iVal = iVal > 255 ? 255 : (iVal < 0 ? 0 : iVal);
        return (byte) iVal;
    }

    public static Mat improveContrast(Mat img){
        Mat lab = new Mat();
        Imgproc.cvtColor(img, lab, COLOR_BGR2Lab);
        List<Mat> channels = new ArrayList<>();
        Core.split(lab, channels);
        CLAHE clahe = Imgproc.createCLAHE(2);
        Mat cl = new Mat();
        clahe.apply(channels.get(0), cl);
        Mat limg = new Mat();
        Core.merge(Arrays.asList(cl, channels.get(1), channels.get(2)), limg);
        Imgproc.cvtColor(limg, limg, COLOR_Lab2BGR);
        return limg;
    }

}
