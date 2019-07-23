package com.vgu.dungluong.cardscannerapp.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.vgu.dungluong.cardscannerapp.data.permission.PermissionHelper;
import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;
import com.vgu.dungluong.cardscannerapp.data.preference.PreferenceHelper;
import com.vgu.dungluong.cardscannerapp.data.remote.ApiHelper;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CardProcessor;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
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

    private final Context mContext;

    private final PermissionHelper mPermissionHelper;

    private final PreferenceHelper mPreferenceHelper;

    private final ApiHelper mApiHelper;

    @Inject
    public AppDataManager(Context context,
                          PermissionHelper permissionHelper,
                          PreferenceHelper preferenceHelper,
                          ApiHelper apiHelper){
        this.mContext = context;
        this.mPermissionHelper = permissionHelper;
        this.mPreferenceHelper = preferenceHelper;
        this.mApiHelper = apiHelper;
    }

    @Override
    public Observable<Boolean> handleTakenPictureByte(byte[] bytes, Camera camera, List<Point> cropCoordinates) {
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
        Imgproc.cvtColor(pic, pic, Imgproc.COLOR_RGB2BGRA);
        SourceManager.getInstance().setPic(pic);

        return Observable.just(true);
    }

    @Override
    public Observable<String> doTesseract(List<Bitmap> bitmap, TessBaseAPI tessBaseAPI) {
        String result = "";
        for(int i =0; i < bitmap.size(); i++){
            Bitmap bm = bitmap.get(i);
            tessBaseAPI.setImage(bm);
            result += tessBaseAPI.getUTF8Text();
        }

        String hocr = tessBaseAPI.getHOCRText(0);
        String boxText = tessBaseAPI.getBoxText(0);
//        AppLogger.i(hocr);
//        AppLogger.i(boxText);
//        AppLogger.i(result);
        tessBaseAPI.end();
        return Observable.just(result);
    }

    @Override
    public void setScanBlackCardState(boolean scanBlackCardState) {
        mPreferenceHelper.setScanBlackCardState(scanBlackCardState);
    }

    @Override
    public boolean getScanBlackCardState() {
        return mPreferenceHelper.getScanBlackCardState();
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
    public Single<String> doServerTextDetection(File imgFile) {
        return mApiHelper.doServerTextDetection(imgFile);
    }
}