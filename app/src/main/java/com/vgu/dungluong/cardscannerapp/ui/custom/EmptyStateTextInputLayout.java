package com.vgu.dungluong.cardscannerapp.ui.custom;

import android.content.Context;
import com.google.android.material.textfield.TextInputLayout;
import com.vgu.dungluong.cardscannerapp.R;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

/**
 * Created by Dung Luong on 22/08/2019
 */
public class EmptyStateTextInputLayout extends TextInputLayout {
    private boolean emptyText = true;
    private static final int[] EMPTY_TEXT_STATE = new int[]{R.attr.state_empty_text};

    public EmptyStateTextInputLayout(Context context) {
        super(context);
    }

    public EmptyStateTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyStateTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] state = super.onCreateDrawableState(extraSpace + 1);
        if (emptyText) {
            mergeDrawableStates(state, EMPTY_TEXT_STATE);
        }
        return state;
    }

    public void setEmptyTextState(boolean emptyTextState) {
        this.emptyText = emptyTextState;
        refreshDrawableState();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof AutoCompleteTextView) {
            AutoCompleteTextView actv = (AutoCompleteTextView) child;
            if (!TextUtils.isEmpty(actv.getText())) {
                setEmptyTextState(false);
            }
            actv.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!TextUtils.isEmpty(editable)) {
                        setEmptyTextState(false);
                    } else {
                        setEmptyTextState(true);
                    }
                }
            });
        } else if (child instanceof EditText) {
            EditText editText = (EditText) child;
            if (!TextUtils.isEmpty(editText.getText())) {
                setEmptyTextState(false);
            }
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!TextUtils.isEmpty(editable)) {
                        setEmptyTextState(false);
                    } else {
                        setEmptyTextState(true);
                    }
                }
            });
        }
        super.addView(child, index, params);
    }
}
