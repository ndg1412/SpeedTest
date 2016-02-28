package com.netvisiontel.speedtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by ngodi on 2/25/2016.
 */
public class MeterView extends RelativeLayout {
    ImageView ivGauge, ivArrow;

    public MeterView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.meter, this);
    }

    public MeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.meter, this);
    }

    public MeterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.meter, this);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Path circle;
        circle = new Path();
        circle.addCircle(100, 100, 200, Path.Direction.CW);

        Paint cPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cPaint.setStyle(Paint.Style.STROKE);
        cPaint.setColor(Color.WHITE);
        cPaint.setStrokeWidth(3);
        Paint tPaint;
        tPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        tPaint.setColor(Color.WHITE);
        tPaint.setTextSize(20f);
        String QUOTE = "This is a test. This is a demo.";
        canvas.drawTextOnPath(QUOTE, circle, 0, 20, tPaint);
        canvas.drawCircle(150, 150, 100, cPaint);


    }
}
