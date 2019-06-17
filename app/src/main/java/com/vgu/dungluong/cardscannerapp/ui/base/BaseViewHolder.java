package com.vgu.dungluong.cardscannerapp.ui.base;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Dung Luong on 17/06/2019
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBind();

}