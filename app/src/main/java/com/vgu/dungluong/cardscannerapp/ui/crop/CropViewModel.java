package com.vgu.dungluong.cardscannerapp.ui.crop;

import android.graphics.Bitmap;
import android.os.Handler;

import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.CardProcessor;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Created by Dung Luong on 19/06/2019
 */
public class CropViewModel extends BaseViewModel<CropNavigator> {

    private Mat mPicture;

    private Corners mCorners;

    public CropViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mPicture = SourceManager.getInstance().getPic();
        mCorners = SourceManager.getInstance().getCorners();
    }

    public void prepareImage(){
        setIsLoading(true);
        Bitmap bitmap = Bitmap.createBitmap(mPicture.width() != 0 ? mPicture.width() : 1080,
                mPicture.height() != 0 ? mPicture.height() : 1920, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mPicture, bitmap, true);
        new Handler().postDelayed(() -> {
            getNavigator().getPaperRect().onCorners2Crop(mCorners, mPicture.size());
            setIsLoading(false);
        }, 1000);

        getNavigator().getCroppedPaper().setImageBitmap(bitmap);
    }

    public void crop(){
        setIsLoading(true);
        mPicture = CardProcessor.cropPicture(mPicture, getNavigator().getPaperRect().getCorners2Crop());
        SourceManager.getInstance().setPic(mPicture);
        setIsLoading(false);
        getNavigator().openResultActivity();
    }
}
