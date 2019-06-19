package com.vgu.dungluong.cardscannerapp.di;

import android.content.Context;
import android.hardware.Camera;

import com.vgu.dungluong.cardscannerapp.data.permission.PermissionHelper;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

import static com.vgu.dungluong.cardscannerapp.utils.PaperProcessor.processPicture;

/**
 * Created by Dung Luong on 17/06/2019
 */
@Singleton
public class AppDataManager implements DataManager{

    private final Context mContext;

    private final PermissionHelper mPermissionHelper;

    @Inject
    public AppDataManager(Context context,
                          PermissionHelper permissionHelper){
        this.mContext = context;
        this.mPermissionHelper = permissionHelper;
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
    public Observable<Boolean> handleTakenPictureByte(byte[] bytes, Camera camera) {
        Camera.Size pictureSize = camera.getParameters().getPictureSize();
        AppLogger.i("Picture size " + pictureSize.toString());
        Mat mat = new Mat(new Size(pictureSize.width != 0 ? pictureSize.width : 1920.0,
                pictureSize.height != 0 ? pictureSize.height : 1920.0),  CvType.CV_8U);
        mat.put(0, 0, bytes);
        Mat pic = Imgcodecs.imdecode(mat, -1);
        Core.rotate(pic, pic, Core.ROTATE_90_CLOCKWISE);
        mat.release();
        SourceManager.getInstance().setCorners(processPicture(pic));
        Imgproc.cvtColor(pic, pic, Imgproc.COLOR_RGB2BGRA);
        SourceManager.getInstance().setPic(pic);
        return Observable.just(true);
    }
}
