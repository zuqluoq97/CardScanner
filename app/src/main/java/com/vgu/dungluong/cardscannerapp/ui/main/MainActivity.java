package com.vgu.dungluong.cardscannerapp.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

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
import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.GALLERY_REQUEST_CODE;
import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.IS_SELECTED_CARD;

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
    public void openCropActivity(boolean isSelectedCard) {
        Intent intent = CropActivity.newIntent(MainActivity.this);
        intent.putExtra(IS_SELECTED_CARD, isSelectedCard);
        startActivity(intent);
    }

    /*
    Get height of status bar/ notification bar
     */
    @Override
    public int getTopOffSet() {
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

        return rectangle.top;
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
    public void openGallery() {
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    //data.getData return the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    // Get the cursor
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();
                    //Get the column index of MediaStore.Images.Media.DATA
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    //Gets the String value in the column
                    String imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();
                    // Set the Image in ImageView after decoding the String
                    mMainViewModel.handlePictureChosen(BitmapFactory.decodeFile(imgDecodableString),
                            CommonUtils.getCameraPhotoOrientation(this, selectedImage, imgDecodableString));
                    break;
            }
    }
}