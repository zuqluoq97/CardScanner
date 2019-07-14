package com.vgu.dungluong.cardscannerapp.ui.main;

import android.hardware.Camera;
import android.os.Handler;

import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import org.opencv.core.Point;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Scheduler;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class MainViewModel extends BaseViewModel<MainNavigator> {

    public MainViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        new Handler().postDelayed(() ->
                getNavigator().changeColorIcon(getDataManager().getScanBlackCardState()), 500);
    }

    public void shut(){
        setIsLoading(true);
        getNavigator().onShutButtonClick();
    }

    public void changeBlackCardScanState(){
        getDataManager().setScanBlackCardState(!getDataManager().getScanBlackCardState());
        getNavigator().changeColorIcon(getDataManager().getScanBlackCardState());
    }

    public void handlePictureTaken(byte[] bytes, Camera camera){
        getCompositeDisposable().add(getDataManager()
                .handleTakenPictureByte(bytes, camera, findCropCoordinate(camera))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(result -> {
                    setIsLoading(false);
                    getNavigator().openCropActivity();
                }, throwable -> {
                    setIsLoading(false);
                    AppLogger.e(throwable.getLocalizedMessage());
                }));
    }

    private List<Point> findCropCoordinate(Camera camera){
        int[] locations = new int[2];
        Camera.Size pictureSize = camera.getParameters().getPictureSize();
        getNavigator().getCroppedView().getLocationOnScreen(locations);
        AppLogger.i(Arrays.toString(locations));
        float cropWidthRatio = (float) 17 / 3;
        float cropHeightRatio = (float) 79 / 9;

        float scaleViewHeight = (float) pictureSize.width / getNavigator().getSurfaceView().getHeight();
        float scaleViewWidth = (float) pictureSize.height / getNavigator().getSurfaceView().getWidth();
        return Arrays.asList(new Point(locations[0] * scaleViewWidth, (locations[1] - getNavigator().getTopOffSet()) * scaleViewHeight),
                new Point(locations[0] * cropWidthRatio * scaleViewWidth, (locations[1] - getNavigator().getTopOffSet()) * scaleViewHeight),
                new Point(locations[0] * cropWidthRatio * scaleViewWidth, (locations[1] - getNavigator().getTopOffSet()) * cropHeightRatio * scaleViewHeight),
                new Point(locations[0] * scaleViewWidth, (locations[1] - getNavigator().getTopOffSet()) * cropHeightRatio * scaleViewHeight));
    }

}