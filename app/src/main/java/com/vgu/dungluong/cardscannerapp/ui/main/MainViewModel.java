package com.vgu.dungluong.cardscannerapp.ui.main;

import android.hardware.Camera;

import com.vgu.dungluong.cardscannerapp.di.DataManager;
import com.vgu.dungluong.cardscannerapp.model.local.Corners;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.PaperProcessor;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

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
                .handleTakenPictureByte(bytes, camera, findCropCoordinate(camera))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(result -> {
                    setIsLoading(false);
                    AppLogger.i(result.toString());
                    getNavigator().openCropActivity();
                }, throwable -> {
                    setIsLoading(false);
                    AppLogger.e(throwable.getLocalizedMessage());
                }));
    }

    private List<Point> findCropCoordinate(Camera camera){
        int[] locations = new int[2];
        Camera.Size pictureSize = camera.getParameters().getPictureSize();
        getNavigator().getCroppedView().getLocationInWindow(locations);
        AppLogger.i(Arrays.toString(locations));
        float cropWidthRatio = (float) 14 / 3;
        float cropHeightRatio = (float) 14 / 3;
        AppLogger.i(pictureSize.width + " " + pictureSize.height);
        float scaleViewHeight = (float) pictureSize.width / getNavigator().getSurfaceView().getHeight();
        float scaleViewWidth = (float) pictureSize.height / getNavigator().getSurfaceView().getWidth();
        return Arrays.asList(new Point(locations[0] * scaleViewWidth, locations[1] * scaleViewHeight),
                new Point(locations[0] * cropWidthRatio * scaleViewWidth, locations[1] * scaleViewHeight),
                new Point(locations[0] * cropWidthRatio * scaleViewWidth, locations[1] * cropHeightRatio * scaleViewHeight),
                new Point(locations[0] * scaleViewWidth, locations[1] * cropHeightRatio * scaleViewHeight));
    }

//    public void handlePreviewFrame(byte[] bytes, Camera camera){
//        setIsLoading(true);
//        getCompositeDisposable().add(getDataManager()
//                .handlePictureFrame(bytes, camera)
//                .subscribeOn(getSchedulerProvider().io())
//                .observeOn(getSchedulerProvider().ui())
//                .subscribe(img -> {
//                    setIsLoading(false);
//                    AppLogger.i(img.toString());
//                    handleMat(img);
//                }, throwable -> {
//                    setIsLoading(false);
//                    AppLogger.e(throwable.getLocalizedMessage());
//                }));
//    }

//    private void handleMat(Mat img){
//        getCompositeDisposable().add(Observable.create((ObservableOnSubscribe<Corners>) emitter -> {
//            Corners corner = PaperProcessor.processPicture(img);
//            setIsLoading(false);
//            if(corner != null){
//                emitter.onNext(corner);
//            }else{
//                emitter.onError(new Throwable("paper not detected"));
//            }
//        })
//                .subscribeOn(getSchedulerProvider().io())
//                .observeOn(getSchedulerProvider().ui())
//                .subscribe(corner -> {
//                    AppLogger.i(corner.toString());
//                    getNavigator().getPaperRect().onCornersDetected(corner);
//                }, throwable -> {
//                    getNavigator().getPaperRect().onConrnersNotDetected();
//                }));
//    }
}