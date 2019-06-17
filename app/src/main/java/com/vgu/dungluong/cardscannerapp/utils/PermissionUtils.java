package com.vgu.dungluong.cardscannerapp.utils;

import android.content.pm.PackageManager;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class PermissionUtils {

    private PermissionUtils() {
    }


    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
