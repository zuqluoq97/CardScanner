package com.vgu.dungluong.cardscannerapp.ui.result;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.R;
import com.vgu.dungluong.cardscannerapp.ViewModelProviderFactory;
import com.vgu.dungluong.cardscannerapp.databinding.ActivityMainBinding;
import com.vgu.dungluong.cardscannerapp.databinding.ActivityResultBinding;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseActivity;
import com.vgu.dungluong.cardscannerapp.ui.main.MainActivity;
import com.vgu.dungluong.cardscannerapp.ui.main.MainViewModel;
import com.vgu.dungluong.cardscannerapp.utils.AppConstants;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.core.view.LayoutInflaterCompat;
import androidx.lifecycle.ViewModelProviders;

import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.DATA_PATH;
import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.TESSDATA;

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

    TessBaseAPI mTessBaseAPI;

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_result;
    }


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
        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);
        mResultBinding = getViewDataBinding();
        mResultViewModel.setNavigator(this);
        mResultBinding.setViewModel(mResultViewModel);
        checkPermission();

        setUp();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mResultViewModel.displayCardImage();
    }

    private void setUp() {

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
    public void prepareTesseract() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA);
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyTessDataFiles(TESSDATA);
        mTessBaseAPI = new TessBaseAPI();
        mTessBaseAPI.init(AppConstants.DATA_PATH, AppConstants.LANG);
        //mTessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "aAáÁàÀạẠãÃảẢăĂắẮằẰặẶẵẴẳẲâÂấẤầẦậẬẫẪẩẨbBcCdDđĐeEéÉèÈẹẸẽẼẻẺêÊếẾềỀệỆễỄểỂfFgGhHiIíÍìÌịỊĩĨỉỈjJkKlLmMnNoOóÓòÒọỌõÕỏỎôÔốỐồỒộỘỗỖổỔơƠớỚờỜợỢỡỠởỞpPqQrRsStTuUúÚùÙụỤũŨủỦưƯứỨừỪựỰữỮửỬvVxXyYýÝỳỲỵỴỹỸỷỶwWzZ123456789',.@-:/ ");
    }

    /**
     * Prepare directory on external storage
     *
     * @param path
     * @throws Exception
     */
    private void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                AppLogger.e( "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            AppLogger.i("Created directory " + path);
        }
    }

    /**
     * Copy tessdata files (located on assets/tessdata) to destination directory
     *
     * @param path - name of directory with .traineddata files
     */
    private void copyTessDataFiles(String path) {
        try {
            String fileList[] = getAssets().list(path);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = DATA_PATH + path + "/" + fileName;

                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    AppLogger.i("Copied " + fileName + "to tessdata");
                }
            }
        } catch (IOException e) {
            AppLogger.i( "Unable to copy files to tessdata " + e.toString());
        }
    }
}
