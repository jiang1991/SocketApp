package com.viatom.socketapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import static com.viatom.socketapp.DataController.dataSrc;

public class OxiView extends View {
    private Paint bPaint;

    private Canvas canvas;

    public int mWidth;
    public int mHeight;
    public int mTop;
    public int mBottom;
    public int mBase;

    private int maxIndex;

    public OxiView(Context context) {
        super(context);
        init(null, 0);
    }

    public OxiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public OxiView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        iniParam();

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
//        int paddingLeft = getPaddingLeft();
//        int paddingTop = getPaddingTop();
//        int paddingRight = getPaddingRight();
//        int paddingBottom = getPaddingBottom();
//
//        int contentWidth = getWidth() - paddingLeft - paddingRight;
//        int contentHeight = getHeight() - paddingTop - paddingBottom;

        this.canvas = canvas;
        drawBkg(canvas);

        drawWave();
    }

    private void drawBkg(Canvas canvas) {
        //
    }

    private void drawWave() {
        Path p = new Path();
        p.moveTo(0, mBase);
        for (int i = 0; i < maxIndex; i++) {

            if (i == DataController.index && i < maxIndex-5) {

                float y = (mBase - (300*dataSrc[i+4]));
//                y = y > mBottom ? mBottom : y;
//                y = y < mTop ? mTop : y;

                p.moveTo(2*(i+4), y);
                i = i+4;
            } else {
                float y1 = mBase - (300*dataSrc[i]);

//                y1 = y1 > mBottom ? mBottom : y1;
//                y1 = y1 < mTop ? mTop : y1;

                p.lineTo(2*i, y1);
            }
        }

        canvas.drawPath(p, bPaint);

//        canvas.drawText("" + DataController.index, 0,100,bPaint);

    }

    private void iniParam() {

        maxIndex = (int) (getWidth() / 2);
//        dataSrc = new byte[maxIndex*2];

        if (dataSrc == null) {
            dataSrc = new float[maxIndex];
        }

        mWidth = getWidth();
        mHeight = getHeight();

        mBase = (mHeight / 2);
        mTop = (int) (mBase - 200);
        mBottom = (int) (mBase + 200);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.OxiView, defStyle, 0);

        a.recycle();

        iniPaint();

    }



    private void iniPaint() {

        bPaint = new Paint();
        bPaint.setTextAlign(Paint.Align.LEFT);
        bPaint.setTextSize(32);
        bPaint.setColor(Color.BLUE);
        bPaint.setStyle(Paint.Style.STROKE);
        bPaint.setStrokeWidth(4.0f);
    }

}
