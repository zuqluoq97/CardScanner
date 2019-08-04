package com.vgu.dungluong.cardscannerapp.ui.result;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Rotate;
import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CardExtract;
import com.vgu.dungluong.cardscannerapp.utils.CardProcessor;
import com.vgu.dungluong.cardscannerapp.utils.CommonUtils;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private int idx;

    private int idx2;

    private List<String> mOCRs;

    public ResultViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mIsOCRSucceed = new ObservableBoolean(false);
        mResultString = new ObservableField<>("");
        bms = new ArrayList<>();
        mOCRs = new ArrayList<>();
        mCardPicture = SourceManager.getInstance().getPic();
        AppLogger.i(mCardPicture.height() + " " + mCardPicture.width());
        if(Objects.requireNonNull(mCardPicture).height() > mCardPicture.width())
            Core.rotate(mCardPicture, mCardPicture, Core.ROTATE_90_CLOCKWISE);
//        CardProcessor.performGammaCorrection(0.8, mCardPicture);
//        mCardPicture = CardProcessor.improveContrast(mCardPicture);
        mCardPicture = CardExtract.run(mCardPicture);
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
        File imgFile = getNavigator().getFileForCropImage();
        saveBitmapToJpg(mBitmap, imgFile, 300);
        Bitmap bm = get300DPIBitmap(imgFile);
        Utils.bitmapToMat(bm, mCardPicture);

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
                    bms = bitmaps;
                    tesseract(bitmaps);
                    setIsLoading(false);
                }, throwable -> {
                    setIsLoading(false);
                    AppLogger.e(throwable.getLocalizedMessage());
                }));
    }

    public void tesseract(List<Bitmap> bitmap){
        for(int i = 0; i < bitmap.size(); i++){
            Bitmap bm = bitmap.get(i);
            getCompositeDisposable().add(getDataManager()
                    .doTesseract(bm, getNavigator().getTesseractApi())
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(ocr -> {
                        AppLogger.i(ocr.first + " " + ocr.second);
//                        if(ocr.second < 50){
//                            getCompositeDisposable().add(getDataManager()
//                                    .doTesseract(bm, getNavigator().getTesseractApi2())
//                                    .subscribeOn(getSchedulerProvider().io())
//                                    .observeOn(getSchedulerProvider().ui())
//                                    .subscribe(secondOcr -> {
//                                        AppLogger.i(secondOcr.first + " " + secondOcr.second);
//                                       // if(secondOcr.first.length() / ocr.first.length() < 2) {
//                                            double similarity = CommonUtils.similarity(secondOcr.first, ocr.first);
//                                            if (similarity > 0.3) mOCRs.add(ocr.first);
//                                            else mOCRs.add(secondOcr.first);
//                                            AppLogger.i(String.valueOf(similarity));
//                                       // } else mOCRs.add(secondOcr.first);
//                                        idx2++;
//                                        if(idx2 == bitmap.size()) displayOCR();
//                                    }, throwable -> {
//                                        getNavigator().handleError(throwable.getLocalizedMessage());
//                                        setIsLoading(false);
//                                    }));
//                        }else{
                            mOCRs.add(ocr.first);
                            idx2++;
                            if(idx2 == bitmap.size()) displayOCR();
                       // }

                    }, throwable -> {
                        getNavigator().handleError(throwable.getLocalizedMessage());
                        setIsLoading(false);
                    }));
        }
    }

    private void displayOCR(){
        setIsLoading(false);
        final String[] result = {""};
        mOCRs.forEach(ocr -> result[0] += ocr + "\n");
        mResultString.set(result[0]);
    }

    private Bitmap get300DPIBitmap(File file){
        Bitmap bm = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            options.inTargetDensity = 300;
            bm = BitmapFactory.decodeFile(file.getPath(), options);
        } catch (Exception e) {
            AppLogger.e(e.getLocalizedMessage());
        }
        return bm;
    }

    public void saveBitmapToJpg(Bitmap bitmap, File file, int dpi){
        try {
            ByteArrayOutputStream imageByteArray = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageByteArray);
            byte[] imageData = imageByteArray.toByteArray();

            setDpi(imageData, dpi);

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(imageData);
            fileOutputStream.close();
        }catch (IOException e){
            AppLogger.e(e.getLocalizedMessage());
        }
    }

    private void setDpi(byte[] imageData, int dpi) {
        imageData[13] = 1;
        imageData[14] = (byte) (dpi >> 8);
        imageData[15] = (byte) (dpi & 0xff);
        imageData[16] = (byte) (dpi >> 8);
        imageData[17] = (byte) (dpi & 0xff);
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
