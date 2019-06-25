package com.vgu.dungluong.cardscannerapp.ui.main;

import com.vgu.dungluong.cardscannerapp.ui.view.CameraPreview;
import com.vgu.dungluong.cardscannerapp.ui.view.DrawingView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Dung Luong on 19/06/2019
 */
@Module
public class MainActivityModule {

    @Provides
    CameraPreview provideCameraPreview(MainActivity mainActivity){
        return new CameraPreview(mainActivity);
    }
}
