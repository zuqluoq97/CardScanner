package com.vgu.dungluong.cardscannerapp.di.module;

import android.app.Application;
import android.content.Context;

import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.data.permission.AppPermissionHelper;
import com.vgu.dungluong.cardscannerapp.data.permission.PermissionHelper;
import com.vgu.dungluong.cardscannerapp.di.AppDataManager;
import com.vgu.dungluong.cardscannerapp.di.DataManager;
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
}
