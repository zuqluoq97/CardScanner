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
public class PhoneItemViewModel extends BaseViewModel {

    @Bindable
    public ObservableArrayList<String> typeObservableArrayList;

    private ObservableField<String> mPhoneObservableField;

    private ObservableField<String> mPhoneDataTypeObservableField;

    private ContactField mContactField;

    private final PhoneItemClickListener mListener;

    public PhoneItemViewModel(DataManager dataManager,
                              SchedulerProvider schedulerProvider,
                              ContactField contactField,
                              PhoneItemClickListener listener) {
        super(dataManager, schedulerProvider);
        typeObservableArrayList = new ObservableArrayList<>();
        typeObservableArrayList.addAll(AppConstants.DATA_TYPE2_TYPE_TITLE);
        notifyPropertyChanged(BR.typeObservableArrayList);
        mPhoneObservableField = new ObservableField<>(contactField.dataValue());
        mPhoneDataTypeObservableField = new ObservableField<>(contactField.dataType() == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM ? contactField.dataLabel() :typeObservableArrayList.get(2));
        mContactField = contactField;
        mListener = listener;
    }

    @Bindable
    public String getPhoneObservableField(){
        return mPhoneObservableField.get();
    }

    public void setPhoneObservableField(String phone){
        if(!Objects.equals(getPhoneObservableField(), phone)){
            mPhoneObservableField.set(phone);
            notifyPropertyChanged(BR.phoneObservableField);
            mListener.updateContactField(mContactField.withDataValue(phone));
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
            switch (typeObservableArrayList.stream()
                    .map(String::toLowerCase).collect(Collectors.toList())
                    .indexOf(type.toLowerCase().trim())) {
                case 0:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE));
                    break;
                case 1:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_HOME));
                    break;
                case 2:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_WORK));
                    break;
                case 3:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK));
                    break;
                case 4:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME));
                    break;
                case 5:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_PAGER));
                    break;
                case 6:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_OTHER));
                    break;
                case 7:
                    mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK));
                    break;
                default:
                    if (type.isEmpty()) {
                        mListener.updateContactField(mContactField.withDataType(ContactsContract.CommonDataKinds.Email.TYPE_WORK));
                    } else {
                        mContactField = mContactField.withDataLabel(type);
                        mContactField = mContactField.withDataType(ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
                        mListener.updateContactField(mContactField);
                    }
            }
        }
    }

    public interface PhoneItemClickListener{
        void updateContactField(ContactField contactField);
    }
}
