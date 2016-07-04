/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.devone.watchface.printivo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.widget.Toast;

import com.devone.watchface.printivo.typeface.AMPMSpan;
import com.devone.watchface.printivo.typeface.Font;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.support.wearable.watchface.WatchFaceStyle.PEEK_OPACITY_MODE_OPAQUE;
import static android.support.wearable.watchface.WatchFaceStyle.PROTECT_HOTWORD_INDICATOR;
import static android.support.wearable.watchface.WatchFaceStyle.PROTECT_STATUS_BAR;
import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.TOP;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class WatchFaceServive extends CanvasWatchFaceService {

    private static final String REGULAR = "Myriad-Pro";
    private static final String BOLD = "Myriad-Pro-Bold";
    private static final String SEMIBOLD = "Myriad-Pro-Semibold";

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;


    @Override
    public Engine onCreateEngine() {

        return new Engine();
    }


    private static class EngineHandler extends Handler {
        private final WeakReference<WatchFaceServive.Engine> mWeakReference;

        public EngineHandler(WatchFaceServive.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {

            WatchFaceServive.Engine engine = mWeakReference.get();

            if (engine != null && msg.what == MSG_UPDATE_TIME) engine.handleUpdateTimeMessage();
        }
    }


    private class Engine extends CanvasWatchFaceService.Engine {

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;
        boolean mAmbient;
        boolean mRegisteredTimeZoneReceiver = false;

        final Handler mUpdateTimeHandler = new EngineHandler(this);

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                calendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };


        Calendar calendar;

        Paint backgroundPaint;
        Paint decorPaint;
        TextPaint datePaint;
        TextPaint timePaint;
        TextPaint logoPaint;

        float ampmTextSize;

        float width;
        float height;
        float scaled;

        float dateOffsetX;
        float timeOffsetX;
        float logoOffsetX;

        // The y-offsets are the offset of the baseline of the text
        float dateOffsetY;
        float timeOffsetY;
        float logoOffsetY;

        float decorCentreX;
        float decorCentreY;
        float decorRadius1;
        float decorRadius2;
        float decorRadius3;
        float decorRadius4;

        @ColorInt int alt;
        @ColorInt int amI;
        @ColorInt int amP;
        @ColorInt int base;
        @ColorInt int yellow;
        @ColorInt int pink;
        @ColorInt int blue;

        private Paint debugPaint;
        private float[] debugLines;
        private boolean DEBUG = false;


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFaceServive.this)

                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)

                    .setHotwordIndicatorGravity(TOP | CENTER_HORIZONTAL)

                    .setStatusBarGravity(TOP | CENTER_HORIZONTAL)

                    .setPeekOpacityMode(PEEK_OPACITY_MODE_OPAQUE)

                    .setViewProtectionMode(PROTECT_STATUS_BAR | PROTECT_HOTWORD_INDICATOR)

                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)

                    .setShowSystemUiTime(false)

                    .setAcceptsTapEvents(true)

                    .build())
            ;


            Resources resources = WatchFaceServive.this.getResources();

            width = resources.getDisplayMetrics().widthPixels;
            height = resources.getDisplayMetrics().heightPixels;

            scaled = resources.getDisplayMetrics().scaledDensity
                    / resources.getDisplayMetrics().density;

            retrieveColors();

            initBgResources();
            initDateResources();
            initTimeResources();
            initLogoResources();
            initDecorResources();

            calculateOffsets();

            calendar = Calendar.getInstance();
            ampmTextSize = scaled * width / 10;

            if (!mAmbient && DEBUG) initDebugResources();
        }

        private void retrieveColors() {
            amI = ContextCompat.getColor(WatchFaceServive.this, R.color.amI);
            amP = ContextCompat.getColor(WatchFaceServive.this, R.color.amP);
            alt = ContextCompat.getColor(WatchFaceServive.this, R.color.alt);
            base = ContextCompat.getColor(WatchFaceServive.this, R.color.base);
            yellow = ContextCompat.getColor(WatchFaceServive.this, R.color.yellow);
            pink = ContextCompat.getColor(WatchFaceServive.this, R.color.pink);
            blue = ContextCompat.getColor(WatchFaceServive.this, R.color.blue);
        }

        private void calculateOffsets() {
            dateOffsetX = width / 8;
            timeOffsetX = width / 10;
            logoOffsetX = width / 8;

            dateOffsetY = height / 4;

            Paint.FontMetrics fm;

            fm = datePaint.getFontMetrics();
            float dateHeight = fm.descent - fm.top;
            timeOffsetY = dateOffsetY + dateHeight + width / 40;

            fm = timePaint.getFontMetrics();
            float timeHeight = - fm.top;
            logoOffsetY = timeOffsetY + timeHeight;
        }

        private void initDebugResources() {
            debugPaint = new Paint();
            debugPaint.setAntiAlias(true);
            debugPaint.setColor(Color.RED);

            debugLines = new float[]{
                    0, dateOffsetY, width, dateOffsetY,
                    0, datePaint.getFontSpacing(), width, datePaint.getFontSpacing(),
                    0, timeOffsetY, width, timeOffsetY,
                    0, timePaint.getFontSpacing(), width, timePaint.getFontSpacing(),
                    0, logoOffsetY, width, logoOffsetY,
                    0, logoPaint.getFontSpacing(), width, logoPaint.getFontSpacing()
            };
        }

        private void initBgResources() {
            backgroundPaint = new Paint();
            backgroundPaint.setAntiAlias(true);
        }

        private void initDateResources() {

            Typeface regular = Font.get(WatchFaceServive.this, REGULAR);
            float dateTextSize = scaled * width / 16;

            datePaint = new TextPaint();
            datePaint.setAntiAlias(true);
            datePaint.setColor(alt);
            datePaint.setTypeface(regular);
            datePaint.setTextSize(dateTextSize);
        }

        private void initTimeResources() {

            Typeface semibold = Font.get(WatchFaceServive.this, SEMIBOLD);
            float timeTextSize = scaled * width / 5;

            timePaint = new TextPaint();
            timePaint.setAntiAlias(true);
            timePaint.setColor(alt);
            timePaint.setTypeface(semibold);
            timePaint.setTextSize(timeTextSize);
        }

        private void initLogoResources() {

            Typeface bold = Font.get(WatchFaceServive.this, BOLD);
            float timeTextSize = scaled * (width * 3 / 20);

            logoPaint = new TextPaint();
            logoPaint.setAntiAlias(true);
            logoPaint.setColor(base);
            logoPaint.setTypeface(bold);
            logoPaint.setTextSize(timeTextSize);
        }

        private void initDecorResources() {
            decorPaint = new Paint();
            decorPaint.setAntiAlias(true);
            // Decor sizes are initialized in onApplyWindowInsets
        }


        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                calendar.setTimeZone(TimeZone.getDefault());
                invalidate();

            } else {

                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }


        private void registerReceiver() {

            if (mRegisteredTimeZoneReceiver) return;

            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            WatchFaceServive.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {

            if (!mRegisteredTimeZoneReceiver) return;

            mRegisteredTimeZoneReceiver = false;
            WatchFaceServive.this.unregisterReceiver(mTimeZoneReceiver);
        }


        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            boolean isRound = insets.isRound();

            decorCentreX = isRound ? width / 8 : 0;
            decorCentreY = isRound ? height - (height / 16) : height;


            decorRadius1 = width * 3 / 16;
            float decrement = width / (isRound ? 40 : 32);

            decorRadius2 = decorRadius1 - decrement;
            decorRadius3 = decorRadius2 - decrement;
            decorRadius4 = decorRadius3 - decrement;
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (mAmbient != inAmbientMode) {

                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    backgroundPaint.setAntiAlias(!inAmbientMode);
                    decorPaint.setAntiAlias(!inAmbientMode);
                    datePaint.setAntiAlias(!inAmbientMode);
                    timePaint.setAntiAlias(!inAmbientMode);
                    logoPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {

            switch (tapType) {

                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;

                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;

                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                    Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            invalidate();
        }


        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            calendar.setTimeInMillis(System.currentTimeMillis());

            // Draw the background.
            canvas.drawColor(mAmbient ? Color.BLACK : Color.WHITE);

            if (!mAmbient && DEBUG) debugDrawLines(canvas);

            // For all text it is assumed that the specified y offset is at their ascents
            // All drawing translates the canvas/ origin with this in mind
            drawDate(canvas);
            drawTime(canvas);
            drawLogo(canvas);

            drawDecor(canvas);
        }

        private void debugDrawLines(Canvas canvas) {

            float val = datePaint.getFontSpacing();

            canvas.drawLine(0, dateOffsetY, width, dateOffsetY, debugPaint);
            canvas.drawLine(0, dateOffsetY + val, width, dateOffsetY + val, debugPaint);

            val = timePaint.getFontSpacing();

            canvas.drawLine(0, timeOffsetY, width, timeOffsetY, debugPaint);
            canvas.drawLine(0, timeOffsetY + val, width, timeOffsetY + val, debugPaint);

            val = logoPaint.getFontSpacing();

            canvas.drawLine(0, logoOffsetY, width, logoOffsetY, debugPaint);
            canvas.drawLine(0, logoOffsetY + val, width, logoOffsetY + val , debugPaint);

            // Only here so I can attach a break point LMAO
            if (DEBUG);
        }

        private void drawDate(Canvas canvas) {

            if (mAmbient) return;

            Locale def = Locale.getDefault();

            String date = String.format(def, "%02d %s",
                    calendar.get(Calendar.DATE),
                    calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, def)
            );

            // The y offset represents the top so we offset the offset back to the baseline
            Paint.FontMetrics fm = datePaint.getFontMetrics();
            canvas.drawText(date, dateOffsetX, dateOffsetY + -fm.top, datePaint);
        }

        private void drawTime(Canvas canvas) {

            timePaint.setColor(mAmbient ? Color.WHITE : alt);

            Locale def = Locale.getDefault();

            // Draw
            // H:MM in ambient mode
            // H:MM:SS in interactive mode.
            String time = mAmbient ?
                    String.format(def, "%d:%02d",
                            calendar.get(Calendar.HOUR),
                            calendar.get(Calendar.MINUTE)
                    ) :
                    String.format(def, "%d:%02d:%02d",
                            calendar.get(Calendar.HOUR),
                            calendar.get(Calendar.MINUTE),
                            calendar.get(Calendar.SECOND)
                    );
            // Get AM/PM
            String ampm = calendar.getDisplayName(Calendar.AM_PM, Calendar.LONG, def);


            Typeface typeface = Font.get(WatchFaceServive.this, REGULAR);

            canvas.save();

            Paint.FontMetrics fm = timePaint.getFontMetrics();
            canvas.translate(timeOffsetX, timeOffsetY);

            SpannableStringBuilder text = new SpannableStringBuilder(time);
            text.append(ampm, new AMPMSpan(typeface, ampmTextSize), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            StaticLayout layout = getStaticLayout(canvas.getWidth(), text);

            layout.draw(canvas);
            canvas.restore();

        }

        private void drawLogo(Canvas canvas) {

            logoPaint.setColor(mAmbient ? amP : base);
            String logo = "printivo";

            Paint.FontMetrics fm = logoPaint.getFontMetrics();
            canvas.drawText(logo, logoOffsetX, logoOffsetY + -fm.ascent, logoPaint);
        }

        private void drawDecor(Canvas canvas) {

            if (mAmbient) return;

            decorPaint.setColor(blue);
            canvas.drawCircle(decorCentreX, decorCentreY, decorRadius1, decorPaint);
            decorPaint.setColor(pink);
            canvas.drawCircle(decorCentreX, decorCentreY, decorRadius2, decorPaint);
            decorPaint.setColor(yellow);
            canvas.drawCircle(decorCentreX, decorCentreY, decorRadius3, decorPaint);
            decorPaint.setColor(base);
            canvas.drawCircle(decorCentreX, decorCentreY, decorRadius4, decorPaint);
        }

        private StaticLayout getStaticLayout(int canvasWidth, CharSequence text) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                return StaticLayout.Builder.obtain(text, 0, text.length(), timePaint, canvasWidth)
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(0f, 1f)
                        .setIncludePad(false)
                        .build()
                ;

            } else {

                return new StaticLayout(text, timePaint,
                        canvasWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
            }
        }


        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {

            invalidate();

            if (shouldTimerBeRunning()) {

                long timeMs = System.currentTimeMillis();

                // Get the number of milliseconds until the next second as use as the delay
                long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);

                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }
}
