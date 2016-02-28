package com.netvisiontel.speedtest.wiget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.netvisiontel.speedtest.R;
import com.netvisiontel.speedtest.util.Config;

/**
 * Created by Giang on 2/25/2016.
 */
public class SpeedView extends View {
    String TAG = "SpeedView";
    Bitmap bGauge, bArrow;
    float fDensity;
    float fAngle = -120f;

    public SpeedView(Context context) {
        super(context);
        fDensity = getResources().getDisplayMetrics().density;
        Log.d(TAG, String.format("fDensity: %f, width: %d, heigh: %d", fDensity, getWidth(), getHeight()));
        bGauge = BitmapFactory.decodeResource(context.getResources(), R.drawable.gauge_bg);
        bArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.gauge_ic_arrow);
    }

    public SpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        fDensity = getResources().getDisplayMetrics().density;
        Log.d(TAG, String.format("fDensity: %f, width: %d, heigh: %d", fDensity, getWidth(), getHeight()));
        bGauge = BitmapFactory.decodeResource(context.getResources(), R.drawable.gauge_bg);
        bArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.gauge_ic_arrow);
    }

    public SpeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        fDensity = getResources().getDisplayMetrics().density;
        Log.d(TAG, String.format("fDensity: %f, width: %d, heigh: %d", fDensity, getWidth(), getHeight()));
        bGauge = BitmapFactory.decodeResource(context.getResources(), R.drawable.gauge_bg);
        bArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.gauge_ic_arrow);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bGauge, 0, 0, null);

        int gw = bGauge.getWidth();
        int gh = bGauge.getHeight();
        int aw = bArrow.getWidth();
        int centreX = (gw  - aw) /2;

        DrawText_Unit(canvas, gw / 2, gh / 2 + 27 * fDensity, Config.RADIUS * fDensity);
        canvas.rotate(fAngle, gw / 2, gh / 2 + 27 * fDensity);
        canvas.drawBitmap(bArrow, centreX, 24 * fDensity, null);
        canvas.rotate(-fAngle, gw / 2, gh / 2 + 27 * fDensity);;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.v(TAG, "w = " + MeasureSpec.getSize(widthMeasureSpec) + "  h = " + MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(bGauge.getWidth(), bGauge.getHeight());
    }

    public void DrawText_Unit(Canvas canvas, float cx, float cy, float r) {
        Paint tPaint;
        tPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        tPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        tPaint.setColor(Color.WHITE);
        tPaint.setTextSize(40f);
        float tmp0_x = (float) (cx - r*Math.cos(Math.toRadians(30)));
        float tmp0_y = (float) (cy + r*Math.sin(Math.toRadians(30)));
        canvas.drawText("0", tmp0_x, tmp0_y + 10, tPaint);
        float tmp1_x = (float) (cx - r);
        float tmp1_y = cy;
        canvas.drawText("100M", tmp1_x, tmp1_y +10, tPaint);

        float tmp2_x = (float) (cx - r*Math.cos(Math.toRadians(30)));
        float tmp2_y = (float) (cy - r*Math.sin(Math.toRadians(30)));
        canvas.drawText("200M", tmp2_x, tmp2_y + 10, tPaint);

        float tmp3_x = (float) (cx - r*Math.cos(Math.toRadians(60)));
        float tmp3_y = (float) (cy - r*Math.sin(Math.toRadians(60)));
        canvas.drawText("300M", tmp3_x - 30, tmp3_y + 30, tPaint);

        float tmp4_x = (float) (cx);
        float tmp4_y = (float) (cy - r);
        canvas.drawText("400M", tmp4_x - 50, tmp4_y + 30, tPaint);

        float tmp5_x = (float) (cx + r*Math.sin(Math.toRadians(30)));
        float tmp5_y = (float) (cy - r*Math.cos(Math.toRadians(30)));
        canvas.drawText("500M", tmp5_x - 50, tmp5_y + 30, tPaint);

        float tmp6_x = (float) (cx + r*Math.sin(Math.toRadians(60)));
        float tmp6_y = (float) (cy - r*Math.cos(Math.toRadians(60)));
        canvas.drawText("600M", tmp6_x - 50, tmp6_y + 30, tPaint);

        float tmp7_x = (float) (cx + r);
        float tmp7_y = (float) (cy);
        canvas.drawText("700M", tmp7_x - 95, tmp7_y + 15, tPaint);

        float tmp8_x = (float) (cx + r*Math.cos(Math.toRadians(30)));
        float tmp8_y = (float) (cy + r*Math.sin(Math.toRadians(30)));
        canvas.drawText("800M", tmp8_x - 95, tmp8_y + 10, tPaint);

    }

    public static Bitmap RotateBitmap(Bitmap source, float cx, float cy, float angle) {
        Matrix matrix = new Matrix();
        matrix.setRotate(angle, cx, cy);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void setValue(float val) {
        fAngle = val*0.3f - 120;
        invalidate();
    }
}
