package com.vgu.dungluong.cardscannerapp.data.remote;

import com.androidnetworking.common.Priority;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.vgu.dungluong.cardscannerapp.data.model.api.Rects;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Single;

/**
 * Created by Dung Luong on 19/07/2019
 */
public class AppApiHelper implements ApiHelper {

    @Inject
    public AppApiHelper(){

    }


    @Override
    public Single<Rects> doServerTextDetection(File imgFile) {
        return Rx2AndroidNetworking.upload(ApiEndPoint.ENDPOINT_SERVER_TEXT_DETECTION)
                .addMultipartFile("image", imgFile)
                .setPriority(Priority.HIGH)
                .build()
                .getObjectSingle(Rects.class);
    }
}
