package com.vgu.dungluong.cardscannerapp.ui.result;

import android.content.ContentResolver;
import android.view.View;
import android.widget.ImageView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.vgu.dungluong.cardscannerapp.data.model.local.Contact;
import com.vgu.dungluong.cardscannerapp.data.model.local.ContactField;

import java.io.File;
import java.util.List;

import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.tokenize.TokenizerModel;

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

    void animateButton(View view, int time);

    void addOnePhoneContactField();

    void addOneEmailContactField();

    void addOneWebContactField();

    List<ContactField> getEmailContactFields();

    List<ContactField> getPhoneContactFields();

    List<String> getWebs();

}
