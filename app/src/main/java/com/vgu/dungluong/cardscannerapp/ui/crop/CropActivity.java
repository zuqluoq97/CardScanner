package com.vgu.dungluong.cardscannerapp.ui.crop;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.ViewModelProviderFactory;
import com.vgu.dungluong.cardscannerapp.databinding.ActivityCropBinding;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseActivity;
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
//        ViewTreeObserver vto =  mCropBinding.card.getViewTreeObserver();
//        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            public boolean onPreDraw() {
//                mCropBinding.card.getViewTreeObserver().removeOnPreDrawListener(this);
//                AppLogger.i("Height: " + mCropBinding.card.getMeasuredHeight() + " Width: " + mCropBinding.card.getMeasuredWidth());
//                return true;
//            }
//        });
        mCropViewModel.prepareImage();
//        File imgFile = new File(Environment.getExternalStorageDirectory() + "/capture/img.jpg");
//
//        if(imgFile.exists()){
//            try {
//                ExifInterface ei = new ExifInterface(imgFile.getAbsolutePath());
//                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                        ExifInterface.ORIENTATION_UNDEFINED);
//                Bitmap bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                Bitmap rotatedBitmap;
//                switch(orientation) {
//
//                    case ExifInterface.ORIENTATION_ROTATE_90:
//                        rotatedBitmap = CameraUtils.rotateImage(bm, 90);
//                        break;
//
//                    case ExifInterface.ORIENTATION_ROTATE_180:
//                        rotatedBitmap = CameraUtils.rotateImage(bm, 180);
//                        break;
//
//                    case ExifInterface.ORIENTATION_ROTATE_270:
//                        rotatedBitmap = CameraUtils.rotateImage(bm, 270);
//                        break;
//
//                    case ExifInterface.ORIENTATION_NORMAL:
//                    default:
//                        rotatedBitmap = bm;
//                }
//                AppLogger.i("bitmap: " + bm.getHeight() + " " + bm.getWidth());
//                AppLogger.i(getIntent().getIntExtra("left", 0) + " " + getIntent().getIntExtra("top", 0) + " " + getIntent().getIntExtra("width", 0) + " " + getIntent().getIntExtra("height", 0) );
//                AppLogger.i(getIntent().getIntExtra("previewWidth", 0) + " " + getIntent().getIntExtra("previewHeight", 0));
//                int previewHeight = getIntent().getIntExtra("previewHeight", 0);
//                int previewWidth = getIntent().getIntExtra("previewWidth", 0);
//                int cropHeight = getIntent().getIntExtra("height", 0);
//                int cropWidth = getIntent().getIntExtra("width", 0);
////                int x = getIntent().getIntExtra("left", 0) * rotatedBitmap.getWidth() / getIntent().getIntExtra("previewHeight", 0);
////                int y = getIntent().getIntExtra("top", 0) * rotatedBitmap.getHeight() / getIntent().getIntExtra("previewWidth", 0);
////                int width = getIntent().getIntExtra("width", 0) * rotatedBitmap.getWidth() / getIntent().getIntExtra("previewHeight", 0);
////                int height = getIntent().getIntExtra("height", 0) * rotatedBitmap.getHeight() / getIntent().getIntExtra("previewWidth", 0);
//                int x = (int) (previewHeight * 0.15  * rotatedBitmap.getHeight() / previewHeight);
//                int y = (int) (previewWidth * 0.15 * rotatedBitmap.getWidth() / previewWidth);
//                int width = (int) (previewWidth * 0.7 * rotatedBitmap.getWidth() / previewWidth);
//                int height = (int) (previewHeight * 0.7 * rotatedBitmap.getHeight() / previewHeight);
//
//                AppLogger.i(x + " " + y + " " + width + " " + height);
//                mCropBinding.card.setImageBitmap(Bitmap.createBitmap(rotatedBitmap,
//                        x,
//                        y,
//                        width ,
//                        height));
//
//            } catch (IOException e) {
//                AppLogger.e(e.getLocalizedMessage());
//            }
//
//        }else{
//            AppLogger.e("File not found!");
//        }
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
