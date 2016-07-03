package com.devone.watchface.printivo.typeface;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * Created by Efe on 08/02/2016.
 */
public class AMPMSpan extends MetricAffectingSpan {
    private final Typeface typeface;
    private final float size;

    public AMPMSpan(final Typeface typeface, float size) {

        this.typeface = typeface;
        this.size = size;
    }

    @Override
    public void updateDrawState(final TextPaint drawState) {

        apply(drawState);
    }

    @Override
    public void updateMeasureState(final TextPaint paint) {

        apply(paint);
    }

    private void apply(final Paint paint) {

        paint.setTypeface(typeface);
        paint.setTextSize(size);
    }
}
