package com.vgu.dungluong.cardscannerapp.di.module;

import android.app.Application;
import android.content.Context;

import com.androidnetworking.gsonparserfactory.GsonParserFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vgu.dungluong.cardscannerapp.AutoValueGsonFactory;
import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.data.permission.AppPermissionHelper;
import com.vgu.dungluong.cardscannerapp.data.permission.PermissionHelper;
import com.vgu.dungluong.cardscannerapp.data.AppDataManager;
import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.data.preference.AppPreferenceHelper;
import com.vgu.dungluong.cardscannerapp.data.preference.PreferenceHelper;
import com.vgu.dungluong.cardscannerapp.data.remote.ApiHelper;
import com.vgu.dungluong.cardscannerapp.data.remote.AppApiHelper;
import com.vgu.dungluong.cardscannerapp.di.PreferenceInfo;
import com.vgu.dungluong.cardscannerapp.utils.AppConstants;
import com.vgu.dungluong.cardscannerapp.utils.rx.AppSchedulerProvider;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Dung Luong on 17/06/2019
 */
@Module
public class AppModule {

    @Provides
    @Singleton
    DataManager provideDataManager(AppDataManager appDataManager){
        return appDataManager;
    }

    @Provides
    @Singleton
    CalligraphyConfig provideCalligraphyDefaultConfig(){
        return new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build();
    }

    @Provides
    @Singleton
    Context provideContext(Application application) {
        return application;
    }

    @Provides
    SchedulerProvider provideSchedulerProvider(){
        return new AppSchedulerProvider();
    }

    @Provides
    @Singleton
    PermissionHelper providePermissionManager(AppPermissionHelper appPermissionManager){
        return appPermissionManager;
    }

    @Provides
    @Singleton
    PreferenceHelper providePreferenceHelper(AppPreferenceHelper appPreferenceHelper){
        return appPreferenceHelper;
    }

    @Provides
    @Singleton
    ApiHelper provideApiHelper(AppApiHelper appApiHelper){
        return appApiHelper;
    }

    @Provides
    @PreferenceInfo
    String providePreferenceName(){
        return AppConstants.PREF_NAME;
    }

    @Provides
    @Singleton
    Gson provideGson(){
        return new GsonBuilder().registerTypeAdapterFactory(AutoValueGsonFactory.create()).create();
    }

    @Provides
    @Singleton
    GsonParserFactory provideGsonParserFactory(Gson gson){
        return new GsonParserFactory(gson);
    }

}
