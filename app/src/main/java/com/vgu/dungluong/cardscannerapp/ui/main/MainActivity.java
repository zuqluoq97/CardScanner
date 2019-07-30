package com.vgu.dungluong.cardscannerapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.os.Bundle;

import android.os.Handler;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.ViewModelProviderFactory;
import com.vgu.dungluong.cardscannerapp.databinding.ActivityMainBinding;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseActivity;
import com.vgu.dungluong.cardscannerapp.ui.crop.CropActivity;
import com.vgu.dungluong.cardscannerapp.ui.view.CameraPreview;
import com.vgu.dungluong.cardscannerapp.ui.view.DrawingView;
import com.vgu.dungluong.cardscannerapp.ui.view.PaperRectangle;
import com.vgu.dungluong.cardscannerapp.ui.view.PreviewSurfaceView;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CameraUtils;
import com.vgu.dungluong.cardscannerapp.utils.CommonUtils;
import com.vgu.dungluong.cardscannerapp.utils.PermissionUtils;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.content.ContextCompat;
import androidx.core.view.LayoutInflaterCompat;
import androidx.lifecycle.ViewModelProviders;

import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.CODE_PERMISSIONS_REQUEST;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel>
        implements MainNavigator, Camera.PictureCallback {

    @Inject
    ViewModelProviderFactory mViewModelProviderFactory;

    private MainViewModel mMainViewModel;

    private ActivityMainBinding mMainBinding;

    public static final String TAG = MainActivity.class.getSimpleName();

    @Inject
    CameraPreview mCameraPreview;

    private PreviewSurfaceView mPreviewSurfaceView;

    private DrawingView mDrawingView;

    private static long mBackPressResponseTime;

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

        setUp();
    }

    private void setUp() {
        if (!OpenCVLoader.initDebug()) {
            AppLogger.i(TAG + ": loading opencv error, exit");
            exit();
        }

        mPreviewSurfaceView = getSurfaceView();
        mPreviewSurfaceView.getHolder().addCallback(mCameraPreview);
        mPreviewSurfaceView.setListener(mCameraPreview);
        mDrawingView = getFocusView();
        mPreviewSurfaceView.setDrawingView(mDrawingView);

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
    public PreviewSurfaceView getSurfaceView() {
        return mMainBinding.surface;
    }

    @Override
    public DrawingView getFocusView() {
        return mMainBinding.focusView;
    }

    @Override
    public View getCroppedView() {
        return mMainBinding.croppedSurface;
    }

    @Override
    public void onShutButtonClick() {
        mCameraPreview.takePicture();
    }

    @Override
    public void openCropActivity() {
        startActivity(CropActivity.newIntent(MainActivity.this));
    }

    /*
    Get height of status bar/ notification bar
     */
    @Override
    public int getTopOffSet() {
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels - getSurfaceView().getMeasuredHeight();
    }

    @Override
    public void changeLocaleIcon(String locale) {
        if(locale.equals("vi")){
            mMainBinding.languageOCR
                    .setImageDrawable(ContextCompat.getDrawable(this, R.drawable.vietnam_flag));
        }else{
            mMainBinding.languageOCR
                    .setImageDrawable(ContextCompat.getDrawable(this, R.drawable.usa_flag));
        }
    }

    @Override
    public void updateLocale(String locale) {
        getViewModel().getDataManager().setNewLocale(this, locale);
        showMessage(locale);
        restart();
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        AppLogger.i(TAG + ": On picture taken");
        mMainViewModel.handlePictureTaken(bytes, camera);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCameraPreview.getCamera() != null) {
            mCameraPreview.getCamera().startPreview();
        } else {
            AppLogger.i(TAG + ": camera null");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraPreview.getCamera() != null) {
            mCameraPreview.getCamera().stopPreview();
        } else {
            AppLogger.i(TAG + ": camera null");
        }
    }

    @Override
    public void onBackPressed() {
        if(mBackPressResponseTime + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
            finish();
        }else{
            CommonUtils.showQuickToast(this, getString(R.string.double_to_exit));
        }
        mBackPressResponseTime = System.currentTimeMillis();
    }
}