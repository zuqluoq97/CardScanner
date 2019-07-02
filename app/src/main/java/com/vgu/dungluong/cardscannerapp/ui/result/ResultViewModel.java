package com.vgu.dungluong.cardscannerapp.ui.result;

import android.graphics.Bitmap;

import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Dung Luong on 02/07/2019
 */
public class ResultViewModel extends BaseViewModel<ResultNavigator> {

    private Mat mCardPicture;

    public ResultViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mCardPicture = SourceManager.getInstance().getPic();
        Imgproc.cvtColor(mCardPicture, mCardPicture, Imgproc.COLOR_BGR2GRAY);
        Core.bitwise_not(mCardPicture, mCardPicture);
        Imgproc.threshold(mCardPicture, mCardPicture, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Mat white = new Mat(mCardPicture.size(), CvType.CV_8UC1);
        Core.findNonZero(mCardPicture, white);
        MatOfPoint points = new MatOfPoint(white);
        MatOfPoint2f points2f = new MatOfPoint2f(points.toArray());
        RotatedRect rotatedRect = Imgproc.minAreaRect(points2f);
        double angle = rotatedRect.angle;
        if(rotatedRect.size.width < rotatedRect.size.height){
            angle = 90 + angle;
        }
        AppLogger.i(String.valueOf(angle));

    }

    public void displayCardImage(){
        Bitmap bitmap = Bitmap.createBitmap(mCardPicture.width() != 0 ? mCardPicture.width() : 1080,
                mCardPicture.height() != 0 ? mCardPicture.height() : 1920, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mCardPicture, bitmap, true);
        getNavigator().getCardImageView().setImageBitmap(bitmap);
    }
}
