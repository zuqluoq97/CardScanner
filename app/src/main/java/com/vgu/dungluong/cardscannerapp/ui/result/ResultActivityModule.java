package com.vgu.dungluong.cardscannerapp.ui.result;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.vgu.dungluong.cardscannerapp.data.AppDataManager;
import com.vgu.dungluong.cardscannerapp.utils.AppConstants;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Dung Luong on 02/07/2019
 */
@Module
public class ResultActivityModule {

    @Provides
    TessBaseAPI provideTessBaseAPI(AppDataManager appDataManager){
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        String locale = appDataManager.getLocale();
        tessBaseAPI.init(AppConstants.DATA_PATH, locale.equals("vi") ? "vie" : "eng", TessBaseAPI.OEM_LSTM_ONLY);
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_RAW_LINE);
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, locale.equals("vi")
                ? "aAáÁàÀạẠãÃảẢăĂắẮằẰặẶẵẴẳẲâÂấẤầẦậẬẫẪẩẨbBcCdDđĐeEéÉèÈẹẸẽẼẻẺêÊếẾềỀệỆễỄểỂfFgGhHiIíÍìÌịỊĩĨỉỈjJkKlLmMnNoOóÓòÒọỌõÕỏỎôÔốỐồỒộỘỗỖổỔơƠớỚờỜợỢỡỠởỞpPqQrRsStTuUúÚùÙụỤũŨủỦưƯứỨừỪựỰữỮửỬvVxXyYýÝỳỲỵỴỹỸỷỶwzZ0123456789',.@-:/&#$+()_* "
                : "qQwWeErRtTyYuUiIoOpPaAsSdDfFgGhHjJkKlLzZxXcCvVbBnNmM0123456789',.@-:/&#$+()_* ");

        return tessBaseAPI;
    }
}