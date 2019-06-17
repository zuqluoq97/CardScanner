package com.vgu.dungluong.cardscannerapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Html;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class CommonUtils {

    private CommonUtils() {
        // This utility class is not publicly instantiable
    }

    public static AlertDialog.Builder dialogConfiguration(Context context,
                                                          String title,
                                                          @Nullable String message,
                                                          boolean cancelable){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        if(message != null) builder.setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
        builder.setCancelable(cancelable);
        return builder;
    }

    public static void showQuickToast(Context context, String mess){
        Toast.makeText(context, mess, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String mess){
        Toast.makeText(context, mess, Toast.LENGTH_LONG).show();
    }
}
