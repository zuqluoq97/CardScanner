package com.vgu.dungluong.cardscannerapp.ui.result.adapter;

import android.provider.ContactsContract;

import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.data.model.local.ContactField;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.AppConstants;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import java.util.Objects;
import java.util.stream.Collectors;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;

/**
 * Created by Dung Luong on 22/08/2019
 */
public class EmailItemViewModel extends BaseViewModel {

    @Bindable
    public ObservableArrayList<String> typeObservableArrayList;

    @Bindable
    public ObservableField<String> mEmailObservableField;

    @Bindable
    public ObservableField<String> mEmailDataTypeObservableField;

    private ContactField mContactField;

    private final EmailItemClickListener mListener;

    public EmailItemViewModel(DataManager dataManager,
                              SchedulerProvider schedulerProvider,
                              ContactField contactField,
                              EmailItemClickListener listener) {
        super(dataManager, schedulerProvider);
        typeObservableArrayList = new ObservableArrayList<>();
        typeObservableArrayList.addAll(AppConstants.DATA_TYPE1_TYPE_TITLE);
        notifyPropertyChanged(BR.typeObservableArrayList);
        mEmailObservableField = new ObservableField<>(contactField.dataValue());
        mEmailDataTypeObservableField = new ObservableField<>(typeObservableArrayList.get(1));
        mContactField = contactField;
        mListener = listener;
    }

    @Bindable
    public String getEmailObservableField(){
        return mEmailObservableField.get();
    }

    public void setEmailObservableField(String email){
        if(!Objects.equals(getEmailObservableField(), email)){
            mEmailObservableField.set(email);
            notifyPropertyChanged(BR.emailObservableField);
            mListener.updateContactField(mContactField.withDataValue(email));
        }
    }

    @Bindable
    public String getEmailDataTypeObservableField(){
        return mEmailDataTypeObservableField.get();
    }

    public void setEmailDataTypeObservableField(String type){
        if(!Objects.equals(getEmailDataTypeObservableField(), type)){
            mEmailDataTypeObservableField.set(type);
            notifyPropertyChanged(BR.emailDataTypeObservableField);
            switch (typeObservableArrayList.stream()
                    .map(String::toLowerCase).collect(Collectors.toList())
                    .indexOf(type.toLowerCase().trim())){
                case 0:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Email.TYPE_HOME));
                    break;
                case 1:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Email.TYPE_WORK));
                    break;
                case 2:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Email.TYPE_OTHER));
                    break;
                default:
                    if(type.isEmpty()){
                        mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Email.TYPE_WORK));
                    }else {
                        mContactField = mContactField.withDataLabel(type);
                        mContactField= mContactField.withDataType(ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
                        mListener.updateContactField(mContactField);
                    }
            }
        }
    }

    public interface EmailItemClickListener{
        void updateContactField(ContactField contactField);
    }
}
