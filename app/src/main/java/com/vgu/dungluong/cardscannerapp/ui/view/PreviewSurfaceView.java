package com.vgu.dungluong.cardscannerapp.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;

import com.vgu.dungluong.cardscannerapp.utils.AppLogger;

import java.util.Objects;

/**
 * Created by Dung Luong on 22/06/2019
 */
public class PreviewSurfaceView extends SurfaceView {

    private CameraPreview camPreview;

    private boolean listenerSet = false;

    public Paint mPaint;

    private DrawingView drawingView;

    private boolean drawingViewSet = false;

    private int mFocusAreaSize;

    private Matrix mMatrix;

    public PreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);

        mFocusAreaSize = 200;

        mMatrix = new Matrix();
        Matrix matrix = new Matrix();
        matrix.postScale(this.getWidth() / 2000f, this.getHeight() / 2000f);
        matrix.postTranslate(this.getWidth() / 2f, this.getHeight() / 2f);
        matrix.invert(this.mMatrix);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

    /**
     * On each tap event we will calculate focus area and metering area.
     * <p>
     * Metering area is slightly larger as it should contain more info for exposure calculation.
     * As it is very easy to over/under expose
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!listenerSet) {
            return false;
        }

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            performClick();
            float x = event.getX();
            float y = event.getY();

            Rect focusRect = convertToCameraFocusDimension(calculateTapArea(x, y, 1f));
            Rect meteringRect = convertToCameraFocusDimension(calculateTapArea(x, y, 1.5f));

            camPreview.doTouchFocus(focusRect, meteringRect);
            if (drawingViewSet) {
                drawingView.setHaveTouch(true, calculateTapArea(x, y, 1f));
                drawingView.invalidate();

                // Remove the square after 3 second
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    drawingView.setHaveTouch(false, new Rect(0, 0, 0, 0));
                    drawingView.invalidate();
                }, 3000);


            }

        }
        return false;
    }

    @Override
    public boolean performClick() {
        AppLogger.i("Focus start");
        return super.performClick();
    }

    /**
     * set CameraPreview instance for touch focus.
     * @param camPreview - CameraPreview
     */
    public void setListener(CameraPreview camPreview) {
        this.camPreview = camPreview;
        listenerSet = true;
    }

    /**
     * set DrawingView instance for touch focus indication.
     * @param focusView - DrawingView
     */
    public void setDrawingView(DrawingView focusView) {
        drawingView = focusView;
        drawingViewSet = true;
    }

    /**
     * Convert touch position x:y to camera position -1000:-1000 to 1000:1000.
     */
    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(mFocusAreaSize * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, this.getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, this.getHeight() - areaSize);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        mMatrix.mapRect(rectF);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
     //   return new Rect(left, top, left + areaSize, top + areaSize);
    }

    private Rect convertToCameraFocusDimension(Rect originalRect){
        return new Rect(originalRect.left * 2000 / this.getWidth() - 1000,
                originalRect.top * 2000 / this.getHeight() - 1000,
                originalRect.right * 2000 / this.getWidth() - 1000,
                originalRect.bottom * 2000 / this.getHeight() - 1000);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

}