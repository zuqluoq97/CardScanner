package com.vgu.dungluong.cardscannerapp.di.builder;

import com.vgu.dungluong.cardscannerapp.ui.main.MainActivity;
import com.vgu.dungluong.cardscannerapp.ui.main.MainActivityModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by Dung Luong on 17/06/2019
 */
@Module
public abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = {MainActivityModule.class})
    abstract MainActivity bindMainActivity();
}
