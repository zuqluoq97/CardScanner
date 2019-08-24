package com.vgu.dungluong.cardscannerapp.data.remote;

import com.vgu.dungluong.cardscannerapp.data.model.api.Labels;
import com.vgu.dungluong.cardscannerapp.data.model.api.Rects;

import org.json.JSONObject;

import java.io.File;

import io.reactivex.Single;

/**
 * Created by Dung Luong on 19/07/2019
 */
public interface ApiHelper {

    Single<Rects> doServerTextDetection(File imgFile);

    Single<Labels> doServerTextClassification(JSONObject textUnLabelJSONObject);
}
