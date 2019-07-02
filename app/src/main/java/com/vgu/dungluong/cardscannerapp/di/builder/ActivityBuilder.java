package com.vgu.dungluong.cardscannerapp.di.builder;

import com.vgu.dungluong.cardscannerapp.ui.crop.CropActivity;
import com.vgu.dungluong.cardscannerapp.ui.main.MainActivity;
import com.vgu.dungluong.cardscannerapp.ui.main.MainActivityModule;
import com.vgu.dungluong.cardscannerapp.ui.result.ResultActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by Dung Luong on 17/06/2019
 */
@Module
public abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = {MainActivityModule.class})
    abstract MainActivity bindMainActivity();

    @ContributesAndroidInjector
    abstract CropActivity bindCropActivity();


    @ContributesAndroidInjector
    abstract ResultActivity bindResultActivity();
}
