package com.devone.watchface.printivo.typeface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.text.style.ReplacementSpan;

/**
 * Created by Efe on 04/07/2016.
 */
public class PrintivoLogoSpan extends ReplacementSpan {

    private static final String template = "i";

    @ColorInt private int base;
    @ColorInt private int yellow;
    @ColorInt private int pink;
    @ColorInt private int blue;

    private float height;
    private float width;
    private int widthTotal;
    private int heightTotal;

    Paint colorPaint;

    public PrintivoLogoSpan(int base, int yellow, int pink, int blue) {

        this.base = base;
        this.yellow = yellow;
        this.pink = pink;
        this.blue = blue;

        colorPaint = new Paint();
        colorPaint.setAntiAlias(true);
    }


    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {

        Rect rect = new Rect();

        paint.getTextBounds("o", 0, 1, rect);
        height = rect.height();

        paint.getTextBounds("i", 0, 1, rect);
        width = rect.width();


        widthTotal = (int) paint.measureText(template);

        return widthTotal;
    }

    @Override
    public void draw(Canvas canvas,
                     CharSequence text, int start, int end,
                     float x, int top,
                     int y, int bottom,
                     Paint paint) {

        // In java primitives are copied to method params so changing x, top etc
        // here doesnt change the value for the caller of this method

        float dx = (widthTotal - width) / 2f;

        colorPaint.setColor(Color.RED);

        RectF rectf = new RectF(x + dx, y, x + width, y - height);

        canvas.drawRect(rectf, colorPaint);
    }

}
