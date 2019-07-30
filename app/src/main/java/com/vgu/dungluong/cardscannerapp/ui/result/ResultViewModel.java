package com.vgu.dungluong.cardscannerapp.ui.result;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Rotate;
import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

/**
 * Created by Dung Luong on 02/07/2019
 */
public class ResultViewModel extends BaseViewModel<ResultNavigator> {

    private Mat mCardPicture;

    private ObservableBoolean mIsOCRSucceed;

    private ObservableField<String> mResultString;

    private Bitmap mBitmap;

    private List<Bitmap> bms;
    private int idx = 0;

    public ResultViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mIsOCRSucceed = new ObservableBoolean(false);
        mResultString = new ObservableField<>("");
        bms = new ArrayList<>();
        mCardPicture = SourceManager.getInstance().getPic();
        AppLogger.i(mCardPicture.height() + " " + mCardPicture.width());
        if(Objects.requireNonNull(mCardPicture).height() > mCardPicture.width())
            Core.rotate(mCardPicture, mCardPicture, Core.ROTATE_90_CLOCKWISE);
//        CardProcessor.performGammaCorrection(0.1, mCardPicture);
//        mCardPicture = CardProcessor.brightnessAndConstraintAuto(mCardPicture, 5);
        
    }

    public void displayCardImage(){

        int cardHeight = getNavigator().getCardImageView().getWidth() * mCardPicture.height() / mCardPicture.width();
        mBitmap = Bitmap.createBitmap(mCardPicture.width(), mCardPicture.height(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(mCardPicture, mBitmap, true);
        getNavigator().getCardImageView().setImageBitmap(Bitmap.createScaledBitmap(mBitmap,
                getNavigator().getCardImageView().getWidth(), cardHeight, false));


    }

    public void next(){
        getNavigator().getCardImageView().setImageBitmap(bms.get(idx));
        idx ++;
    }

    public void rotate(){
        Core.rotate(mCardPicture, mCardPicture, Core.ROTATE_180);
        displayCardImage();
    }

    public void textDetect(){
        setIsLoading(true);

        OutputStream os;
        File imgFile = getNavigator().getFileForCropImage();
        try{
            os = new FileOutputStream(imgFile);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        }catch (Exception e) {
            AppLogger.e(e.getLocalizedMessage());
        }

        // Change density to 300dpi
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            options.inTargetDensity = 300;
            Bitmap bm = BitmapFactory.decodeFile(imgFile.getPath(), options);
            Utils.bitmapToMat(bm, mCardPicture);
        } catch (Exception e) {
            AppLogger.e(e.getLocalizedMessage());
        }

        getCompositeDisposable().add(getDataManager()
                .doServerTextDetection(imgFile)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(rects -> {
                            cropTextArea(rects.getCorners());
                            displayCardImage();
                        },
                        throwable -> {
                            setIsLoading(false);
                            AppLogger.e(throwable.getLocalizedMessage());
                        }));
    }

    public void cropTextArea(List<Corners> textBoxCorners){
        getCompositeDisposable().add(CardProcessor
                .cropTextArea(mCardPicture, textBoxCorners)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(bitmaps -> {
                    setIsLoading(false);
                        bms = bitmaps;
                        tesseract(bitmaps);
                }, throwable -> {
                    setIsLoading(false);
                    AppLogger.e(throwable.getLocalizedMessage());
                }));
    }

    public void tesseract(List<Bitmap> bitmap){
        getCompositeDisposable().add(getDataManager()
                .doTesseract(bitmap, getNavigator().getTesseractApi())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(result -> {
                    //setIsOCRSucceed(true);
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
