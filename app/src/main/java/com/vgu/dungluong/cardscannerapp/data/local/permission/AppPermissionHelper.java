package com.vgu.dungluong.cardscannerapp.data.local.permission;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.core.content.ContextCompat;

/**
 * Created by Dung Luong on 17/06/2019
 */
@Singleton
public class AppPermissionHelper implements PermissionHelper{

    private Context mContext;

    @Inject
    public AppPermissionHelper(Context context){
        this.mContext = context;
    }

    @Override
    public boolean hasUseCamera() {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean hasReadExternalStorage() {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean hasWriteExternalStorage() {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean hasWriteContact() {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean hasReadContact() {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }
}
