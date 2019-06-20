package com.vgu.dungluong.cardscannerapp.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.ViewGroup;

import com.vgu.dungluong.cardscannerapp.ui.base.BaseActivity;
import com.vgu.dungluong.cardscannerapp.ui.main.MainActivity;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;

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
