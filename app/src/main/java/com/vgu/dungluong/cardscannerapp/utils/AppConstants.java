package com.vgu.dungluong.cardscannerapp.utils;

import android.Manifest;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class AppConstants {

    // Id to identify permissions request
    public static final int CODE_PERMISSIONS_REQUEST = 0;

    public static final String[] PERMISSIONS = {Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

}
