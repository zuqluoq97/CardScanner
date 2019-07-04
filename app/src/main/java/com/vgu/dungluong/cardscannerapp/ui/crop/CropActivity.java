package com.vgu.dungluong.cardscannerapp.ui.crop;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.ViewModelProviderFactory;
import com.vgu.dungluong.cardscannerapp.databinding.ActivityCropBinding;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseActivity;
import com.vgu.dungluong.cardscannerapp.ui.result.ResultActivity;
import com.vgu.dungluong.cardscannerapp.ui.view.PaperRectangle;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CameraUtils;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by Dung Luong on 19/06/2019
 */
public class CropActivity extends BaseActivity<ActivityCropBinding, CropViewModel>
        implements CropNavigator {

    @Inject
    ViewModelProviderFactory mViewModelProviderFactory;

    private CropViewModel mCropViewModel;

    private ActivityCropBinding mCropBinding;

    public static final String TAG = CropActivity.class.getSimpleName();

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_crop;
    }

    @Override
    public CropViewModel getViewModel() {
        mCropViewModel = ViewModelProviders.of(this, mViewModelProviderFactory).get(CropViewModel.class);
        return mCropViewModel;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, CropActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCropBinding = getViewDataBinding();
        mCropViewModel.setNavigator(this);
        mCropBinding.setViewModel(mCropViewModel);

        setUp();
    }

    private void setUp() {
        mCropViewModel.prepareImage();
    }


    @Override
    public ImageView getCroppedPaper() {
        return mCropBinding.pictureCropped;
    }

    @Override
    public int getTopOffSet() {
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels - mCropBinding.rootLayout.getMeasuredHeight();
    }

    @Override
    public PaperRectangle getPaperRect() {
        return mCropBinding.paperRect;
    }

    @Override
    public void openResultActivity() {
        finish();
        startActivity(ResultActivity.newIntent(this));
    }
}
