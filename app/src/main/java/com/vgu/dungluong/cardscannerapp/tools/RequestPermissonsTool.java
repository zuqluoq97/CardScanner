package com.vgu.dungluong.cardscannerapp.tools;

import android.app.Activity;
import android.content.Context;

/**
 * Created by Dung Luong on 6/5/2019
 */
public interface RequestPermissonsTool {

    void requestPermissions(Activity context, String[] permissions);

    boolean isPermissionsGranted(Context context, String[] permissions);

    void onPermissionDenied();
}
