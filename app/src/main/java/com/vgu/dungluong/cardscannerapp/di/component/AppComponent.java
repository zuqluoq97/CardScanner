package com.vgu.dungluong.cardscannerapp.di.component;

import android.app.Application;

import com.vgu.dungluong.cardscannerapp.CardScannerApp;
import com.vgu.dungluong.cardscannerapp.di.builder.ActivityBuilder;
import com.vgu.dungluong.cardscannerapp.di.module.AppModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

/**
 * Created by Dung Luong on 17/06/2019
 */
@Singleton
@Component(modules = {AndroidInjectionModule.class, AppModule.class, ActivityBuilder.class})
public interface AppComponent {

    void inject(CardScannerApp app);

    @Component.Builder
    interface Builder{

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }
}