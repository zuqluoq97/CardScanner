package com.vgu.dungluong.cardscannerapp.data.model.local;

/**
 * Created by Dung Luong on 10/08/2019
 */
public final class OnTouchZone {

    private final double left, top, right, bottom;

    public OnTouchZone(final double left, final double top, final double right, final double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public final boolean contains(final double x, final double y) {
        return x > this.left && x < this.right && y > this.top && y < this.bottom;
    }
}
