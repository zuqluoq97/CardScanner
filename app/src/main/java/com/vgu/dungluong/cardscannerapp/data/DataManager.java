package com.vgu.dungluong.cardscannerapp.data;

import android.graphics.Bitmap;
import android.hardware.Camera;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.vgu.dungluong.cardscannerapp.data.permission.PermissionHelper;
import com.vgu.dungluong.cardscannerapp.data.preference.PreferenceHelper;

import org.opencv.core.Point;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Dung Luong on 17/06/2019
 */
public interface DataManager extends PermissionHelper, PreferenceHelper {

    Observable<Boolean> handleTakenPictureByte(byte[] bytes, Camera camera, List<Point> cropCoordinates);

    Observable<String> doTesseract(List<Bitmap> bitmap, TessBaseAPI tessBaseAPI);
}