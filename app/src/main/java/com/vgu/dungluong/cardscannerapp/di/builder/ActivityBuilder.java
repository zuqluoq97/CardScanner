package com.vgu.dungluong.cardscannerapp.di.builder;

import com.vgu.dungluong.cardscannerapp.ui.crop.CropActivity;
import com.vgu.dungluong.cardscannerapp.ui.main.MainActivity;
import com.vgu.dungluong.cardscannerapp.ui.main.MainActivityModule;
import com.vgu.dungluong.cardscannerapp.ui.result.ResultActivity;
import com.vgu.dungluong.cardscannerapp.ui.result.ResultActivityModule;
import com.vgu.dungluong.cardscannerapp.ui.splash.SplashActivity;
import com.vgu.dungluong.cardscannerapp.ui.splash.SplashActivityModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by Dung Luong on 17/06/2019
 */
@Module
public abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = {SplashActivityModule.class})
    abstract SplashActivity bindSplashctivity();

    @ContributesAndroidInjector(modules = {MainActivityModule.class})
    abstract MainActivity bindMainActivity();

    @ContributesAndroidInjector
    abstract CropActivity bindCropActivity();

    @ContributesAndroidInjector(modules = {ResultActivityModule.class})
    abstract ResultActivity bindResultActivity();
}
