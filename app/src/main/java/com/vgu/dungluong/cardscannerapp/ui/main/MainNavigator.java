package com.vgu.dungluong.cardscannerapp.ui.main;

import android.view.Display;
import android.view.SurfaceView;
import android.view.View;

import com.vgu.dungluong.cardscannerapp.ui.view.DrawingView;
import com.vgu.dungluong.cardscannerapp.ui.view.PaperRectangle;
import com.vgu.dungluong.cardscannerapp.ui.view.PreviewSurfaceView;

/**
 * Created by Dung Luong on 17/06/2019
 */
public interface MainNavigator {

    void exit();

    Display getDisplay();

    PreviewSurfaceView getSurfaceView();

    DrawingView getFocusView();

    View getCroppedView();

    void onShutButtonClick();

    void openCropActivity();

}
