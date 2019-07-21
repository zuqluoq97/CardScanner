package com.vgu.dungluong.cardscannerapp.data.remote;

import java.io.File;

import io.reactivex.Single;

/**
 * Created by Dung Luong on 19/07/2019
 */
public interface ApiHelper {

    Single<String> doServerTextDetection(File imgFile);
}
