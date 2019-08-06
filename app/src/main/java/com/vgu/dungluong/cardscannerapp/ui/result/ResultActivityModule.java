package com.vgu.dungluong.cardscannerapp.ui.result;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.vgu.dungluong.cardscannerapp.data.AppDataManager;
import com.vgu.dungluong.cardscannerapp.utils.AppConstants;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;

import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.LANGUAGE_MODEL;
import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.MODELDATA;
import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.MODEL_PATH;

/**
 * Created by Dung Luong on 02/07/2019
 */
@Module
public class ResultActivityModule {

    @Provides
    TessBaseAPI provideTessBaseAPI(AppDataManager appDataManager){
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        String locale = appDataManager.getLocale();
        tessBaseAPI.init(AppConstants.DATA_PATH, locale.equals("vi") ? "vie_best" : "eng_best", TessBaseAPI.OEM_LSTM_ONLY);
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
        return tessBaseAPI;
    }

}