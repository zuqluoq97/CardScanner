package com.vgu.dungluong.cardscannerapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.ViewModelProviderFactory;
import com.vgu.dungluong.cardscannerapp.databinding.ActivityMainBinding;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseActivity;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.AndroidInjector;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel>
        implements MainNavigator {

    @Inject
    ViewModelProviderFactory mViewModelProviderFactory;

    private MainViewModel mMainViewModel;

    private ActivityMainBinding mMainBinding;

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public MainViewModel getViewModel() {
        mMainViewModel = ViewModelProviders.of(this, mViewModelProviderFactory).get(MainViewModel.class);
        return mMainViewModel;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainBinding = getViewDataBinding();
        mMainBinding.setViewModel(mMainViewModel);
        mMainViewModel.setNavigator(this);
        checkPermission();
    }
}
