package com.vgu.dungluong.cardscannerapp.ui.result.adapter;

import android.provider.ContactsContract;

import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.data.model.local.ContactField;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.AppConstants;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import java.util.Objects;
import java.util.stream.Collectors;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;

/**
 * Created by Dung Luong on 22/08/2019
 */
public class PhoneItemViewModel extends BaseViewModel {

    @Bindable
    public ObservableArrayList<String> typeObservableArrayList;

    private ObservableField<String> mPhoneObservableField;

    private ObservableField<String> mPhoneDataTypeObservableField;

    private ContactField mContactField;

    private final PhoneItemClickListener mListener;

    private int mPosition;

    public PhoneItemViewModel(DataManager dataManager,
                              SchedulerProvider schedulerProvider,
                              ContactField contactField,
                              PhoneItemClickListener listener,
                              int position) {
        super(dataManager, schedulerProvider);
        mPosition = position;
        typeObservableArrayList = new ObservableArrayList<>();
        typeObservableArrayList.addAll(AppConstants.DATA_TYPE2_TYPE_TITLE);
        notifyPropertyChanged(BR.typeObservableArrayList);
        mPhoneObservableField = new ObservableField<>(contactField.dataValue());
        mPhoneDataTypeObservableField = new ObservableField<>(typeObservableArrayList.get(2));
        mContactField = contactField;
        mListener = listener;
        if(!contactField.dataLabel().isEmpty()) {
            AppLogger.i(contactField.dataLabel());
            setPhoneDataTypeObservableField(contactField.dataLabel());
        }
    }

    @Bindable
    public String getPhoneObservableField(){
        return mPhoneObservableField.get();
    }

    public void setPhoneObservableField(String phone){
        if(!Objects.equals(getPhoneObservableField(), phone)){
            mPhoneObservableField.set(phone);
            notifyPropertyChanged(BR.phoneObservableField);
            mListener.updateContactField(mContactField.withDataValue(phone), mPosition);
        }
    }

    @Bindable
    public String getPhoneDataTypeObservableField(){
        return mPhoneDataTypeObservableField.get();
    }

    public void setPhoneDataTypeObservableField(String type){
        if(!Objects.equals(getPhoneDataTypeObservableField(), type)){
            mPhoneDataTypeObservableField.set(type);
            notifyPropertyChanged(BR.phoneDataTypeObservableField);
            AppLogger.i(type);
            AppLogger.i(typeObservableArrayList.stream()
                    .map(String::toLowerCase).collect(Collectors.toList()).toString());
            AppLogger.i(String.valueOf(typeObservableArrayList.stream()
                    .map(String::toLowerCase).collect(Collectors.toList()).indexOf(type.toLowerCase().trim())));
            switch (typeObservableArrayList.stream()
                    .map(String::toLowerCase).collect(Collectors.toList())
                    .indexOf(type.toLowerCase().trim())) {
                case 0:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE), mPosition);
                    break;
                case 1:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_HOME), mPosition);
                    break;
                case 2:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_WORK), mPosition);
                    break;
                case 3:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK), mPosition);
                    break;
                case 4:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME), mPosition);
                    break;
                case 5:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_PAGER), mPosition);
                    break;
                case 6:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_OTHER), mPosition);
                    break;
                case 7:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK), mPosition);
                    break;
                default:
                    if (type.isEmpty()) {
                        mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Email.TYPE_WORK), mPosition);
                    } else {
                        mContactField = mContactField.withDataLabel(type);
                        mContactField = mContactField.withDataType(ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
                        mListener.updateContactField(mContactField, mPosition);
                    }
            }
        }
    }

    public interface PhoneItemClickListener{
        void updateContactField(ContactField contactField, int position);
    }
}
