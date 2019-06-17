package com.vgu.dungluong.cardscannerapp.utils;

import timber.log.Timber;

/**
 * Created by Dung Luong on 17/06/2019
 */
public class AppLogger {

    private AppLogger() {
        // This utility class is not publicly instantiable
    }

    public static void init() {
        // if (BuildConfig.DEBUG) {
        Timber.plant(new Timber.DebugTree());
        // }
    }

    public static void d(String s, Object... objects) {
        Timber.d(s, objects);
    }

    public static void d(Throwable throwable, String s, Object... objects) {
        Timber.d(throwable, s, objects);
    }

    public static void i(String s, Object... objects) {
        Timber.i(s, objects);
    }

    public static void i(Throwable throwable, String s, Object... objects) {
        Timber.i(throwable, s, objects);
    }

    public static void w(String s, Object... objects) {
        Timber.w(s, objects);
    }

    public static void w(Throwable throwable, String s, Object... objects) {
        Timber.w(throwable, s, objects);
    }

    public static void e(String s, Object... objects) {
        Timber.e(s, objects);
    }

    public static void e(Throwable throwable, String s, Object... objects) {
        Timber.e(throwable, s, objects);
    }
}
