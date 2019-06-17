package com.vgu.dungluong.cardscannerapp.utils.rx;

import io.reactivex.Scheduler;

/**
 * Created by Dung Luong on 17/06/2019
 */
public interface SchedulerProvider {

    Scheduler ui();

    Scheduler computation();

    Scheduler io();
}
