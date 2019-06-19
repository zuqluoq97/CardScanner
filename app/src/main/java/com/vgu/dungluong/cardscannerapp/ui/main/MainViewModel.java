package com.vgu.dungluong.cardscannerapp.ui.main;

import android.hardware.Camera;

import com.vgu.dungluong.cardscannerapp.di.DataManager;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.ByteArrayOutputStream;

import io.reactivex.Observable;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class MainViewModel extends BaseViewModel<MainNavigator> {

    public MainViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void shut(){
        setIsLoading(true);
        getNavigator().onShutButtonClick();
    }

    public void handlePictureTaken(byte[] bytes, Camera camera){
        getCompositeDisposable().add(getDataManager()
                .handleTakenPictureByte(bytes, camera)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(result -> {
                    setIsLoading(false);
                    AppLogger.i(result.toString());
                }, throwable -> {
                    setIsLoading(false);
                    AppLogger.e(throwable.getLocalizedMessage());
                }));
    }

    public void handlePreviewFrame(){}

}
