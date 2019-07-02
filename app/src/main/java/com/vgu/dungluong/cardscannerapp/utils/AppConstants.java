package com.vgu.dungluong.cardscannerapp.utils;

import android.Manifest;
import android.util.SparseIntArray;
import android.view.Surface;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class AppConstants {

    // Id to identify permissions request
    public static final int CODE_PERMISSIONS_REQUEST = 0;

    public static final String[] PERMISSIONS = {Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    public static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public static final double CANNY_SIGMA = 0.33;

    public static final String PREF_NAME = "CARD_SCANNER";

}
