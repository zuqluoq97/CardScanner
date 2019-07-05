package com.vgu.dungluong.cardscannerapp.ui.result;

import android.graphics.Bitmap;

import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CardProcessor;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.Objects;

/**
 * Created by Dung Luong on 02/07/2019
 */
public class ResultViewModel extends BaseViewModel<ResultNavigator> {

    private Mat mCardPicture;

    public ResultViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mCardPicture = SourceManager.getInstance().getPic();
    }

    public void displayCardImage(){
        if(Objects.requireNonNull(mCardPicture).height() > mCardPicture.width()) rotate();
        int cardHeight = getNavigator().getCardImageView().getWidth() * mCardPicture.height() / mCardPicture.width();
        Bitmap bitmap = Bitmap.createBitmap(mCardPicture.width(), mCardPicture.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mCardPicture, bitmap, true);
        getNavigator().getCardImageView().setImageBitmap(Bitmap.createScaledBitmap(bitmap,
                getNavigator().getCardImageView().getWidth(), cardHeight, false));

    }

    public void rotate(){
        Core.rotate(mCardPicture, mCardPicture, Core.ROTATE_90_CLOCKWISE);
        displayCardImage();
    }

    public void ocr(){
        CardProcessor.textSkewCorrection(mCardPicture, getDataManager().getScanBlackCardState());
    }
}
