package com.vgu.dungluong.cardscannerapp.ui.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.data.local.locale.AppLocaleHelper;
import com.vgu.dungluong.cardscannerapp.data.local.preference.AppPreferenceHelper;
import com.vgu.dungluong.cardscannerapp.utils.AppConstants;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CommonUtils;
import com.vgu.dungluong.cardscannerapp.utils.PermissionUtils;

import java.util.Objects;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import dagger.android.AndroidInjection;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.CODE_PERMISSIONS_REQUEST;
import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.PERMISSIONS;

/**
 * Created by Dung Luong on 17/06/2019
 */
public abstract class BaseActivity<T extends ViewDataBinding, V extends BaseViewModel>
        extends AppCompatActivity implements BaseFragment.Callback{

    // TODO
    // This can probably depend on isLoading variable of BaseViewModel
    // since its going to be common for all the activities
    private ProgressDialog mProgressDialog;
    private V mViewModel;
    private T mViewDataBinding;

    /**
     * Override for set binding variable
     * @return variable id
     */
    public abstract int getBindingVariable();

    /**
     * @return layout resource id
     */
    public abstract
    @LayoutRes
    int getLayoutId();

    /**
     * Override for set view model
     * @return view model instance
     */
    public abstract V getViewModel();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        performDependencyInjection();
        super.onCreate(savedInstanceState);
        performDataBinding();
    }

    public T getViewDataBinding(){
        return mViewDataBinding;
    }

    public void performDependencyInjection(){
        AndroidInjection.inject(this);
    }

    public void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(imm).hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    public void hideLoading(){
        if(mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
    }

    private void showLoading(){
        hideLoading();
        //  mProgressDialog = CommonUtils.showLoadingDialog(this);
    }

    private void performDataBinding(){
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId());
        this.mViewModel = mViewModel == null ? getViewModel() : mViewModel;
        mViewDataBinding.setVariable(getBindingVariable(), mViewModel);
        mViewDataBinding.executePendingBindings();
    }

    @Override
    public void onFragmentAttached() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper
                .wrap(new AppLocaleHelper(new AppPreferenceHelper(base,
                        AppConstants.PREF_NAME)).setLocale(base)));
    }

    public void restart(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public boolean checkPermission() {
        if(!mViewModel.haveFullPermissionGained()){
            requestPermissions();
        }
        return mViewModel.haveFullPermissionGained();
    }

    /**
     * Requesting permissions
     * If the permission has been denied previously, a dialog will prompt the user to grant
     * the permission, otherwise it is requested directly
     */
    private void requestPermissions() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                PERMISSIONS[0])
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                PERMISSIONS[1])
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                PERMISSIONS[2])){
            // Provide an additional rationale to the user if the permissions were not granted
            // and the user would benefit from additional contest for the use of the permission.
            // For example if the user has previously denied the permission
            CommonUtils.dialogConfiguration(this,
                    getString(R.string.request_permissions_title),
                    getString(R.string.request_permissions_content),
                    false)
                    .setPositiveButton(getString(R.string.confirm_title), ((dialog, which) -> {
                        // re-request
                        openRequestPermissionDialog();
                    })).show();

        } else {
            // permissions have not been granted yet. Request them directly
            openRequestPermissionDialog();
        }
    }

    private void openRequestPermissionDialog() {
        ActivityCompat.requestPermissions(this,
                PERMISSIONS,
                CODE_PERMISSIONS_REQUEST);

    }

    public void requestFocus(View view){
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void showMessage(String message){
        CommonUtils.showQuickToast(this, message);
    }

    public void handleError(String error){
        CommonUtils.showLongToast(this, error);
    }
}

