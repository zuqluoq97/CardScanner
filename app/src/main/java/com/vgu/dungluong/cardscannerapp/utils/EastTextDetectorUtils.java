package com.vgu.dungluong.cardscannerapp.utils;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Dung Luong on 16/07/2019
 */
public class EastTextDetectorUtils {

    private EastTextDetectorUtils() {
        // This utility class is not publicly instantiable
    }

    public static void test(Mat img){
        float scoreThresh = 0.5f;
        float nmsThresh = 0.4f;
        // Model from https://github.com/argman/EAST
        // You can find it here : https://github.com/opencv/opencv_extra/blob/master/testdata/dnn/download_models.py#L309
        Net net = Dnn.readNetFromTensorflow(AppConstants.DATA_PATH + AppConstants.EASTMODEL + AppConstants.EAST);
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2RGB);
        Size siz = new Size(320, 320);
        int W = (int)(siz.width / 4); // width of the output geometry  / score maps
        int H = (int)(siz.height / 4); // height of those. the geometry has 4, vertically stacked maps, the score one 1
        Mat blob = Dnn.blobFromImage(img, 1.0,siz, new Scalar(123.68, 116.78, 103.94), true, false);
    }

}
