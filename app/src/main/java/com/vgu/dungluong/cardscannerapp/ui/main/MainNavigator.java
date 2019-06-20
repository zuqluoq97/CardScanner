package com.vgu.dungluong.cardscannerapp.ui.main;

import android.view.Display;
import android.view.SurfaceView;

import com.vgu.dungluong.cardscannerapp.ui.view.PaperRectangle;

/**
 * Created by Dung Luong on 17/06/2019
 */
public interface MainNavigator {

    void exit();

    Display getDisplay();

    SurfaceView getSurfaceView();

    PaperRectangle getPaperRect();

    void onShutButtonClick();

    void openCropActivity();
}
