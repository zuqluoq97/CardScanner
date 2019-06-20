package com.vgu.dungluong.cardscannerapp;

import com.vgu.dungluong.cardscannerapp.di.DataManager;
import com.vgu.dungluong.cardscannerapp.ui.crop.CropViewModel;
import com.vgu.dungluong.cardscannerapp.ui.main.MainViewModel;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Created by Dung Luong on 17/06/2019
 */
@Singleton
public class ViewModelProviderFactory extends ViewModelProvider.NewInstanceFactory {

    private final DataManager mDataManager;

    private final SchedulerProvider mSchedulerProvider;

    @Inject
    public ViewModelProviderFactory(DataManager dataManager,
                                    SchedulerProvider schedulerProvider) {
        mDataManager = dataManager;
        mSchedulerProvider = schedulerProvider;
    }

    @NonNull
    @SuppressWarnings({"unchecked"})
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass){
        if(modelClass.isAssignableFrom(MainViewModel.class)){
            return (T) new MainViewModel(mDataManager, mSchedulerProvider);
        } else if (modelClass.isAssignableFrom(CropViewModel.class)){
            return (T) new CropViewModel(mDataManager, mSchedulerProvider);
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}