package com.vgu.dungluong.cardscannerapp.data.model.local;

import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Created by Dung Luong on 19/06/2019
 */
public class Corners {

    private List<Point> corners;

    private Size size;

    public Corners(List<Point> corners, Size size) {
        this.corners = corners;
        this.size = size;
    }

    public Corners(List<Point> corners){
        this.corners = corners;
    }

    public List<Point> getCorners() {
        return corners;
    }

    public void setCorners(List<Point> corners) {
        this.corners = corners;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    @NonNull
    @Override
    public String toString() {
        return corners.toString() + " " + size.toString();
    }
}
