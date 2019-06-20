package com.vgu.dungluong.cardscannerapp.ui.crop;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.ViewModelProviderFactory;
import com.vgu.dungluong.cardscannerapp.databinding.ActivityCropBinding;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseActivity;
import com.vgu.dungluong.cardscannerapp.ui.view.PaperRectangle;

import javax.inject.Inject;

import androidx.annotation.Nullable;
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
    public ImageView getCard() {
        return mCropBinding.card;
    }

    @Override
    public ImageView getCroppedPaper() {
        return mCropBinding.pictureCropped;
    }

    @Override
    public PaperRectangle getPaperRect() {
        return mCropBinding.paperRect;
    }

    @Override
    public void onCropButtonClick() {

    }

    @Override
    public void onEnhanceButtonClick() {

    }
}
