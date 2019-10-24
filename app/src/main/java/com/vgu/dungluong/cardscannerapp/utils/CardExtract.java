package com.vgu.dungluong.cardscannerapp.utils;


import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static org.opencv.core.Core.BORDER_CONSTANT;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.INTER_AREA;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;

/**
 * Created by Dung Luong on 31/07/2019
 */
public class CardExtract {
    private Mat img;
    private int img_y, img_x;
    private List<MatOfPoint> contours;

    public CardExtract(Mat src) {
        // This utility class is not publicly instantiable
        img = src.clone();
    }

    public Mat run() {
        double scale = img.size().height/100;
        Size size;
        size = new Size(img.size().width / scale, 100);
        if(scale > 1) Imgproc.resize(img,img,size, INTER_AREA);
        img_y = img.rows();
        img_x = img.cols();

        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, COLOR_BGR2GRAY);
        Imgproc.Canny(gray,gray,70,210);
        contours=new ArrayList<>();
        Mat hierarchy=new Mat();
        Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
       // Mat clone = new Mat(size, CV_8U);
        //printExternalContours(clone, contours, hierarchy, 0);
        List<MatOfPoint> keepers = new ArrayList<>();
        Mat processed = gray.clone();
//        for(int i = 0; i >= 0; i = (int) hierarchy.get(0, i)[0])
//        {
//            MatOfPoint contour=contours.get(i);
//
//            Rect rect=Imgproc.boundingRect(contour);
//            double x=rect.x,y=rect.y,w=rect.width,h=rect.height;
//
//            if (keep(contour) && include_box(i, hierarchy, contour)) {
//                // It's a winner!
//                keepers.add(contour);
//                Imgproc.rectangle(processed, new Point(x, y),new Point(x + w, y + h), new Scalar(100, 100, 100), 3);
//            }
//        }

        for(int i=0;i<contours.size();i++) {
            MatOfPoint contour=contours.get(i);

            Rect rect=Imgproc.boundingRect(contour);
            double x=rect.x,y=rect.y,w=rect.width,h=rect.height;

            if (keep(contour) && include_box(i, hierarchy, contour)) {
                // It's a winner!
                keepers.add(contour);
                Imgproc.rectangle(processed, new Point(x, y),new Point(x + w, y + h), new Scalar(100, 100, 100), 2);
            }else{
                AppLogger.i("not a winner");
            }
        }

        Mat new_image=img.clone();
        new_image.setTo(new Scalar(255,255,255));

        int lightText = 0;
        int darkText = 0;
        for(int i = 0; i< keepers.size(); i++) {
            //# Find the average intensity of the edge pixels to
            //# determine the foreground intensity
            double fg_int = 0;
            for (Point p: keepers.get(i).toArray()){
                fg_int +=ii(p.x, p.y);

            }

            fg_int /= keepers.get(i).rows();

            Rect rect=Imgproc.boundingRect(keepers.get(i));
            double x_=rect.x, y_=rect.y, width=rect.width, height=rect.height;

            List<Double> mBgIntensities = Arrays.asList(
                    //# bottom left corner 3 pixels
                    ii(x_ - 1, y_ - 1),
                    ii(x_ - 1, y_),
                    ii(x_, y_ - 1),

                    //# bottom right corner 3 pixels
                    ii(x_ + width + 1, y_ - 1),
                    ii(x_ + width, y_ - 1),
                    ii(x_ + width + 1, y_),

                    //# top left corner 3 pixels
                    ii(x_ - 1, y_ + height + 1),
                    ii(x_ - 1, y_ + height),
                    ii(x_, y_ + height + 1),

                    //# top right corner 3 pixels
                    ii(x_ + width + 1, y_ + height + 1),
                    ii(x_ + width, y_ + height + 1),
                    ii(x_ + width + 1, y_ + height));

            mBgIntensities = mBgIntensities.stream().filter(intensity -> intensity > -1).collect(Collectors.toList());

            double [] bg_int = mBgIntensities.stream().mapToDouble(d -> d).toArray();

            //# Find the median of the background
            //# pixels determined above
            double bg_intN = findMedian(bg_int);
            //# Determine if the box should be inverted
            int fg,bg;
            AppLogger.i("intensity comparison: " + fg_int + " " + bg_intN);
            if (fg_int > bg_intN) {
                lightText ++;
                fg = 255;
                bg = 0;
            } else {
                darkText ++;
                fg = 0;
                bg = 255;
            }

            for (double x = x_; x < x_ + width; x++) {
                for (double y = y_; y < y_ + height; y++) {
                    if (y >= img_y || x >= img_x) {
                        //System.out.println("pixel out of bounds ("+y+","+x+")");
                        continue;
                    }
                    if (ii(x, y) >= fg_int) {
                        new_image.put((int) y, (int) x, bg, bg, bg);
                    } else {
                        new_image.put((int) y, (int) x, fg, fg, fg);
                    }
                }
            }
        }

