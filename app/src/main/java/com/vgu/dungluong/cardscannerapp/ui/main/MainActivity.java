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

import com.mikepenz.iconics.IconicsColor;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.IconicsSize;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
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
        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
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
    public void changeColorIcon(boolean isBlackColor) {
        if(isBlackColor){
            mMainBinding.blackCard.setImageDrawable(new IconicsDrawable(this)
                    .icon(FontAwesome.Icon.faw_address_card1)
                    .color(IconicsColor.colorInt(getColor(R.color.black)))
                    .size(IconicsSize.dp(32)));
            mMainBinding.blackCardBackground.setVisibility(View.VISIBLE);
        }else{
            mMainBinding.blackCard.setImageDrawable(new IconicsDrawable(this)
                    .icon(FontAwesome.Icon.faw_address_card1)
                    .color(IconicsColor.colorInt(getColor(R.color.white)))
                    .size(IconicsSize.dp(32)));
            mMainBinding.blackCardBackground.setVisibility(View.GONE);
        }
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
                mCameraPreview.initCam();
                mCameraPreview.updateCam();
            } else {
                CommonUtils.dialogConfiguration(this,
                        getString(R.string.request_permissions_title),
                        getString(R.string.permission_not_grant_message),
                        false)
                        .setPositiveButton(android.R.string.yes, ((dialog, which) -> restart())).show();
            }
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