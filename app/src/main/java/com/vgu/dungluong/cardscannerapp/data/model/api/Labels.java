package com.vgu.dungluong.cardscannerapp.data.model.api;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.vgu.dungluong.cardscannerapp.data.model.local.ContactField;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dung Luong on 23/08/2019
 */
@AutoValue.CopyAnnotations
@AutoValue
public abstract class Labels implements Parcelable {

    @AutoValue.CopyAnnotations
    @SerializedName("labels")
    public abstract List<String> labels();

    public static Labels create(List<String> labels){
        return builder().labels(labels).build();
    }

    public static Builder builder(){
        return new AutoValue_Labels.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder{

        public abstract Builder labels(List<String> labels);

        public abstract Labels build();
    }

    public static TypeAdapter<Labels> typeAdapter(Gson gson){
        return new AutoValue_Labels.GsonTypeAdapter(gson);
    }

    public String getName(List<String> texts){
        String name = "";
        for(int i = 0; i < labels().size(); i++){
            if(labels().get(i).equals("name")){
                name += texts.get(i) + " ";
            }
        }

        return name.trim();
    }

    public String getCompany(List<String> texts){
        String company = "";
        for(int i = 0; i < labels().size(); i++){
            if(labels().get(i).equals("company")){
                company += texts.get(i) + " ";
            }
        }

        return company.trim();
    }

    public String getJob(List<String> texts){
        String job = "";
        for(int i = 0; i < labels().size(); i++){
            if(labels().get(i).equals("job")){
                job += texts.get(i) + " ";
            }
        }

        return job.trim();
    }
}
