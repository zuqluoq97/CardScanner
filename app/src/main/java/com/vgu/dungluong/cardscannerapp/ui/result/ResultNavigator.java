package com.vgu.dungluong.cardscannerapp.ui.result;

import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Created by Dung Luong on 02/07/2019
 */
public interface ResultNavigator {

    ImageView getCardImageView();

    void handleError(String error);

    void showMessage(String mess);

    TessBaseAPI getTesseractApi();

    void prepareTesseract();
}
