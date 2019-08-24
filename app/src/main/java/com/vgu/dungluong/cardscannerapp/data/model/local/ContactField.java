package com.vgu.dungluong.cardscannerapp.data.model.local;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

/**
 * Created by Dung Luong on 22/08/2019
 */
@AutoValue.CopyAnnotations
@AutoValue
public abstract class ContactField implements Parcelable {

    @AutoValue.CopyAnnotations
    public abstract int dataType();

    @AutoValue.CopyAnnotations
    public abstract String dataLabel();

    @AutoValue.CopyAnnotations
    public abstract String dataValue();

    public static ContactField create(int dataType, String dataLabel, String dataValue){
        return builder()
                .dataType(dataType)
                .dataLabel(dataLabel)
                .dataValue(dataValue)
                .build();
    }

    public static Builder builder(){
        return new AutoValue_ContactField.Builder();
    }

    public abstract ContactField withDataType(int dataType);

    public abstract ContactField withDataLabel(String dataLabel);

    public abstract ContactField withDataValue(String dataValue);

    @AutoValue.Builder
    public abstract static class Builder{

        public abstract Builder dataType(int dataType);

        public abstract Builder dataLabel(String dataLabel);

        public abstract Builder dataValue(String dataValue);

        public abstract ContactField build();
    }
}
