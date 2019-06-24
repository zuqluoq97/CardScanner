package com.vgu.dungluong.cardscannerapp.ui.crop;

import android.graphics.Bitmap;

import com.vgu.dungluong.cardscannerapp.di.DataManager;
import com.vgu.dungluong.cardscannerapp.model.local.Corners;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
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

    private Mat mCroppedPicture;

    private Mat mEnhanedPicture;

    private Mat mCroppedBitmap;

    public CropViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mPicture = SourceManager.getInstance().getPic();
      //  mCorners = SourceManager.getInstance().getCorners();
    }

    public void prepareImage(){
        getNavigator().getPaperRect().onCorners2Crop(mCorners, mPicture.size());
        Bitmap bitmap = Bitmap.createBitmap(mPicture.width() != 0 ? mPicture.width() : 1080,
                mPicture.height() != 0 ? mPicture.height() : 1920, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mPicture, bitmap, true);
        getNavigator().getCard().setImageBitmap(bitmap);
    }

}
