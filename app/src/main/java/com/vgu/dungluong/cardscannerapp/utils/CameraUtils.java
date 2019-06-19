package com.vgu.dungluong.cardscannerapp.utils;

import android.hardware.Camera;

import java.util.Comparator;

/**
 * Created by Dung Luong on 19/06/2019
 */
public class CameraUtils {

    private CameraUtils(){

    }

    public static Camera.Size getMaxResolution(Camera.Parameters parameters){
        return parameters.getSupportedPreviewSizes().stream().max(Comparator.comparing(size -> size.width)).orElse(null);
    }
}