        Mat sharpen = new Mat();
        Imgproc.GaussianBlur(new_image, sharpen, new Size(0,0), 3);
        Core.addWeighted(new_image, 1.5, sharpen, -0.5, 0, new_image);
        if(lightText > darkText) return new_image;
        else return img;
    }
    // Function for calculating median
    private double findMedian(double a[]) {
        int n=a.length;
        // First we sort the array
        Arrays.sort(a);

        // check for even case
        if (n % 2 != 0)
            return a[n / 2];

        return (a[(n - 1) / 2] + a[n / 2]) / 2.0;
    }


    private double ii(double xx,double yy){
        if ((yy >= img_y) || (xx >= img_x)) {
            //System.out.println("pixel out of bounds ("+str(y)+","+str(x)+")");
            return -1;
        }
        double[] pixel = img.get((int)yy,(int)xx);
        //return 0.2126 * pixel[0] + 0.7152 * pixel[1] + 0.0722 * pixel[2];
        return 0.299 * pixel[0] + 0.587 * pixel[1] + 0.114 * pixel[2];
    }

    // Whether we care about this contour
    private boolean keep(MatOfPoint contour){
        return keep_box(contour) && connected(contour);
    }

    private boolean keep_box(MatOfPoint contour) {
        Rect rect=Imgproc.boundingRect(contour);
        double x=rect.x,y=rect.y,
                w=rect.width * 1.0,
                h=rect.height * 1.0;

        // Test it's shape - if it's too oblong or tall it's
        // probably not a real character
        if (w / h < 0.05 || w/h > 20)return false;
        // check size of the box
        if ((w * h) > ((img_x * img_y) / 2))
                //|| ((w * h) < 4))
            return false;
        return true;
    }

    private boolean connected(MatOfPoint contour) {
        //# A quick test to check whether the contour is
        //# a connected shape
        double[] first = contour.get(0,0);
        double[] last = contour.get(contour.rows() - 1,0);
        return abs(first[0] - last[0]) <= 1 && abs(first[1] - last[1]) <= 1;
    }

    private boolean include_box(int index,Mat  h_,MatOfPoint contour){
        // if DEBUG: print str(index) + ":"
        //if (is_child(index, h_))
        // print "\tIs a child"
        // print "\tparent " + str(get_parent(index, h_)) + " has " + str(
        // count_children(get_parent(index, h_), h_, contour)) + " children"
        //  print "\thas " + str(count_children(index, h_, contour)) + " children"

        if(is_child(index, h_) && (count_children((int)get_parent(index, h_), h_, contour) <= 5))
            //if DEBUG: print "\t skipping: is an interior to a letter"
            return false;

        if (count_children(index, h_, contour) > 5)
            //if DEBUG: print "\t skipping, is a container of letters"
            return false;

        //if DEBUG: print "\t keeping"
        return true;
    }

    private boolean is_child(int index, Mat h_) {
        return get_parent(index, h_) > 0;
    }

    private int count_children(int index, Mat h_, MatOfPoint contour) {
        int count;
        //# No children
        if (h_.get(0,index)[2] < 0) return 0;
            //#If the first child is a contour we care about
            //# then count it, otherwise don't
        else
        {
            if(keep(c((int)h_.get(0,index)[2]))) count = 1;
            else count = 0;
            //# Also count all of the child's siblings and their children
            count += count_siblings((int)h_.get(0,index)[2], h_, contour, true);
        }
        return count;
    }

    private MatOfPoint c(int index) {
        return contours.get(index);
    }
    private double get_parent(int index, Mat h_) {
        double d=h_.get(0,index)[3];
        double parent = (int) d;
        while (parent > 0 && !keep(c((int)parent)) )
            parent = h_.get(0,(int)parent)[3];
        return parent;
    }

    private int count_siblings(int index, Mat h_, MatOfPoint contour, boolean inc_children) {
        int count;
        //# Include the children if necessary
        if (inc_children)
            count = count_children(index, h_, contour);
        else count=0;
        //# Look ahead
        int  p_ =(int) h_.get(0,index)[0];
        while (p_ > 0) {
            if (keep(c(p_)))
                count += 1;
            if (inc_children)
                count += count_children(p_, h_, contour);
            p_ =(int) h_.get(0,p_)[0];
        }
        //# Look behind
        int n =(int) h_.get(0,index)[1];
        while (n > 0)
        {       if (keep(c(n)))
            count += 1;
            if (inc_children)
                count += count_children(n, h_, contour);
            n =(int) h_.get(0,n)[1];
        }
        return count;
    }

    private void printExternalContours(Mat img, List<MatOfPoint> contours, Mat hierarchy, int idx) {
        //for every contour of the same hierarchy level
        for(int i = idx; i >= 0; i = (int) hierarchy.get(0, i)[0])
        {
            AppLogger.i(Arrays.toString(hierarchy.get(0, i)));
            Imgproc.drawContours(img, contours, i, new Scalar(255));
            //for every of its internal contours
            for(int j = (int) hierarchy.get(0, i)[2]; j >= 0; j = (int) hierarchy.get(0, j)[0])
            {
                //recursively print the external contours of its children
               // printExternalContours(img, contours, hierarchy, (int) hierarchy.get(j, 2)[0]);
            }
        }
    }

}
