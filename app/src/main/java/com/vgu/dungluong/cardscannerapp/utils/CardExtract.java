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

import static java.lang.Math.abs;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_PLAIN;

/**
 * Created by Dung Luong on 31/07/2019
 */
public class CardExtract {
    private static Mat img;
    private static int img_y, img_x;
    private static List<MatOfPoint> contours;

    private CardExtract() {
        // This utility class is not publicly instantiable
    }

    public static Mat run(Mat src) {
        img = src.clone();
        Imgproc.resize(img,img,new Size(640,480));
        Core.copyMakeBorder(img,img,50,50,50,50, Core.BORDER_CONSTANT);
        img_y=img.rows();
        img_x=img.cols();
        List<Mat> channels=new ArrayList<>();
        Core.split(img,channels);
        for(int i=0;i<3;i++) Imgproc.Canny(channels.get(i),channels.get(i),200,250);
        Mat edges=new Mat();
        Core.merge(channels,edges);
        contours=new ArrayList<>();
        Mat hierarchy=new Mat();
        Imgproc.cvtColor(edges,edges,Imgproc.COLOR_BGR2GRAY);
        Imgproc.findContours(edges,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_NONE);
        List<MatOfPoint> keepers = new ArrayList<>();
        Mat processed = edges.clone();
        Mat rejected = edges.clone();
        for(int i=0;i<contours.size();i++)
        {
            MatOfPoint contour=contours.get(i);
            Rect rect=Imgproc.boundingRect(contour);
            double x=rect.x,y=rect.y,w=rect.width,h=rect.height;
            if ( keep(contour) && include_box(i, hierarchy, contour)){
                // It's a winner!
                keepers.add(contour);
                Imgproc.rectangle(processed, new Point(x, y),new Point(x + w, y + h), new Scalar(100, 100, 100), 1);
                Imgproc.putText(processed, ""+i, new Point(x, y - 5), FONT_HERSHEY_PLAIN, 1 ,new Scalar(255, 255, 255));
            }
            else{
                Imgproc.rectangle(rejected, new Point(x, y), new Point(x + w, y + h), new Scalar(100, 100, 100), 1);
                Imgproc.putText(rejected, ""+i, new Point(x, y - 5), FONT_HERSHEY_PLAIN, 1, new Scalar(255, 255, 255));
            }

        }
        Mat new_image=img.clone();
        new_image.setTo(new Scalar(255,255,255));
        for(int i = 0; i< keepers.size(); i++)
        {
            //# Find the average intensity of the edge pixels to
            //# determine the foreground intensity
            double fg_int = 0;
            for (Point p: keepers.get(i).toArray())
                fg_int +=ii(p.x, p.y);
            fg_int /= keepers.get(i).rows();
            fg_int = (int)fg_int;
            Rect rect=Imgproc.boundingRect(keepers.get(i));
            double x_=rect.x, y_=rect.y, width=rect.width, height=rect.height;
            double [] bg_int = new double[]{
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
                    ii(x_ + width + 1, y_ + height)
            };
            //# Find the median of the background
            //# pixels determined above
            double bg_intN = findMedian(bg_int);
            //# Determine if the box should be inverted
            int fg,bg;
            if (fg_int >= bg_intN)
            {
                fg = 255;
                bg = 0;
            }
            else
            {
                fg = 0;
                bg = 255;
            }
            for(double x=x_;x<x_+width;x++)
            {
                for(double y=y_;y<y_+height;y++)
                {
                    if (y >= img_y || x >= img_x){
                        System.out.println("pixel out of bounds ("+y+","+x+")");
                        continue;
                    }
                    if (ii(x, y) > fg_int) new_image.put((int)y,(int)x,bg,bg,bg);
                    else new_image.put((int)y,(int)x,fg,fg,fg);
                }
            }
        }
        Imgproc.blur(new_image,new_image,new Size(2,2));

        return new_image;
    }

    // Function for calculating median
    private static double findMedian(double a[])
    {
        int n=a.length;
        // First we sort the array
        Arrays.sort(a);

        // check for even case
        if (n % 2 != 0)
            return a[n / 2];

        return (a[(n - 1) / 2] + a[n / 2]) / 2.0;
    }


    private static double ii(double xx,double yy){
        if ((yy >= img_y) || (xx >= img_x))
        {
            //System.out.println("pixel out of bounds ("+str(y)+","+str(x)+")");
            return 0;
        }
        double[] pixel = img.get((int)yy,(int)xx);
        double lout= (0.30 * pixel[2]) + (0.59 * pixel[1]) + (0.11 * pixel[0]);
        return lout;
    }
    // Whether we care about this contour
    static boolean keep(MatOfPoint contour){
        return keep_box(contour) && connected(contour);
    }

    private static boolean keep_box(MatOfPoint contour)
    {
        Rect rect= Imgproc.boundingRect(contour);
        double x=rect.x,y=rect.y,
                w=rect.width * 1.0,
                h=rect.height * 1.0;
        // Test it's shape - if it's too oblong or tall it's
        // probably not a real character
        if (w / h < 0.1 || w/h > 10)return false;
        // check size of the box
        if (((w * h) > ((img_x * img_y) / 5)) || ((w * h) < 15))return false;
        return true;
    }
    private static boolean connected(MatOfPoint contour)
    {
        //# A quick test to check whether the contour is
        //# a connected shape
        double[] first = contour.get(0,0);
        double[] last = contour.get(contour.rows() - 1,0);
        return abs(first[0] - last[0]) <= 1 && abs(first[1] - last[1]) <= 1;
    }
    private static boolean include_box(int index, Mat h_, MatOfPoint contour){
        // if DEBUG: print str(index) + ":"
        //if (is_child(index, h_))
        // print "\tIs a child"
        // print "\tparent " + str(get_parent(index, h_)) + " has " + str(
        // count_children(get_parent(index, h_), h_, contour)) + " children"
        //  print "\thas " + str(count_children(index, h_, contour)) + " children"

        if(is_child(index, h_) && (count_children((int)get_parent(index, h_), h_, contour) <= 2))
            //if DEBUG: print "\t skipping: is an interior to a letter"
            return false;

        if (count_children(index, h_, contour) > 2)
            //if DEBUG: print "\t skipping, is a container of letters"
            return false;

        //if DEBUG: print "\t keeping"
        return true;
    }

    private static boolean is_child(int index, Mat h_) {
        return get_parent(index, h_) > 0;
    }

    private static int count_children(int index, Mat h_, MatOfPoint contour) {
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

    private static MatOfPoint c(int index) {
        return contours.get(index);
    }
    private static double get_parent(int index, Mat h_) {
        double d=h_.get(0,index)[3];
        double parent = (int) d;
        while (parent > 0 && !keep(c((int)parent)) )
            parent = h_.get(0,(int)parent)[3];
        return parent;
    }

    private static int count_siblings(int index, Mat h_, MatOfPoint contour, boolean inc_children) {
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
}
