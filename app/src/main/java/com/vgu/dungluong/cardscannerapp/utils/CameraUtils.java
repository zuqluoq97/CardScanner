package com.vgu.dungluong.cardscannerapp.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.ViewGroup;

import com.vgu.dungluong.cardscannerapp.ui.base.BaseActivity;
import com.vgu.dungluong.cardscannerapp.ui.main.MainActivity;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;

/**
 * Created by Dung Luong on 19/06/2019
 */
public class CameraUtils {

    private CameraUtils(){

    }

    public static Camera.Size getMaxResolution(Camera.Parameters parameters){
        return parameters.getSupportedPreviewSizes().stream().max(Comparator.comparing(size -> size.width)).orElse(null);
    }

    /**
     * Compares two sizes based on their areas.
     */
    public static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static double getMedian(Mat hist) {
        // binapprox algorithm

        long n = hist.total();
        byte[] histBuff = new byte[(int) n];
        hist.get(0, 0, histBuff);

        // Compute the mean and standard deviation
        // int n = x.length;
        double sum = 0;
        // int i;
        for (int i = 0; i < n; i++) {
            sum += histBuff[i];
        }
        double mu = sum / n;

        sum = 0;
        for (int i = 0; i < n; i++) {
            sum += (histBuff[i] - mu) * (histBuff[i] - mu);
        }
        double sigma = Math.sqrt(sum / n);

        // Bin x across the interval [mu-sigma, mu+sigma]
        int bottomcount = 0;
        int[] bincounts = new int[1001];
        for (int i = 0; i < 1001; i++) {
            bincounts[i] = 0;
        }
        double scalefactor = 1000 / (2 * sigma);
        double leftend = mu - sigma;
        double rightend = mu + sigma;
        int bin;

        for (int i = 0; i < n; i++) {
            if (histBuff[i] < leftend) {
                bottomcount++;
            } else if (histBuff[i] < rightend) {
                bin = (int) ((histBuff[i] - leftend) * scalefactor);
                bincounts[bin]++;
            }
        }

        double median = 0;
        // If n is odd
        if ((n % 2) != 0) {
            // Find the bin that contains the median
            int k = (int) ((n + 1) / 2);
            int count = bottomcount;

            for (int i = 0; i < 1001; i++) {
                count += bincounts[i];

                if (count >= k) {
                    median = (i + 0.5) / scalefactor + leftend;
                }
            }
        }

        // If n is even
        else {
            // Find the bins that contains the medians
            int k = (int) (n / 2);
            int count = bottomcount;

            for (int i = 0; i < 1001; i++) {
                count += bincounts[i];

                if (count >= k) {
                    int j = i;
                    while (count == k) {
                        j++;
                        count += bincounts[j];
                    }
                    median = (i + j + 1) / (2 * scalefactor) + leftend;
                }
            }
        }
        return median;
    }

    public static void autoCanny(Mat src, double sigma){
        double median = 50;
        double lowerThreshold = Math.max(0, (1.0 - sigma) * median);
        double higherThreshold = Math.min(255, (1.0 + sigma) * median);
        Imgproc.Canny(src, src, lowerThreshold, higherThreshold);
    }
}
