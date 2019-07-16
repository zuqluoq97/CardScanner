package com.vgu.dungluong.cardscannerapp.ui.result;

import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.vgu.dungluong.cardscannerapp.utils.AppConstants;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Dung Luong on 02/07/2019
 */
@Module
public class ResultActivityModule {

    @Provides
    TessBaseAPI provideTessBaseAPI(){
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(AppConstants.DATA_PATH, AppConstants.LANG, TessBaseAPI.OEM_TESSERACT_ONLY);
//        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "qQwWeErRtTyYuUiIoOpPaAsSdDfFgGhHjJkKlLzZxXcCvVbBnNmM0123456789+-.:@()");
//        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "aAáÁàÀạẠãÃảẢăĂắẮằẰặẶẵẴẳẲâÂấẤầẦậẬẫẪẩẨbBcCdDđĐeEéÉèÈẹẸẽẼẻẺêÊếẾềỀệỆễỄểỂfFgGhHiIíÍìÌịỊĩĨỉỈjJkKlLmMnNoOóÓòÒọỌõÕỏỎôÔốỐồỒộỘỗỖổỔơƠớỚờỜợỢỡỠởỞpPqQrRsStTuUúÚùÙụỤũŨủỦưƯứỨừỪựỰữỮửỬvVxXyYýÝỳỲỵỴỹỸỷỶwWzZ123456789',.@-:/()");
        return tessBaseAPI;
    }
}