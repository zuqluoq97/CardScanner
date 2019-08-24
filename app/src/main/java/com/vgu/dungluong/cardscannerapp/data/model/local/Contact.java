package com.vgu.dungluong.cardscannerapp.data.model.local;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import java.util.List;

/**
 * Created by Dung Luong on 22/08/2019
 */
@AutoValue.CopyAnnotations
@AutoValue
public abstract class Contact implements Parcelable {

    @AutoValue.CopyAnnotations
    public abstract long groupId();

    @AutoValue.CopyAnnotations
    public abstract long contactId();

    @AutoValue.CopyAnnotations
    public abstract long rawContactId();

    @AutoValue.CopyAnnotations
    public abstract String displayName();

    // Contact organization fields.
    @AutoValue.CopyAnnotations
    public abstract String company();

    @AutoValue.CopyAnnotations
    public abstract String department();

    @AutoValue.CopyAnnotations
    public abstract String title();

    // Contact phone list.
    @AutoValue.CopyAnnotations
    public abstract List<ContactField> phoneList();

    // Contact email list
    @AutoValue.CopyAnnotations
    public abstract List<ContactField> emailList();

    // Contact address list.
    @AutoValue.CopyAnnotations
    public abstract ContactField addressList();

    // Contact website list.
    @AutoValue.CopyAnnotations
    public abstract List<String> websiteList();

    // Contact website list.
    @SuppressWarnings("mutable")
    @AutoValue.CopyAnnotations
    public abstract byte[] contactPhoto();

    public static Contact create(long groupId,
                                 long contactId,
                                 long rawContactId,
                                 String displayName,
                                 String company,
                                 String department,
                                 String title,
                                 List<ContactField> phoneList,
                                 List<ContactField> emailList,
                                 ContactField addressList,
                                 List<String> websiteList,
                                 byte[] contactPhoto){
        return builder()
                .groupId(groupId)
                .contactId(contactId)
                .rawContactId(rawContactId)
                .displayName(displayName)
                .company(company)
                .department(department)
                .title(title)
                .phoneList(phoneList)
                .emailList(emailList)
                .addressList(addressList)
                .websiteList(websiteList)
                .contactPhoto(contactPhoto)
                .build();
    }

    public static Builder builder(){
        return new AutoValue_Contact.Builder();
    }

    public abstract Contact withGroupId(long groupId);
    public abstract Contact withContactId(long contactId);
    public abstract Contact withrawContactId(long rawContactId);
    public abstract Contact withDisplayName(String displayName);
    public abstract Contact withCompany(String company);
    public abstract Contact withDepartment(String department);
    public abstract Contact withTitle(String title);
    public abstract Contact withPhoneList(List<ContactField> phoneList);
    public abstract Contact withEmailList(List<ContactField> emailList);
    public abstract Contact withAddressList(ContactField addressList);
    public abstract Contact withWebsiteList(List<String> websiteList);
    public abstract Contact withContactPhoto(byte[] contactPhoto);


    @AutoValue.Builder
    public abstract static class Builder{

        public abstract Builder groupId(long groupId);
        public abstract Builder contactId(long contactId);
        public abstract Builder rawContactId(long rawContactId);
        public abstract Builder displayName(String displayName);
        public abstract Builder company(String company);
        public abstract Builder department(String department);
        public abstract Builder title(String title);
        public abstract Builder phoneList(List<ContactField> phoneList);
        public abstract Builder emailList(List<ContactField> emailList);
        public abstract Builder addressList(ContactField addressList);
        public abstract Builder websiteList(List<String> websiteList);
        public abstract Builder contactPhoto(byte[] contactPhoto);


        public abstract Contact build();
    }
}
