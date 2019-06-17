package com.vgu.dungluong.cardscannerapp.data.permission;

/**
 * Created by Dung Luong on 17/06/2019
 */
public interface PermissionHelper {

    boolean hasUseCamera();

    boolean hasReadExternalStorage();

    boolean hasWriteExternalStorage();
}
