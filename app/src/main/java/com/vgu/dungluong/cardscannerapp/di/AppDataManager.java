package com.vgu.dungluong.cardscannerapp.di;

import android.content.Context;

import com.vgu.dungluong.cardscannerapp.data.permission.PermissionHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Dung Luong on 17/06/2019
 */
@Singleton
public class AppDataManager implements DataManager{

    private final Context mContext;

    private final PermissionHelper mPermissionHelper;

    @Inject
    public AppDataManager(Context context,
                          PermissionHelper permissionHelper){
        this.mContext = context;
        this.mPermissionHelper = permissionHelper;
    }

    @Override
    public boolean hasUseCamera() {
        return mPermissionHelper.hasUseCamera();
    }

    @Override
    public boolean hasReadExternalStorage() {
        return mPermissionHelper.hasReadExternalStorage();
    }

    @Override
    public boolean hasWriteExternalStorage() {
        return mPermissionHelper.hasWriteExternalStorage();
    }
}
