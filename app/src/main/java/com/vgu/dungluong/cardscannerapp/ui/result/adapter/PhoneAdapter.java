package com.vgu.dungluong.cardscannerapp.ui.result.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.data.model.local.ContactField;
import com.vgu.dungluong.cardscannerapp.databinding.ItemEmailBinding;
import com.vgu.dungluong.cardscannerapp.databinding.ItemPhoneBinding;
import com.vgu.dungluong.cardscannerapp.ui.result.ResultActivity;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Dung Luong on 22/08/2019
 */
public class PhoneAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<ContactField> mContactFieldList;

    private DataManager mDataManager;

    private SchedulerProvider mSchedulerProvider;

    private ResultActivity mResultActivity;

    @Inject
    public PhoneAdapter(DataManager dataManager,
                        SchedulerProvider schedulerProvider,
                        ResultActivity resultActivity){
        mContactFieldList = new ArrayList<>();
        mDataManager = dataManager;
        mSchedulerProvider = schedulerProvider;
        mResultActivity = resultActivity;
    }

    public void setContactFieldList(List<ContactField> contactFieldList){
        mContactFieldList = contactFieldList;
        notifyDataSetChanged();
    }

    public List<ContactField> getContactField() {
        return mContactFieldList.stream().filter(contactField -> !contactField.dataValue().isEmpty()).collect(Collectors.toList());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhoneViewHolder(ItemPhoneBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof PhoneViewHolder)
            ((PhoneViewHolder) holder).onBind(mContactFieldList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mContactFieldList.size();
    }

    class PhoneViewHolder extends RecyclerView.ViewHolder
            implements PhoneItemViewModel.PhoneItemClickListener{

        private ItemPhoneBinding mBinding;

        private PhoneItemViewModel mPhoneItemViewModel;

        private int mPosition;

        public PhoneViewHolder(ItemPhoneBinding binding){
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void onBind(ContactField contactField, int position){
            mPhoneItemViewModel = new PhoneItemViewModel(mDataManager,
                    mSchedulerProvider,
                    contactField,
                    this);
            mPosition = position;
            mBinding.setViewModel(mPhoneItemViewModel);
            mBinding.executePendingBindings();
        }

        @Override
        public void updateContactField(ContactField contactField) {
            mContactFieldList.set(mPosition, contactField);
        }
    }
}