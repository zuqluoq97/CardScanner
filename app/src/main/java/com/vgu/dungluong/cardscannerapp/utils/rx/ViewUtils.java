package com.vgu.dungluong.cardscannerapp.utils.rx;

import android.content.res.Resources;

/**
 * Created by Dung Luong on 04/07/2019
 */
public final class ViewUtils {

    private ViewUtils() {
        // This class is not publicly instantiable
    }


    public static int dpToPx(float dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static float pxToDp(float px) {
        float densityDpi = Resources.getSystem().getDisplayMetrics().densityDpi;
        return px / (densityDpi / 160f);
    }
}