package com.vgu.dungluong.cardscannerapp.di;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

import com.vgu.dungluong.cardscannerapp.data.permission.PermissionHelper;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
                pictureSize.height != 0 ? pictureSize.height : 1080.0),  CvType.CV_8U);
        mat.put(0, 0, bytes);
        AppLogger.i(mat.toString());
        Mat pic = Imgcodecs.imdecode(mat, -1);
        Core.rotate(pic, pic, Core.ROTATE_90_CLOCKWISE);
        mat.release();
        SourceManager.getInstance().setCorners(processPicture(pic));
        AppLogger.i(pic.toString());
        Imgproc.cvtColor(pic, pic, Imgproc.COLOR_RGB2BGRA);
        SourceManager.getInstance().setPic(pic);
        return Observable.just(true);
    }

    @Override
    public Observable<Mat> handlePictureFrame(byte[] bytes, Camera camera) {
        AppLogger.i("Start prepare paper");
        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;
        YuvImage yuv = new YuvImage(bytes, parameters.getPreviewFormat(), width != 0 ? width : 1080, height != 0 ? height : 1920, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width != 0 ? width : 1080, height != 0 ? height : 1920), 100, out);
        byte[] outBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(outBytes, 0, outBytes.length);

        Mat img = new Mat();
        Utils.bitmapToMat(bitmap, img);
        bitmap.recycle();
        Core.rotate(img, img, Core.ROTATE_90_CLOCKWISE);

        try {
            out.close();
        }catch (IOException ie){
            AppLogger.e(ie.getLocalizedMessage());
        }
        return Observable.just(img);
    }

    @Override
    public Observable<Boolean>  handleTakenPictureByte2(byte[] bytes, int previewHeight, int previewWidth) {
        AppLogger.i("Preview size " + previewHeight + " " + previewWidth);
        Mat mat = new Mat(new Size((double) previewWidth, (double) previewHeight),  CvType.CV_8U);
        mat.put(0, 0, bytes);
        AppLogger.i(mat.toString());
        // Read an image from a buffer in memory
        Mat pic = Imgcodecs.imdecode(mat, -1);
        // Rotate Matrix pic itself 90 degree
        Core.rotate(pic, pic, Core.ROTATE_90_CLOCKWISE);
        mat.release();
        SourceManager.getInstance().setCorners(processPicture(pic));
        AppLogger.i(pic.toString());
        // Converts an image from RGB to BGRA
        Imgproc.cvtColor(pic, pic, Imgproc.COLOR_RGB2BGRA);
        SourceManager.getInstance().setPic(pic);
        return Observable.just(true);
    }
}
