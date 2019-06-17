package com.vgu.dungluong.cardscannerapp.ui.main;

import com.vgu.dungluong.cardscannerapp.di.DataManager;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class MainViewModel extends BaseViewModel<MainNavigator> {

    public MainViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }
}
