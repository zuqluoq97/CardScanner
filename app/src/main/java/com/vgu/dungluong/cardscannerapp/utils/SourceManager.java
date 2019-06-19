package com.vgu.dungluong.cardscannerapp.utils;

import com.vgu.dungluong.cardscannerapp.model.local.Corners;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

/**
 * Created by Dung Luong on 19/06/2019
 */
public class SourceManager {

    @Nullable
    private Mat pic;
    @Nullable
    private  Corners corners;
    @Nullable
    private Size size;
    @NotNull
    private final Point defaultTl = new Point(180.0D, 320.0D);
    @NotNull
    private final Point defaultTr = new Point(900.0D, 320.0D);
    @NotNull
    private final Point defaultBr = new Point(900.0D, 1600.0D);
    @NotNull
    private final Point defaultBl = new Point(180.0D, 1600.0D);

    private static SourceManager instance;

    private SourceManager(){

    }

    public static SourceManager getInstance(){
        if(instance == null){
            instance = new SourceManager();
        }
        return instance;
    }

    public Mat getPic() {
        return pic;
    }

    public void setPic(Mat pic) {
        this.pic = pic;
    }

    public Corners getCorners() {
        return corners;
    }

    public void setCorners(Corners corners) {
        this.corners = corners;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Point getDefaultTl() {
        return defaultTl;
    }

    public Point getDefaultTr() {
        return defaultTr;
    }

    public Point getDefaultBr() {
        return defaultBr;
    }

    public Point getDefaultBl() {
        return defaultBl;
    }
}
