package com.vgu.dungluong.cardscannerapp.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;

import com.vgu.dungluong.cardscannerapp.utils.AppLogger;

/**
 * Created by Dung Luong on 22/06/2019
 */
public class PreviewSurfaceView extends SurfaceView {

    private CameraPreview camPreview;

    private boolean listenerSet = false;

    public Paint mPaint;

    private DrawingView drawingView;

    private boolean drawingViewSet = false;

    public PreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!listenerSet) {
            return false;
        }

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            performClick();
            float x = event.getX();
            float y = event.getY();

            Rect touchRect = new Rect(
                    (int)(x - 100),
                    (int)(y - 100),
                    (int)(x + 100),
                    (int)(y + 100));

            final Rect targetFocusRect = new Rect(
                    touchRect.left * 2000 / this.getWidth() - 1000,
                    touchRect.top * 2000 / this.getHeight() - 1000,
                    touchRect.right * 2000 / this.getWidth() - 1000,
                    touchRect.bottom * 2000 / this.getHeight() - 1000);

            camPreview.doTouchFocus(targetFocusRect);
            if (drawingViewSet) {
                drawingView.setHaveTouch(true, touchRect);
                drawingView.invalidate();

                // Remove the square after some time
//                Handler handler = new Handler();
//                handler.postDelayed(() -> {
//                    drawingView.setHaveTouch(false, new Rect(0, 0, 0, 0));
//                    drawingView.invalidate();
//                }, 1000);


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

}