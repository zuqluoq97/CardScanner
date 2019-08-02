package com.vgu.dungluong.cardscannerapp.data;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Pair;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.vgu.dungluong.cardscannerapp.data.local.locale.LocaleHelper;
import com.vgu.dungluong.cardscannerapp.data.local.permission.PermissionHelper;
import com.vgu.dungluong.cardscannerapp.data.local.preference.PreferenceHelper;
import com.vgu.dungluong.cardscannerapp.data.remote.ApiHelper;

import org.opencv.core.Point;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Dung Luong on 17/06/2019
 */
public interface DataManager extends PermissionHelper, PreferenceHelper, ApiHelper, LocaleHelper {

    Observable<Boolean> handleTakenPictureByte(byte[] bytes, Camera camera, List<Point> cropCoordinates);

    Observable<Pair<String, Float>> doTesseract(Bitmap bitmap, TessBaseAPI tessBaseAPI);


}