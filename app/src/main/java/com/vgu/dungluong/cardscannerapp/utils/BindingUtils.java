package com.vgu.dungluong.cardscannerapp.utils;

import com.vgu.dungluong.cardscannerapp.ui.custom.AutoCompleteAdapter;
import com.vgu.dungluong.cardscannerapp.ui.custom.ClearableInstantAutoComplete;

import java.util.List;

import androidx.databinding.BindingAdapter;

/**
 * Created by Dung Luong on 23/08/2019
 */
public class BindingUtils {

    private BindingUtils(){}

    // InputEditText
    @BindingAdapter({"items"})
    public static void setFilterAdapter(ClearableInstantAutoComplete inputEditText,
                                        List<String> objectItems){
        inputEditText.setAdapter(new AutoCompleteAdapter(inputEditText.getContext(), objectItems));
    }
}
