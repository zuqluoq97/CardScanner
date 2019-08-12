package com.vgu.dungluong.cardscannerapp.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CardProcessor;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;

import org.jetbrains.annotations.Nullable;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Dung Luong on 19/06/2019
 */
public class PaperRectangle extends View {

    private Paint rectPaint = new Paint();

    private Paint circlePaint = new Paint();

    private double ratioX = 1.0;

    private double ratioY = 1.0;

    private Point tl = new Point();

    private Point tr = new Point();

    private Point br = new Point();

    private Point bl = new Point();

    private Path path = new Path();

    private Point point2Move = new Point();

    private boolean cropMode = false;

    private float latestDownX = 0.0F;

    private float latestDownY = 0.0F;

    private SourceManager sm;

    public PaperRectangle(Context context) {
        super(context);
        initializeVariable();
    }

    public PaperRectangle(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeVariable();
    }

    public PaperRectangle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeVariable();
    }

    private void initializeVariable(){
        rectPaint.setColor(Color.BLUE);
        rectPaint.setAntiAlias(true);
        rectPaint.setDither(true);
        rectPaint.setStrokeWidth(6.0F);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeJoin(Paint.Join.ROUND);
        rectPaint.setStrokeCap(Paint.Cap.ROUND);
        rectPaint.setPathEffect(new CornerPathEffect(10.0F));

        circlePaint.setColor(Color.LTGRAY);
        circlePaint.setDither(true);
        circlePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth(4.0F);
        circlePaint.setStyle(Paint.Style.STROKE);

        sm = SourceManager.getInstance();
    }

    public void onCornersDetected(Corners corners){
        this.ratioX = corners.getSize().width / getMeasuredWidth();
        this.ratioY = corners.getSize().height / getMeasuredHeight();
        tl = corners.getCorners().get(0) != null ? corners.getCorners().get(0) : new Point();
        tr = corners.getCorners().get(1) != null ? corners.getCorners().get(1) : new Point();
        br = corners.getCorners().get(2) != null ? corners.getCorners().get(2) : new Point();
        bl = corners.getCorners().get(3) != null ? corners.getCorners().get(3) : new Point();
        resize();
        path.reset();
        path.moveTo((float)this.tl.x, (float)this.tl.y);
        path.lineTo((float)this.tr.x, (float)this.tr.y);
        path.lineTo((float)this.br.x, (float)this.br.y);
        path.lineTo((float)this.bl.x, (float)this.bl.y);
        path.close();
        invalidate();
    }

    public void onConrnersNotDetected(){
        path.reset();
        invalidate();
    }

    public void onCorners2Crop(@Nullable Corners corners, @Nullable Size size, int height) {
        cropMode = true;

        int space = 30;
        tl = new Point(space,  space);
        tr = new Point(getWidth() - space, space);
        br = new Point(getWidth() - space, getHeight() - space);
        bl = new Point( space, getHeight() - space);

        if(corners != null){
            if(corners.getCorners() != null) {
                tl = corners.getCorners().get(0);
                tr = corners.getCorners().get(1);
                br = corners.getCorners().get(2);
                bl = corners.getCorners().get(3);
                if (size != null) {
                    this.ratioX = size.width / this.getWidth();
                    if(this.getHeight() == 0) this.ratioY = size.height / height;
                    else this.ratioY = size.height / this.getHeight();
                }
                resize();
            }
        }
        movePoints();
    }

    public List<Point> getCorners2Crop() {
        reverseSize();
        return Arrays.asList(tl, tr, br, bl);
    }

    protected void onDraw(@Nullable Canvas canvas) {
        super.onDraw(canvas);
        if (canvas != null) {
            canvas.drawPath(this.path, this.rectPaint);
        }

        if (this.cropMode) {
            if (canvas != null) {
                canvas.drawCircle((float)this.tl.x, (float)this.tl.y, 20.0F, this.circlePaint);
            }

            if (canvas != null) {
                canvas.drawCircle((float)this.tr.x, (float)this.tr.y, 20.0F, this.circlePaint);
            }

            if (canvas != null) {
                canvas.drawCircle((float)this.bl.x, (float)this.bl.y, 20.0F, this.circlePaint);
            }

            if (canvas != null) {
                canvas.drawCircle((float)this.br.x, (float)this.br.y, 20.0F, this.circlePaint);
            }
        }

    }

    @Override
    public boolean onTouchEvent(@Nullable MotionEvent event) {
        if(!cropMode){
            return false;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                latestDownX = event.getX();
                latestDownY = event.getY();
                calculatePoint2Move(latestDownX, latestDownY);
                break;
            case MotionEvent.ACTION_MOVE:
                point2Move.x = (event.getX() - latestDownX) + point2Move.x;
                point2Move.y = (event.getY() - latestDownY) + point2Move.y;
                movePoints();
                latestDownY = event.getY();
                latestDownX = event.getX();
                break;
        }
        return true;
    }

    private void calculatePoint2Move(float downX, float downY){
        List<Point> points = Arrays.asList(tl, tr, br, bl);
        point2Move = points.stream().min(Comparator.comparing(point -> Math.abs((point.x - downX) * (point.y - downY)))).orElse(tl);
    }

    private void movePoints() {
        AppLogger.i("tl: " + tl.x + " " + tl.y);
        AppLogger.i("tr: " + tr.x + " " + tr.y);
        AppLogger.i("br: " + br.x + " " + br.y);
        AppLogger.i("bl: " + bl.x + " " + bl.y);
        this.path.reset();
        this.path.moveTo((float)this.tl.x, (float)this.tl.y);
        this.path.lineTo((float)this.tr.x, (float)this.tr.y);
        this.path.lineTo((float)this.br.x, (float)this.br.y);
        this.path.lineTo((float)this.bl.x, (float)this.bl.y);
        this.path.close();
        this.invalidate();
    }

    private void resize() {
        this.tl.x /= this.ratioX;
        this.tl.y /= this.ratioY;
        this.tr.x /= this.ratioX;
        this.tr.y /= this.ratioY;
        this.br.x /= this.ratioX;
        this.br.y /= this.ratioY;
        this.bl.x /= this.ratioX;
        this.bl.y /= this.ratioY;
    }

    private void reverseSize() {
        AppLogger.i(ratioX + " " + ratioY);
        this.tl.x *= this.ratioX;
        this.tl.y *= this.ratioY;
        this.tr.x *= this.ratioX;
        this.tr.y *= this.ratioY;
        this.br.x *= this.ratioX;
        this.br.y *= this.ratioY;
        this.bl.x *= this.ratioX;
        this.bl.y *= this.ratioY;
    }

    private int getNavigationBarHeight(Context pContext) {
        Resources resources = pContext.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    private int getStatusBarHeight(Context pContext) {
        Resources resources = pContext.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : 0;
    }

}
