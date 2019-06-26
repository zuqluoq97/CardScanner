package com.vgu.dungluong.cardscannerapp.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup;

import com.vgu.dungluong.cardscannerapp.ui.main.MainActivity;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CameraUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Dung Luong on 25/06/2019
 */
public class CameraPreview implements SurfaceHolder.Callback {

    private Camera mCamera;

    private SurfaceHolder mSurfaceHolder;

    private final static String TAG = "CameraPreview";

    private MainActivity mMainActivity;

    private boolean mFocusAreaSupported, mMeteringAreaSupported;

    public CameraPreview(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        initCam();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        updateCam();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
       synchronized (this){
           releaseCam();
       }
    }

    /**
     * Called from PreviewSurfaceView to set touch focus.
     *
     * @param - Rect - new area for auto focus
     */
    public void doTouchFocus(final Rect tfocusRect, final Rect tMeteringRect) {
        AppLogger.i(TAG + ": TouchFocus");
        try {
            // cancel auto focus
            mCamera.cancelAutoFocus();
            Camera.Parameters para = mCamera.getParameters();
            para.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            AppLogger.i(mFocusAreaSupported + " " + mMeteringAreaSupported);
            List<String> supportedFocusModes = para.getSupportedFocusModes();
            if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                if (mFocusAreaSupported)
                    para.setMeteringAreas(Collections.singletonList(new Camera.Area(tfocusRect, 1000)));
                if (mMeteringAreaSupported)
                    para.setMeteringAreas(Collections.singletonList(new Camera.Area(tMeteringRect, 1000)));
                mCamera.setParameters(para);
                mCamera.autoFocus(myAutoFocusCallback);
            }
        } catch (Exception e) {
            AppLogger.e(e.getLocalizedMessage());
            AppLogger.e(TAG +  ": Unable to autofocus");
        }

    }

    /**
     * AutoFocus callback
     */
    private Camera.AutoFocusCallback myAutoFocusCallback = (arg0, arg1) -> {
            AppLogger.i(String.format("Auto focus success=%s. Focus mode: '%s'. Focused on: %s",
                    arg0,
                    arg1.getParameters().getFocusMode(),
                    arg1.getParameters().getFocusAreas().get(0).rect.toString()));
            Camera.Parameters param = mCamera.getParameters();
            new Handler().postDelayed(() -> {
                AppLogger.i("Turn on continous picture mode");
                mCamera.cancelAutoFocus();
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mCamera.setParameters(param);
            }, 3000);
    };


    public void initCam(){
        try{
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (RuntimeException re){
            AppLogger.e(re.getLocalizedMessage());
            return ;
        }

        if(mCamera != null){
            Camera.Parameters param = mCamera.getParameters();
            if(param != null) {
                // Maximum resolution that device screen can provide
                Camera.Size size = CameraUtils.getMaxResolution(param);
                param.setPreviewSize(size != null ? size.width : 1920,
                        size != null ? size.height : 1080);

                Point point = new Point();
                // Get real size of the device screen
                mMainActivity.getDisplay().getRealSize(point);

                // Higher value of height and smaller value for width
                int displayWidth = point.x < point.y ? point.x : point.y;
                int displayHeight = point.x > point.y ? point.x : point.y;

                float displayRatio = (float) displayWidth / displayHeight;
                float previewRatio = size != null ? (float) size.height / size.width : displayRatio;

                if(displayRatio > previewRatio){
                    ViewGroup.LayoutParams surfaceParams = mMainActivity.getSurfaceView().getLayoutParams();
                    surfaceParams.height = (int) (displayHeight / displayRatio * previewRatio);
                    mMainActivity.getSurfaceView().setLayoutParams(surfaceParams);
                }

                List<Camera.Size> supportedPicSize = mCamera.getParameters()
                        .getSupportedPictureSizes().stream() .sorted((s1, s2) -> Integer.compare(s2.height * s2.width, s1.height * s1.width)).collect(Collectors.toList());
                Camera.Size pictureSize = supportedPicSize.stream()
                        .filter(s -> (float) s.height / s.width - previewRatio < 0.01)
                        .findFirst()
                        .orElse(null);

                if(pictureSize == null){
                    pictureSize = supportedPicSize.get(0);
                }

                if(pictureSize == null){
                    AppLogger.e(TAG + ": Can not get picture size");
                }else{
                    param.setPictureSize(pictureSize.width, pictureSize.height);
                }
                PackageManager pm = mMainActivity.getPackageManager();
                if(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)){
                    param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    AppLogger.i(TAG + ": Enabling autofocus");
                }else {
                    AppLogger.i(TAG + ": Auto focus not available");
                }
                param.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);

                if(param.getMaxNumFocusAreas() > 0) mFocusAreaSupported = true;
                if(param.getMaxNumMeteringAreas() > 0) mMeteringAreaSupported = true;
                mCamera.setParameters(param);
                mCamera.setDisplayOrientation(90);
            }
        }
    }

    public void updateCam(){
        if (null == mCamera) {
            return ;
        }
        mCamera.stopPreview();
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException ie) {
            AppLogger.e(ie.getLocalizedMessage());
            return ;
        }
        mCamera.startPreview();
    }

    public void takePicture(){
        AppLogger.i(TAG + " : Try to focus");
        if(mCamera != null){
//            mCamera.autoFocus((bool, cam) -> {
//                AppLogger.i(TAG + " :Focus result " + bool);
//                mCamera.takePicture(null, null, mMainActivity);
//                new MediaActionSound().play(MediaActionSound.SHUTTER_CLICK);
//            });
            mCamera.takePicture(null, null, mMainActivity);
        }
    }

    private void releaseCam(){
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    public Camera getCamera() {
        return mCamera;
    }
}