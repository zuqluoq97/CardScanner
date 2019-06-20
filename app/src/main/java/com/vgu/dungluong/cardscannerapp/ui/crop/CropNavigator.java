package com.vgu.dungluong.cardscannerapp.ui.crop;

import android.view.Display;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.vgu.dungluong.cardscannerapp.ui.view.PaperRectangle;

/**
 * Created by Dung Luong on 19/06/2019
 */
public interface CropNavigator {

    ImageView getCard();

    ImageView getCroppedPaper();

    PaperRectangle getPaperRect();

    void onCropButtonClick();

    void onEnhanceButtonClick();
}
