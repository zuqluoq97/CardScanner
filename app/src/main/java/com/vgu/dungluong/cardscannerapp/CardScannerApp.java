package com.vgu.dungluong.cardscannerapp;

import android.app.Activity;
import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.gsonparserfactory.GsonParserFactory;
import com.androidnetworking.interceptors.HttpLoggingInterceptor;
import com.vgu.dungluong.cardscannerapp.di.component.DaggerAppComponent;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import okhttp3.OkHttpClient;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class CardScannerApp extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> mActivityDispatchingAndroidInjector;

    @Inject
    CalligraphyConfig mCalligraphyConfig;

    @Inject
    GsonParserFactory mGsonParserFactory;

    @Override
    public void onCreate() {
        super.onCreate();

        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this);

        AppLogger.init();

        AndroidNetworking.initialize(getApplicationContext());

        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.BASIC);

        CalligraphyConfig.initDefault(mCalligraphyConfig);

        AndroidNetworking.setParserFactory(mGsonParserFactory);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return mActivityDispatchingAndroidInjector;
    }
}
