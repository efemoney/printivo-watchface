package com.devone.watchface.printivo.typeface;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

/**
 * Created by Efe on 04/07/2016.
 */
public class PrintivoLogoSpan extends ReplacementSpan {

    private static final String template = "i";

    private int[] colors = new int[4];

    private float height;
    private float width;

    private int widthTotal;
    private int heightTotal;

    Paint colorPaint;

    public PrintivoLogoSpan(int base, int yellow, int pink, int blue) {

        colors = new int[]{base, yellow, pink, blue};

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
        heightTotal = rect.height();

        widthTotal = (int) paint.measureText(template);

        return widthTotal;
    }

    @Override
    public void draw(Canvas canvas,
                     CharSequence text, int start, int end,
                     float x, int top,
                     int y, int bottom,
                     Paint paint) {

        RectF rectf = new RectF();

        // Reducing width slightly (-1) because it looks better
        // Comment this decrement to see the difference
        width--;

        float dx = (widthTotal - width) / 2f;
        float dy = height / 4;

        float l, t, r, b, cx, cy;

        // In java primitives are copied to method params so changing x, top etc
        // here doesnt change the value for the caller of this method
        x += dx;

        for (int i = 0; i < colors.length; i++) {

            colorPaint.setColor(colors[i]);

            l = x;
            t = y - dy * (i + 1);
            r = x + width;
            b = y - dy * i;

            rectf.set(l, t, r, b);

            canvas.drawRect(rectf, colorPaint);
        }

        cx = x + width / 2;
        cy = y - heightTotal + width/2;

        colorPaint.setColor(colors[0]);
        canvas.drawCircle(cx, cy, width/2, colorPaint);
    }

}
