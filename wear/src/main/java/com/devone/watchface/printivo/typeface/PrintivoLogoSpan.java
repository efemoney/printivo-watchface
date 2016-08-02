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

    private int[] colors = new int[4];

    private float xheight;
    private float tightwidth;

    private int totalwidth;
    private int totalheight;

    private Paint colorPaint;

    public PrintivoLogoSpan(int base, int yellow, int pink, int blue) {

        colors = new int[]{base, yellow, pink, blue};

        colorPaint = new Paint();
        colorPaint.setAntiAlias(true);
    }


    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {

        Rect rect = new Rect();

        String i = "i", o = "o";

        paint.getTextBounds(o, 0, 1, rect);
        xheight = rect.height();

        paint.getTextBounds(i, 0, 1, rect);
        tightwidth = rect.width();
        totalheight = rect.height();

        totalwidth = (int) paint.measureText(i);

        return totalwidth;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {

        // TODO Make the drawing respect paints text Align setting

        // should preallocate this rect and reuse for drawing
        RectF rectf = new RectF();

        // Reducing width slightly (-1) because it looks better
        // Comment this decrement to see the difference
        tightwidth--;

        // (x, y) is initialized to the baseline left of the rect that the i glyph will draw in
        // this includes dx which is the space on either side of the i glyph
        float dx = (totalwidth - tightwidth) / 2f;
        float dy = xheight / 4;

        float l, t, r, b, cx, cy;


        // so we offset x to the desired position
        //
        // In java primitives are <b>copied</b> to method params so changing x, top etc
        // here doesnt change the value for the caller of this method
        x += dx;

        // draw the colors
        for (int i = 0; i < colors.length; i++) {

            colorPaint.setColor(colors[i]);

            l = x;
            t = y - dy * (i + 1);
            r = x + tightwidth;
            b = y - dy * i;

            rectf.set(l, t, r, b);

            canvas.drawRect(rectf, colorPaint);
        }

        cx = x + tightwidth / 2;
        cy = y - totalheight + tightwidth / 2;

        // draw the dot
        colorPaint.setColor(colors[0]);
        canvas.drawCircle(cx, cy, tightwidth / 2, colorPaint);
    }

}
