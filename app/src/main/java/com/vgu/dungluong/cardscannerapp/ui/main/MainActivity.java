package com.vgu.dungluong.cardscannerapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.os.Bundle;

import android.text.format.DateUtils;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.ViewModelProviderFactory;
import com.vgu.dungluong.cardscannerapp.databinding.ActivityMainBinding;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseActivity;
import com.vgu.dungluong.cardscannerapp.ui.crop.CropActivity;
import com.vgu.dungluong.cardscannerapp.ui.view.PaperRectangle;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CameraUtils;
import com.vgu.dungluong.cardscannerapp.utils.CommonUtils;
import com.vgu.dungluong.cardscannerapp.utils.PermissionUtils;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.lifecycle.ViewModelProviders;

import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.CODE_PERMISSIONS_REQUEST;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel>
        implements MainNavigator, SurfaceHolder.Callback, Camera.PictureCallback, Camera.PreviewCallback {

    @Inject
    ViewModelProviderFactory mViewModelProviderFactory;

    private MainViewModel mMainViewModel;

    private ActivityMainBinding mMainBinding;

    public static final String TAG = MainActivity.class.getSimpleName();

    private Camera mCamera;

    private SurfaceHolder mSurfaceHolder;

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public MainViewModel getViewModel() {
        mMainViewModel = ViewModelProviders.of(this, mViewModelProviderFactory).get(MainViewModel.class);
        return mMainViewModel;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainBinding = getViewDataBinding();
        mMainViewModel.setNavigator(this);
        mMainBinding.setViewModel(mMainViewModel);
        checkPermission();

        setUp();
    }

    private void setUp() {
        if (!OpenCVLoader.initDebug()) {
            AppLogger.i(TAG + ": loading opencv error, exit");
            exit();
        }
        mSurfaceHolder = getSurfaceView().getHolder();
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    public Display getDisplay() {
        return getWindowManager().getDefaultDisplay();
    }

    @Override
    public SurfaceView getSurfaceView() {
        return mMainBinding.surface;
    }

    @Override
    public PaperRectangle getPaperRect() {
        return mMainBinding.paperRect;
    }

    @Override
    public void onShutButtonClick() {
        AppLogger.i(TAG + " : Try to focus");
        if(mCamera != null){
            mCamera.autoFocus((bool, cam) -> {
                AppLogger.i(TAG + " :Focus result " + bool);
                mCamera.takePicture(null, null, this);
                //new MediaActionSound().play(MediaActionSound.SHUTTER_CLICK);
            });
        }
    }

    @Override
    public void openCropActivity() {
        startActivity(CropActivity.newIntent(MainActivity.this));
        finish();
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        AppLogger.i(TAG + ": On picture taken");
        mMainViewModel.handlePictureTaken(bytes, camera);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if(mMainViewModel.getIsLoading().get()){
            return ;
        }
        AppLogger.i(TAG + ": On process start");
        mMainViewModel.handlePreviewFrame(bytes, camera);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        initCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        updateCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        synchronized(this) {
            if(mCamera != null){
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCamera != null) {
            mCamera.startPreview();
        } else {
            AppLogger.i(TAG + ": camera null");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCamera != null) {
            mCamera.stopPreview();
        } else {
            AppLogger.i(TAG + ": camera null");
        }
    }

    /**
     * Callback received when a permissions request has been completed
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == CODE_PERMISSIONS_REQUEST){
            if(PermissionUtils.verifyPermissions(grantResults)){
                showMessage(getString(R.string.camera_grant));
                initCamera();
                updateCamera();
            } else {
                CommonUtils.dialogConfiguration(this,
                        getString(R.string.request_permissions_title),
                        getString(R.string.permission_not_grant_message),
                        false)
                        .setPositiveButton(android.R.string.yes, ((dialog, which) -> restart())).show();
            }
        }
    }

    private void initCamera(){
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
                getDisplay().getRealSize(point);

                // Higher value of height and smaller value for width
                int displayWidth = point.x < point.y ? point.x : point.y;
                int displayHeight = point.x > point.y ? point.x : point.y;

                float displayRatio = (float) displayWidth / displayHeight;
                float previewRatio = size != null ? (float) size.height / size.width : displayRatio;

                if(displayRatio > previewRatio){
                    ViewGroup.LayoutParams surfaceParams = getSurfaceView().getLayoutParams();
                    surfaceParams.height = (int) (displayHeight / displayRatio * previewRatio);
                    getSurfaceView().setLayoutParams(surfaceParams);
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
                PackageManager pm = getPackageManager();
                if(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)){
                    param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    AppLogger.i(TAG + ": Enabling autofocus");
                }else {
                    AppLogger.i(TAG + ": Auto focus not available");
                }
                param.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                mCamera.setParameters(param);
                mCamera.setDisplayOrientation(90);
            }
        }
    }

    private void updateCamera(){
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
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();
    }
}
