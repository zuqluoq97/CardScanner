package com.vgu.dungluong.cardscannerapp.ui.result;

import android.content.ContentResolver;
import android.widget.ImageView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

import opennlp.tools.langdetect.LanguageDetector;

/**
 * Created by Dung Luong on 02/07/2019
 */
public interface ResultNavigator {

    ImageView getCardImageView();

    void handleError(String error);

    void showMessage(String mess);

    TessBaseAPI getTesseractApi();

    File getFileForCropImage();

    ContentResolver getContentResolver();
}
