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

    // Camera state: Showing camera preview.
    public static final int STATE_PREVIEW = 0;

    // Camera state: Waiting for the focus to be locked.
    public static final int STATE_WAITING_LOCK = 1;

    // Camera state: Waiting for the exposure to be precapture state.
    public static final int STATE_WAITING_PRECAPTURE = 2;

    // Camera state: Waiting for the exposure state to be something other than precapture.
    public static final int STATE_WAITING_NON_PRECAPTURE = 3;

    // Camera state: Picture was taken.
    public static final int STATE_PICTURE_TAKEN = 4;

    // Max preview width that is guaranteed by Camera2 API
    public static final int MAX_PREVIEW_WIDTH = 1920;

    // Max preview height that is guaranteed by Camera2 API
    public static final int MAX_PREVIEW_HEIGHT = 1080;

}
