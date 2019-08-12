package com.vgu.dungluong.cardscannerapp.ui.crop;

import android.graphics.Bitmap;
import android.os.Handler;

import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CardProcessor;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import androidx.databinding.ObservableBoolean;

/**
 * Created by Dung Luong on 19/06/2019
 */
public class CropViewModel extends BaseViewModel<CropNavigator> {

    private Mat mPicture;

    private Corners mCorners;

    private ObservableBoolean mIsCardSelected;

    public CropViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mPicture = SourceManager.getInstance().getPic();
        mCorners = SourceManager.getInstance().getCorners();
        mIsCardSelected = new ObservableBoolean(false);
    }

    public void prepareImage(){
        setIsLoading(true);
        Bitmap bitmap = Bitmap.createBitmap(mPicture.width() != 0 ? mPicture.width() : 1080,
                mPicture.height() != 0 ? mPicture.height() : 1920, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mPicture, bitmap, true);

        int cardHeight = (int) (getScaleRatioWidth() * mPicture.height());
        AppLogger.i("height " + getScaleRatioWidth() + " " + mPicture.height() + " " + cardHeight);
        if(getIsCardSelected().get()){
            getNavigator().getCroppedPaper2().setImageBitmap(Bitmap.createScaledBitmap(bitmap,
                    getNavigator().getCroppedPaper2().getWidth(), cardHeight, false));
        }
        else getNavigator().getCroppedPaper().setImageBitmap(bitmap);

        AppLogger.i(mCorners.toString());
        if(getIsCardSelected().get()) getNavigator().getPaperRect2().onCorners2Crop(mCorners, mPicture.size(), cardHeight);
        else getNavigator().getPaperRect().onCorners2Crop(mCorners, mPicture.size(), 0);
        setIsLoading(false);
    }

    public void crop(){
        setIsLoading(true);
        mPicture = CardProcessor.cropPicture(mPicture, getIsCardSelected().get() ? getNavigator().getPaperRect2().getCorners2Crop() : getNavigator().getPaperRect().getCorners2Crop());
        SourceManager.getInstance().setPic(mPicture);
        getNavigator().openResultActivity();
    }

    private double getScaleRatioWidth(){
        return (double) getNavigator().getPaperRect2().getWidth() / mPicture.width();
    }

    public ObservableBoolean getIsCardSelected() {
        return mIsCardSelected;
    }

    public void setIsCardSelected(boolean isCardSelected) {
        mIsCardSelected.set(isCardSelected);
    }
}
