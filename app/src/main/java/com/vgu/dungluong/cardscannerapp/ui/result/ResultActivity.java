package com.vgu.dungluong.cardscannerapp.ui.result;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.ViewModelProviderFactory;
import com.vgu.dungluong.cardscannerapp.data.model.local.ContactField;
import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;
import com.vgu.dungluong.cardscannerapp.data.model.local.OnTouchZone;
import com.vgu.dungluong.cardscannerapp.databinding.ActivityResultBinding;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseActivity;
import com.vgu.dungluong.cardscannerapp.ui.main.MainActivity;
import com.vgu.dungluong.cardscannerapp.ui.result.adapter.EmailAdapter;
import com.vgu.dungluong.cardscannerapp.ui.result.adapter.PhoneAdapter;
import com.vgu.dungluong.cardscannerapp.ui.result.adapter.WebAdapter;
import com.vgu.dungluong.cardscannerapp.utils.AppConstants;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CommonUtils;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 * Created by Dung Luong on 02/07/2019
 */
public class ResultActivity extends BaseActivity<ActivityResultBinding, ResultViewModel>
        implements ResultNavigator{

    @Inject
    ViewModelProviderFactory mViewModelProviderFactory;

    private ResultViewModel mResultViewModel;

    private ActivityResultBinding mResultBinding;

    public static final String TAG = MainActivity.class.getSimpleName();

    @Inject
    EmailAdapter mEmailAdapter;

    @Inject
    PhoneAdapter mPhoneAdapter;

    @Inject
    WebAdapter mWebAdapter;

    @Inject
    LinearLayoutManager mLinearLayoutManager1;

    @Inject
    LinearLayoutManager mLinearLayoutManager2;

    @Inject
    LinearLayoutManager mLinearLayoutManager3;
    @Inject
    TessBaseAPI mTessBaseAPI;

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_result;
    }

    private List<OnTouchZone> mOnTouchZones;

    private int mStatusBarHeight;

    @Override
    public ResultViewModel getViewModel() {
        mResultViewModel = ViewModelProviders.of(this, mViewModelProviderFactory).get(ResultViewModel.class);
        return mResultViewModel;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, ResultActivity.class);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        mResultBinding = getViewDataBinding();
        mResultViewModel.setNavigator(this);
        mResultBinding.setViewModel(mResultViewModel);
        checkPermission();

        setUp();
        listeners();
        subscribeToLiveData();
    }


    private void listeners() {
        mResultBinding.cardImageView.setOnTouchListener((view, motionEvent) -> {
            view.performClick();
            for(int i = 0; i < mOnTouchZones.size(); i++){
                OnTouchZone onTouchZone = mOnTouchZones.get(i);
                if (onTouchZone.contains(motionEvent.getX() / mResultViewModel.getScaleRatioWidth(), (motionEvent.getY() - mStatusBarHeight) / mResultViewModel.getScaleRatioHeight())) {
                    // Your action
                    List<Corners> currentCropAreas = mResultViewModel.getRects();
                    currentCropAreas.remove(mOnTouchZones.indexOf(onTouchZone));
                    mResultViewModel.updateCropAreas(currentCropAreas);
                    break;
                }
            }
            return false;
        });
    }

    private void subscribeToLiveData() {
        mResultViewModel.getOnTouchZones().observe(this, touchZones -> {
            mOnTouchZones = touchZones;
        });

        mResultViewModel.getPhoneContactFieldMutableLiveData().observe(this, phoneContactFields ->{
            mPhoneAdapter.setContactFieldList(phoneContactFields);
        });

        mResultViewModel.getEmailContactFieldMutableLiveData().observe(this, emailContactFields ->{
            mEmailAdapter.setContactFieldList(emailContactFields);
        });

        mResultViewModel.getWebContactFieldMutableLiveData().observe(this, webList ->{
            mWebAdapter.setWebList(webList);
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mResultViewModel.displayCardImage();
    }

    private void setUp() {
        mOnTouchZones = new ArrayList<>();
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        mStatusBarHeight = rectangle.top;

        // email adapter
        mResultBinding.telRecyclerView.setLayoutManager(mLinearLayoutManager1);
        mResultBinding.telRecyclerView.setNestedScrollingEnabled(false);
        mResultBinding.telRecyclerView.setAdapter(mPhoneAdapter);

        // email adapter
        mResultBinding.emailRecyclerView.setLayoutManager(mLinearLayoutManager2);
        mResultBinding.emailRecyclerView.setNestedScrollingEnabled(false);
        mResultBinding.emailRecyclerView.setAdapter(mEmailAdapter);

        // web adapter
        mResultBinding.webRecyclerView.setLayoutManager(mLinearLayoutManager3);
        mResultBinding.webRecyclerView.setNestedScrollingEnabled(false);
        mResultBinding.webRecyclerView.setAdapter(mWebAdapter);
    }

    @Override
    public ImageView getCardImageView() {
        return mResultBinding.cardImageView;
    }

    @Override
    public void handleError(String error) {
        super.handleError(error);
    }

    @Override
    public void showMessage(String message) {
        super.showMessage(message);
    }

    @Override
    public TessBaseAPI getTesseractApi() {
        return mTessBaseAPI;
    }

    @Override
    public File getFileForCropImage() {
        return new File(Environment.getExternalStorageDirectory(), "crop.jpg");
    }

    @Override
    public ContentResolver getContentResolver() {
        return super.getContentResolver();
    }

    @Override
    public void animateButton(View view, int time) {
        CommonUtils.rotate(view, time);
    }

    @Override
    public void addOnePhoneContactField() {
        List<ContactField> phoneContactFields = mPhoneAdapter.getContactField();
        phoneContactFields.add(ContactField.create(-1, "", ""));
        AppLogger.i(phoneContactFields.toString());
        mPhoneAdapter.setContactFieldList(phoneContactFields);
    }

    @Override
    public void addOneEmailContactField() {
        List<ContactField> emailContactFields = mEmailAdapter.getContactField();
        emailContactFields.add(ContactField.create(-1, "", ""));
        AppLogger.i(emailContactFields.toString());
        mEmailAdapter.setContactFieldList(emailContactFields);
    }

    @Override
    public void addOneWebContactField() {
        List<String> webContactFields = mWebAdapter.getWebList();
        webContactFields.add("");
        mWebAdapter.setWebList(webContactFields);
    }

    @Override
    public List<ContactField> getEmailContactFields() {
        return mEmailAdapter.getContactField();
    }

    @Override
    public List<ContactField> getPhoneContactFields() {
        return mPhoneAdapter.getContactField();
    }

    @Override
    public List<String> getWebs() {
        return mWebAdapter.getWebList();
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public void openMainActivity() {
        onBackPressed();
    }
}
