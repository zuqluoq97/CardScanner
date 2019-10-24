package com.vgu.dungluong.cardscannerapp.utils;

import android.Manifest;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.SparseIntArray;
import android.view.Surface;

import java.util.Arrays;
import java.util.List;
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

    public static final double EPISILON = 1E-5;

    public static final int GALLERY_REQUEST_CODE = 1;

    public static final String IS_SELECTED_CARD = "IS_SELECTED_CARD";

    public static final String GROUP_PHONE_TITLE = "CardScanner";
    // REGEX
//    public static final Pattern EMAIL_ADDDRESS_PATTERN = Pattern.compile("[a-zA-Z0-9.\\-_(]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z0-9]+");
    public static final Pattern EMAIL_ADDDRESS_PATTERN = Pattern.compile("[a-zA-Z0-9.\\-_(]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z0-9]{2,}");
    public static final Pattern INTENT_EMAIL_ADDDRESS_PATTERN = Pattern.compile(".+@.+\\..+");
//    public static final Pattern WEB_ADDRESS_PATTERN = Pattern.compile("(?:[a-zA-Z0-9]+?\\.)+[a-zA-Z0-9]{2,}");
    public static final Pattern WEB_ADDRESS_PATTERN = Pattern.compile("[a-zA-Z]+\\.([a-zA-Z0-9-]+\\.)+[a-zA-Z0-9]{2,}");
    public static final Pattern ORDINAL_NUMBER_PATTERN = Pattern.compile("([1-9]?1)st|([1-9]?2)nd|([1-9]?3)rd|(([1-9]?([456789])|[1-9]+0)th)");

    public static final List<String> DATA_TYPE1_TYPE_TITLE = Arrays.asList("Home", "Work", "Other");
    public static final List<Integer> DATE_TYPE1_ADDRESS_TYPE = Arrays.asList(ContactsContract.CommonDataKinds.SipAddress.TYPE_HOME, ContactsContract.CommonDataKinds.SipAddress.TYPE_WORK, ContactsContract.CommonDataKinds.SipAddress.TYPE_OTHER, ContactsContract.CommonDataKinds.SipAddress.TYPE_CUSTOM);
    public static final List<String> DATA_TYPE2_TYPE_TITLE = Arrays.asList("Mobile", "Home", "Work", "Work Fax", "Home Fax", "Pager", "Other", "Callback");
}
