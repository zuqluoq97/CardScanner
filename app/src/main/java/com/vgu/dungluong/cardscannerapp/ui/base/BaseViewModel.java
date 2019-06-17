package com.vgu.dungluong.cardscannerapp.ui.base;

import com.vgu.dungluong.cardscannerapp.di.DataManager;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import java.lang.ref.WeakReference;

import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.ViewModel;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Dung Luong on 17/06/2019
 */
public abstract class BaseViewModel<N> extends ViewModel implements Observable {

    private PropertyChangeRegistry mCallbacks;

    private WeakReference<N> mNavigator;

    private final DataManager mDataManager;

    private final SchedulerProvider mSchedulerProvider;

    private final ObservableBoolean mIsLoading;

    private CompositeDisposable mCompositeDisposable;

    public BaseViewModel(DataManager dataManager,
                         SchedulerProvider schedulerProvider) {
        mCallbacks = new PropertyChangeRegistry();
        mDataManager = dataManager;
        mSchedulerProvider = schedulerProvider;
        mCompositeDisposable = new CompositeDisposable();
        mIsLoading = new ObservableBoolean(false);
    }

    @Override
    protected void onCleared() {
        mCompositeDisposable.dispose();
        super.onCleared();
    }

    public N getNavigator(){
        return mNavigator.get();
    }

    public void setNavigator( N navigator){
        this.mNavigator = new WeakReference<>(navigator);
    }

    public DataManager getDataManager(){
        return mDataManager;
    }

    public SchedulerProvider getSchedulerProvider() {
        return mSchedulerProvider;
    }

    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    public ObservableBoolean getIsLoading() {

        return mIsLoading;
    }

    public void setIsLoading(boolean isLoading){
        this.mIsLoading.set(isLoading);
    }

    public boolean haveFullPermissionGained(){
        return getDataManager().hasUseCamera()
                && getDataManager().hasReadExternalStorage()
                && getDataManager().hasWriteExternalStorage();
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        mCallbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        mCallbacks.remove(callback);
    }

    public void notifyPropertyChanged(int fieldId) {
        mCallbacks.notifyCallbacks(this, fieldId, null);
    }
}

