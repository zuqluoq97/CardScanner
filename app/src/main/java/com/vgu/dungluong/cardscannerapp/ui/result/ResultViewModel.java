package com.vgu.dungluong.cardscannerapp.ui.result;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.AppConstants;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CardProcessor;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.TESSDATA;

/**
 * Created by Dung Luong on 02/07/2019
 */
public class ResultViewModel extends BaseViewModel<ResultNavigator> {

    private Mat mCardPicture;

    private ObservableBoolean mIsOCRSucceed;

    private ObservableField<String> mResultString;

    private List<Bitmap> bm;

    private int idx = 0;

    public ResultViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mIsOCRSucceed = new ObservableBoolean(false);
        mResultString = new ObservableField<>("");
        bm = new ArrayList<>();
        mCardPicture = SourceManager.getInstance().getPic();
        AppLogger.i(mCardPicture.height() + " " + mCardPicture.width());
        if(Objects.requireNonNull(mCardPicture).height() > mCardPicture.width())
            Core.rotate(mCardPicture, mCardPicture, Core.ROTATE_90_CLOCKWISE);
    }

    public void displayCardImage(){

        int cardHeight = getNavigator().getCardImageView().getWidth() * mCardPicture.height() / mCardPicture.width();
        Bitmap bitmap = Bitmap.createBitmap(mCardPicture.width(), mCardPicture.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mCardPicture, bitmap, true);

//        if(mIsOCRSucceed.get()){
//            tesseract(bitmap);
//        }

        getNavigator().getCardImageView().setImageBitmap(Bitmap.createScaledBitmap(bitmap,
                getNavigator().getCardImageView().getWidth(), cardHeight, false));

    }

    public void next(){
        displayNextImage(bm.get(idx));
        idx ++;
    }

    public void displayNextImage(Bitmap bm){
        getNavigator().getCardImageView().setImageBitmap(bm);
    }

    public void rotate(){
        Core.rotate(mCardPicture, mCardPicture, Core.ROTATE_180);
        displayCardImage();
    }

    public void ocr(){
        setIsLoading(true);
        getCompositeDisposable().add(CardProcessor
                .textSkewCorrection(mCardPicture)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(bitmaps -> {
                    setIsOCRSucceed(true);
                    getNavigator().showMessage("Pre-image processing success");
                    //displayCardImage();

//                    tesseract(bitmaps);
//                    bm.addAll(bitmaps);
                }, throwable -> {
                    getNavigator().handleError(throwable.getLocalizedMessage());
                    setIsLoading(false);
                }));

    }

    public void tesseract(List<Bitmap> bitmap){
        getCompositeDisposable().add(getDataManager()
                .doTesseract(bitmap, getNavigator().getTesseractApi())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(result -> {
                    setIsOCRSucceed(false);
                    setIsLoading(false);
                    getNavigator().showMessage("OCR success");
                    mResultString.set(result);
                }, throwable -> {
                    getNavigator().handleError(throwable.getLocalizedMessage());
                    setIsLoading(false);
                }));
    }

    public ObservableBoolean getIsOCRSucceed() {
        return mIsOCRSucceed;
    }

    public void setIsOCRSucceed(boolean isOCRSucceed) {
        mIsOCRSucceed.set(isOCRSucceed);
    }

    public ObservableField<String> getResultString() {
        return mResultString;
    }
}
