package com.devone.watchface.printivo.typeface;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapted from github.com/romannurik/muzei/
 * <p/>
 * Also see https://code.google.com/p/android/issues/detail?id=9904
 */
public class Font {

    private static final Map<String, Typeface> cache = new HashMap<String, Typeface>();

    private Font() {}

    public static Typeface get(Context context, @Nullable String font) {

        synchronized (cache) {

            if (!TextUtils.isEmpty(font) && !cache.containsKey(font)) {

                Typeface tf = Typeface.createFromAsset(context.getApplicationContext().getAssets(),
                        "fonts/" + font + ".ttf");

                cache.put(font, tf);
            }

            return cache.get(font);
        }
    }
}

