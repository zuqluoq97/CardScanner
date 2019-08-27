package com.vgu.dungluong.cardscannerapp.ui.result.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.data.model.local.ContactField;
import com.vgu.dungluong.cardscannerapp.databinding.ItemWebBinding;
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
public class WebAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<String> mWebList;

    private DataManager mDataManager;

    private SchedulerProvider mSchedulerProvider;

    private ResultActivity mResultActivity;

    @Inject
    public WebAdapter(DataManager dataManager,
                        SchedulerProvider schedulerProvider,
                        ResultActivity resultActivity){
        mWebList = new ArrayList<>();
        mDataManager = dataManager;
        mSchedulerProvider = schedulerProvider;
        mResultActivity = resultActivity;
    }

    public void setWebList(List<String> webList){
        mWebList = webList;
        notifyDataSetChanged();
    }

    public List<String> getWebList() {
        return mWebList.stream()
                .filter(web -> !web.isEmpty())
                .map(web -> web.replaceAll("\\s+", ""))
                .collect(Collectors.toList());

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WebViewHolder(ItemWebBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof WebViewHolder)
            ((WebViewHolder) holder).onBind(mWebList.get(position));
    }

    @Override
    public int getItemCount() {
        return mWebList.size();
    }

    class WebViewHolder extends RecyclerView.ViewHolder
            implements WebItemViewModel.WebItemClickListener{

        private ItemWebBinding mBinding;

        private WebItemViewModel mWebItemViewModel;

        private int mPosition;

        public WebViewHolder(ItemWebBinding binding){
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void onBind(String web){
            mWebItemViewModel = new WebItemViewModel(mDataManager,
                    mSchedulerProvider,
                    web,
                    this,
                    mWebList.indexOf(web));
            mBinding.setViewModel(mWebItemViewModel);
            mBinding.executePendingBindings();
        }

        @Override
        public void updateWeb(String web, int position) {
            mWebList.set(position, web);
        }
    }
}