package com.vgu.dungluong.cardscannerapp.data.model.api;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Dung Luong on 25/07/2019
 */
@AutoValue.CopyAnnotations
@AutoValue
public abstract class Rects implements Parcelable {

    @AutoValue.CopyAnnotations
    @SerializedName("rect")
    public abstract List<List<Integer>> rects();

    public static Rects create(List<List<Integer>> rects){
        return builder().rects(rects).build();
    }

    public static Builder builder(){
        return new AutoValue_Rects.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder{

        public abstract Builder rects(List<List<Integer>> rects);

        public abstract Rects build();
    }

    public static TypeAdapter<Rects> typeAdapter(Gson gson){
        return new AutoValue_Rects.GsonTypeAdapter(gson);
    }
}
