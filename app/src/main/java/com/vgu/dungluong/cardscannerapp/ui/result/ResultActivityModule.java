package com.vgu.dungluong.cardscannerapp.ui.result;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.vgu.dungluong.cardscannerapp.data.AppDataManager;
import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.ui.result.adapter.EmailAdapter;
import com.vgu.dungluong.cardscannerapp.ui.result.adapter.PhoneAdapter;
import com.vgu.dungluong.cardscannerapp.ui.result.adapter.WebAdapter;
import com.vgu.dungluong.cardscannerapp.utils.AppConstants;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.inject.Named;

import androidx.recyclerview.widget.LinearLayoutManager;
import dagger.Module;
import dagger.Provides;


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

    @Provides
    EmailAdapter provideEmailAdapter(DataManager dataManager,
                                     SchedulerProvider schedulerProvider,
                                     ResultActivity resultActivity) {
        return new EmailAdapter(dataManager, schedulerProvider, resultActivity);
    }

    @Provides
    PhoneAdapter providePhoneAdapter(DataManager dataManager,
                                     SchedulerProvider schedulerProvider,
                                     ResultActivity resultActivity) {
        return new PhoneAdapter(dataManager, schedulerProvider, resultActivity);
    }

    @Provides
    WebAdapter provideWebAdapter(DataManager dataManager,
                                 SchedulerProvider schedulerProvider,
                                 ResultActivity resultActivity) {
        return new WebAdapter(dataManager, schedulerProvider, resultActivity);
    }

    @Provides
    LinearLayoutManager provideLinearLayoutManager(ResultActivity activity){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        return linearLayoutManager;
    }

}