package com.vgu.dungluong.cardscannerapp.ui.result.adapter;

import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.data.model.local.ContactField;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import java.util.Objects;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

/**
 * Created by Dung Luong on 22/08/2019
 */
public class WebItemViewModel extends BaseViewModel {

    private ObservableField<String> mWebObservableField;

    private WebItemClickListener mListener;

    public WebItemViewModel(DataManager dataManager,
                            SchedulerProvider schedulerProvider,
                            String web,
                            WebItemClickListener listener) {
        super(dataManager, schedulerProvider);
        mWebObservableField = new ObservableField<>(web);
        mListener = listener;
    }

    @Bindable
    public String getWebObservableField(){
        return mWebObservableField.get();
    }

    public void setWebObservableField(String web){
        if(!Objects.equals(getWebObservableField(), web)){
            mWebObservableField.set(web);
            notifyPropertyChanged(BR.webObservableField);
            mListener.updateWeb(web);
        }
    }

    public interface WebItemClickListener{
        void updateWeb(String web);
    }
}
