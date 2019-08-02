package com.vgu.dungluong.cardscannerapp.ui.result;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.ViewModelProviderFactory;
import com.vgu.dungluong.cardscannerapp.databinding.ActivityResultBinding;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseActivity;
import com.vgu.dungluong.cardscannerapp.ui.main.MainActivity;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by Dung Luong on 02/07/2019
 */
public class ResultActivity extends BaseActivity<ActivityResultBinding, ResultViewModel>
        implements ResultNavigator{

    @Inject
    ViewModelProviderFactory mViewModelProviderFactory;

    private ResultViewModel mResultViewModel;

    private ActivityResultBinding mResultBinding;

    public static final String TAG = MainActivity.class.getSimpleName();

    @Named("lstm")
    @Inject
    TessBaseAPI mTessBaseAPI;

    @Named("legacy")
    @Inject
    TessBaseAPI mTessBaseAPI2;

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_result;
    }


    @Override
    public ResultViewModel getViewModel() {
        mResultViewModel = ViewModelProviders.of(this, mViewModelProviderFactory).get(ResultViewModel.class);
        return mResultViewModel;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, ResultActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);
        mResultBinding = getViewDataBinding();
        mResultViewModel.setNavigator(this);
        mResultBinding.setViewModel(mResultViewModel);
        checkPermission();

        setUp();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mResultViewModel.displayCardImage();
    }

    private void setUp() {

    }

    @Override
    public ImageView getCardImageView() {
        return mResultBinding.cardImageView;
    }

    @Override
    public void handleError(String error) {
        super.handleError(error);
    }

    @Override
    public void showMessage(String message) {
        super.showMessage(message);
    }

    @Override
    public TessBaseAPI getTesseractApi() {
        return mTessBaseAPI;
    }

    @Override
    public TessBaseAPI getTesseractApi2() {
        return mTessBaseAPI2;
    }

    @Override
    public File getFileForCropImage() {
        return new File(Environment.getExternalStorageDirectory(), "crop.jpg");
    }

}
