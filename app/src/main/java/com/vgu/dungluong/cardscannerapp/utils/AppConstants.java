package com.vgu.dungluong.cardscannerapp.utils;

import android.Manifest;
import android.os.Environment;
import android.util.SparseIntArray;
import android.view.Surface;

import java.util.regex.Pattern;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class AppConstants {

    // Id to identify permissions request
    public static final int CODE_PERMISSIONS_REQUEST = 0;

    public static final String[] PERMISSIONS = {Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CONTACTS};

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

    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractData/";
    public static final String TESSDATA = "tessdata";
    public static final String MODEL_PATH = Environment.getExternalStorageDirectory().toString() + "/OpenNLP/";
    public static final String MODELDATA = "model";
    public static final String LANGUAGE_MODEL = "langdetect-183.bin";
    public static final double EPISILON = 1E-8;

    public static final int GALLERY_REQUEST_CODE = 1;

    public static final String IS_SELECTED_CARD = "IS_SELECTED_CARD";

    // REGEX
    public static final Pattern EMAIL_ADDDRESS_PATTERN = Pattern.compile("([a-zA-Z0-9.]+\\(?+@[a-zA-Z0-9.]+\\.[a-zA-Z0-9]+)");
    public static final Pattern INTENT_EMAIL_ADDDRESS_PATTERN = Pattern.compile(".+@.+\\..+");

}
