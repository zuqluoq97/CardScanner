package com.vgu.dungluong.cardscannerapp.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.util.Pair;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.vgu.dungluong.cardscannerapp.data.local.locale.LocaleHelper;
import com.vgu.dungluong.cardscannerapp.data.model.api.Labels;
import com.vgu.dungluong.cardscannerapp.data.model.api.Rects;
import com.vgu.dungluong.cardscannerapp.data.local.permission.PermissionHelper;
import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;
import com.vgu.dungluong.cardscannerapp.data.local.preference.PreferenceHelper;
import com.vgu.dungluong.cardscannerapp.data.remote.ApiHelper;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CardProcessor;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;

import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;

import static com.vgu.dungluong.cardscannerapp.utils.CardProcessor.processPicture;

/**
 * Created by Dung Luong on 17/06/2019
 */
@Singleton
public class AppDataManager implements DataManager{

    private final PermissionHelper mPermissionHelper;

    private final PreferenceHelper mPreferenceHelper;

    private final ApiHelper mApiHelper;

    private final LocaleHelper mLocaleHelper;

    @Inject
    public AppDataManager(PermissionHelper permissionHelper,
                          PreferenceHelper preferenceHelper,
                          ApiHelper apiHelper,
                          LocaleHelper localeHelper){
        this.mPermissionHelper = permissionHelper;
        this.mPreferenceHelper = preferenceHelper;
        this.mApiHelper = apiHelper;
        this.mLocaleHelper = localeHelper;
    }

    @Override
    public Observable<Boolean> handleTakenPicture(byte[] bytes, Camera camera, List<Point> cropCoordinates) {
        Camera.Size pictureSize = camera.getParameters().getPictureSize();
        AppLogger.i("Picture size " + pictureSize.height + " " + pictureSize.width);

        Mat mat = new Mat(new Size(pictureSize.width != 0 ? pictureSize.width : 1920.0,
                pictureSize.height != 0 ? pictureSize.height : 1080.0),  CvType.CV_8UC1);
        mat.put(0, 0, bytes);

        // Rean an image from a buffer in memory
        Mat pic = Imgcodecs.imdecode(mat, -1);
        Core.rotate(pic, pic, Core.ROTATE_90_CLOCKWISE);
        pic = CardProcessor.cropPicture(pic, cropCoordinates);
        mat.release();
        // Set corners && picture
        Corners corners = processPicture(pic);
        SourceManager.getInstance().setCorners(corners);
        Imgproc.cvtColor(pic, pic, Imgproc.COLOR_RGB2BGR);
        SourceManager.getInstance().setPic(pic);

        return Observable.just(true);
    }

    @Override
    public Observable<Boolean> handleSeletedPicture(Bitmap bitmap, int orientation) {
        // Rean an image from a buffer in memory
        Mat pic = new Mat();

        Utils.bitmapToMat(bitmap, pic);
        switch(orientation) {
            case 90:
                Core.rotate(pic, pic, Core.ROTATE_90_CLOCKWISE);
                break;
            case 180:
                Core.rotate(pic, pic, Core.ROTATE_180);
                break;
            case 270:
                Core.rotate(pic, pic, Core.ROTATE_90_COUNTERCLOCKWISE);
                break;
            default:
                break;
        }

        // Set corners && picture
        Corners corners = processPicture(pic);
        SourceManager.getInstance().setCorners(corners);
        SourceManager.getInstance().setPic(pic);

        return Observable.just(true);
    }

    @Override
    public Observable<Pair<String, Float>> doTesseract(Bitmap bitmap, TessBaseAPI tessBaseAPI) {
        String result = "";
        tessBaseAPI.setImage(bitmap);
        result = tessBaseAPI.getUTF8Text();
        ResultIterator iterator = tessBaseAPI.getResultIterator();
        int level = TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE;
        float confident = iterator.confidence(level);
        AppLogger.i(result + " " + confident);
        return Observable.just(new Pair<>(result, confident));
    }

    @Override
    public boolean hasUseCamera() {
        return mPermissionHelper.hasUseCamera();
    }

    @Override
    public boolean hasReadExternalStorage() {
        return mPermissionHelper.hasReadExternalStorage();
    }

    @Override
    public boolean hasWriteExternalStorage() {
        return mPermissionHelper.hasWriteExternalStorage();
    }

    @Override
    public boolean hasWriteContact() {
        return mPermissionHelper.hasWriteContact();
    }

    @Override
    public boolean hasReadContact() {
        return mPermissionHelper.hasReadContact();
    }

    @Override
    public Single<Rects> doServerTextDetection(File imgFile) {
        return mApiHelper.doServerTextDetection(imgFile);
    }

    @Override
    public Single<Labels> doServerTextClassification(JSONObject textUnLabelJSONObject) {
        return mApiHelper.doServerTextClassification(textUnLabelJSONObject);
    }

    @Override
    public String getLocale() {
        return mPreferenceHelper.getLocale();
    }

    @Override
    public void setLocale(String language) {
        mPreferenceHelper.setLocale(language);
    }

    @Override
    public Context setNewLocale(Context c, String language) {
        return mLocaleHelper.setNewLocale(c, language);
    }

    @Override
    public Context setLocale(Context c) {
        return mLocaleHelper.setLocale(c);
    }
}
