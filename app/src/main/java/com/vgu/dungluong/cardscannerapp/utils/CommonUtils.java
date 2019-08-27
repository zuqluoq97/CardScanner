package com.vgu.dungluong.cardscannerapp.utils;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Animatable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.florent37.viewanimator.ViewAnimator;
import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;
import com.vgu.dungluong.cardscannerapp.data.model.local.OnTouchZone;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

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

    public static void handleImageAnimated(final ImageView view){
        final Animatable front = (Animatable) view.getDrawable();
        front.start();
    }

    public static void rotate(View view, int time){
        if(time % 2 == 0){
            ViewAnimator.animate(view)
                    .rotation(0, 180)
                    .duration(300)
                    .decelerate()
                    .start();
        }else{
            ViewAnimator.animate(view)
                    .rotation(180, 360)
                    .duration(300)
                    .decelerate()
                    .start();
        }

    }


    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     */
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
        /* // If you have StringUtils, you can use it to calculate the edit distance:
        return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
                                                             (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://r...content-available-to-author-only...e.org/wiki/Levenshtein_distance#Java
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    public static Rect getRect(List<Point> pointList){
        Point tl = pointList.stream().min(Comparator.comparing(point -> point.x + point.y)).orElse(new Point());
        //Point tr = pointList.stream().max(Comparator.comparing(point -> point.x - point.y)).orElse(new Point());
        Point br = pointList.stream().max(Comparator.comparing(point -> point.x + point.y)).orElse(new Point());
        //Point bl = pointList.stream().min(Comparator.comparing(point -> point.x - point.y)).orElse(new Point());

        return new Rect(tl, br);
    }

    public static OnTouchZone getOnTouchZone(Corners corners){
        List<Point> points = corners.getCorners();
        return new OnTouchZone(points.get(0).x, points.get(0).y, points.get(2).x, points.get(2).y);
    }

    public static int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath){
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            AppLogger.i("RotateImage" + " Exif orientation: " + orientation);
            AppLogger.i("RotateImage" + " Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static boolean isNullOrEmpty(String str){
        if(str != null && !str.isEmpty() && !str.equals("null") ) return false;
        return true;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static int getMaxContactPhotoSize(final Context context) {
        // Note that this URI is safe to call on the UI thread.
        final Uri uri = ContactsContract.DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI;
        final String[] projection = new String[] { ContactsContract.DisplayPhoto.DISPLAY_MAX_DIM };
        final Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
        try {
            c.moveToFirst();
            return c.getInt(0);
        } finally {
            c.close();
        }
        // fallback: 96x96 is the max contact photo size for pre-ICS versions
    }
}
