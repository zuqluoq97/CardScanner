package com.vgu.dungluong.cardscannerapp.ui.custom;

import android.content.Context;
import androidx.annotation.NonNull;

import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;


import com.vgu.dungluong.cardscannerapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dung Luong on 23/08/2019
 */
public class AutoCompleteAdapter extends ArrayAdapter{

    private List<String> mArrayList;
    private Filter filter = new NoFilter();

    public AutoCompleteAdapter(@NonNull Context context,
                               @NonNull List<String> objects) {
        super(context, R.layout.dropdown_item, R.id.item, objects);
        this.mArrayList = objects;
    }

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class NoFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence arg0) {
            FilterResults result = new FilterResults();
            result.values = mArrayList;
            result.count = mArrayList.size();
            return result;
        }

        @Override
        protected void publishResults(CharSequence arg0, FilterResults arg1) {
            notifyDataSetChanged();
        }
    }
}
