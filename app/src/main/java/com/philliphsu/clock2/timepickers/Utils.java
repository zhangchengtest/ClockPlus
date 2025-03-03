/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.philliphsu.clock2.timepickers;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.text.format.Time;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.philliphsu.clock2.R;

import java.util.Calendar;

/**
 * Utility helper functions for time and date pickers.
 */
public class Utils {

    public static final int MONDAY_BEFORE_JULIAN_EPOCH = Time.EPOCH_JULIAN_DAY - 3;
    public static final int PULSE_ANIMATOR_DURATION = 544;

    // Alpha level for time picker selection.
    public static final int SELECTED_ALPHA = 255;
    public static final int SELECTED_ALPHA_THEME_DARK = 255;
    // Alpha level for fully opaque.
    public static final int FULL_ALPHA = 255;

    static final String SHARED_PREFS_NAME = "com.android.calendar_preferences";

    public static boolean isJellybeanOrLater() {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * Try to speak the specified text, for accessibility. Only available on JB or later.
     * @param text Text to announce.
     */
    @SuppressLint("NewApi")
    public static void tryAccessibilityAnnounce(View view, CharSequence text) {
        if (isJellybeanOrLater() && view != null && text != null) {
            view.announceForAccessibility(text);
        }
    }

    public static int getDaysInMonth(int month, int year) {
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                return 31;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                return 30;
            case Calendar.FEBRUARY:
                return (year % 4 == 0) ? 29 : 28;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
    }

    /**
     * Takes a number of weeks since the epoch and calculates the Julian day of
     * the Monday for that week.
     *
     * This assumes that the week containing the {@link Time#EPOCH_JULIAN_DAY}
     * is considered week 0. It returns the Julian day for the Monday
     * {@code week} weeks after the Monday of the week containing the epoch.
     *
     * @param week Number of weeks since the epoch
     * @return The julian day for the Monday of the given week since the epoch
     */
    public static int getJulianMondayFromWeeksSinceEpoch(int week) {
        return MONDAY_BEFORE_JULIAN_EPOCH + week * 7;
    }

    /**
     * Returns the week since {@link Time#EPOCH_JULIAN_DAY} (Jan 1, 1970)
     * adjusted for first day of week.
     *
     * This takes a julian day and the week start day and calculates which
     * week since {@link Time#EPOCH_JULIAN_DAY} that day occurs in, starting
     * at 0. *Do not* use this to compute the ISO week number for the year.
     *
     * @param julianDay The julian day to calculate the week number for
     * @param firstDayOfWeek Which week day is the first day of the week,
     *          see {@link Time#SUNDAY}
     * @return Weeks since the epoch
     */
    public static int getWeeksSinceEpochFromJulianDay(int julianDay, int firstDayOfWeek) {
        int diff = Time.THURSDAY - firstDayOfWeek;
        if (diff < 0) {
            diff += 7;
        }
        int refDay = Time.EPOCH_JULIAN_DAY - diff;
        return (julianDay - refDay) / 7;
    }

    /**
     * Render an animator to pulsate a view in place.
     * @param labelToAnimate the view to pulsate.
     * @return The animator object. Use .start() to begin.
     */
    public static ObjectAnimator getPulseAnimator(View labelToAnimate, float decreaseRatio,
            float increaseRatio) {
        Keyframe k0 = Keyframe.ofFloat(0f, 1f);
        Keyframe k1 = Keyframe.ofFloat(0.275f, decreaseRatio);
        Keyframe k2 = Keyframe.ofFloat(0.69f, increaseRatio);
        Keyframe k3 = Keyframe.ofFloat(1f, 1f);

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofKeyframe("scaleX", k0, k1, k2, k3);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe("scaleY", k0, k1, k2, k3);
        ObjectAnimator pulseAnimator =
                ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, scaleX, scaleY);
        pulseAnimator.setDuration(PULSE_ANIMATOR_DURATION);

        return pulseAnimator;
    }

    /**
     * Gets the colorAccent from the current context, if possible/available
     * @param context The context to use as reference for the color
     * @return the accent color of the current context
     */
    public static int getThemeAccentColor(Context context) {
        // Source from MDTP
//        TypedValue typedValue = new TypedValue();
//        // First, try the android:colorAccent
//        if (Build.VERSION.SDK_INT >= 21) {
//            context.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
//            return typedValue.data;
//        }
//        // Next, try colorAccent from support lib
//        int colorAccentResId = context.getResources().getIdentifier("colorAccent", "attr", context.getPackageName());
//        if (colorAccentResId != 0 && context.getTheme().resolveAttribute(colorAccentResId, typedValue, true)) {
//            return typedValue.data;
//        }
//
//        return ContextCompat.getColor(context, R.color.accent_color);
        return getColorFromThemeAttr(context, R.attr.colorAccent);
    }

    public static int getTextColorFromThemeAttr(Context context, int resid) {
        // http://stackoverflow.com/a/33839580/5055032
//        final TypedValue value = new TypedValue();
//        context.getTheme().resolveAttribute(resid, value, true);
//        TypedArray a = context.obtainStyledAttributes(value.data,
//                new int[] {resid});
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[] {resid});
        final int color = a.getColor(0/*index*/, 0/*defValue*/);
        a.recycle();
        return color;
        // Didn't work! Gave me white!
//        return getColorFromThemeAttr(context, android.R.attr.textColorPrimary);
    }

    /**
     * @param resId The resource identifier of the desired theme attribute.
     */
    public static int getColorFromThemeAttr(Context context, int resId) {
        // http://stackoverflow.com/a/28777489/5055032
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(resId, value, true);
        return value.data;
    }

    /**
     * Mutates the given drawable, applies the specified tint list, and sets this tinted
     * drawable on the target ImageView.
     *
     * @param target the ImageView that should have the tinted drawable set on
     * @param drawable the drawable to tint
     * @param tintList Color state list to use for tinting this drawable, or null to clear the tint
     */
    public static void setTintList(ImageView target, Drawable drawable, ColorStateList tintList) {
        // TODO: What is the VectorDrawable counterpart for this process?
        // Use a mutable instance of the drawable, so we only affect this instance.
        // This is especially useful when you need to modify properties of drawables loaded from
        // resources. By default, all drawables instances loaded from the same resource share a
        // common state; if you modify the state of one instance, all the other instances will
        // receive the same modification.
        // Wrap drawable so that it may be used for tinting across the different
        // API levels, via the tinting methods in this class.
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintList(drawable, tintList);
        target.setImageDrawable(drawable);
    }

    public static void setTint(Drawable drawable, @ColorInt int color) {
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTint(drawable, color);
    }

    /**
     * Returns a tinted drawable from the given drawable resource, if {@code tintList != null}.
     * Otherwise, no tint will be applied.
     */
    public static Drawable getTintedDrawable(@NonNull Context context,
                                             @DrawableRes int drawableRes,
                                             @Nullable ColorStateList tintList) {
        Drawable d = DrawableCompat.wrap(ContextCompat.getDrawable(context, drawableRes).mutate());
        DrawableCompat.setTintList(d, tintList);
        return d;
    }

    /**
     * Returns a tinted drawable from the given drawable resource and color resource.
     */
    public static Drawable getTintedDrawable(@NonNull Context context,
                                             @DrawableRes int drawableRes,
                                             @ColorInt int colorInt) {
        Drawable d = DrawableCompat.wrap(ContextCompat.getDrawable(context, drawableRes).mutate());
        DrawableCompat.setTint(d, colorInt);
        return d;
    }

    /**
     * Sets the color on the {@code view}'s {@code selectableItemBackground} or the
     * borderless variant, whichever was set as the background.
     * @param view the view that should have its highlight color changed
     */
    public static void setColorControlHighlight(@NonNull View view, @ColorInt int color) {
        Drawable selectableItemBackground = view.getBackground();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && selectableItemBackground instanceof RippleDrawable) {
            ((RippleDrawable) selectableItemBackground).setColor(ColorStateList.valueOf(color));
        } else {
            // Draws the color (src) onto the background (dest) *in the same plane*.
            // That means the color is not overlapping (i.e. on a higher z-plane, covering)
            // the background. That would be done with SRC_OVER.
            // The DrawableCompat tinting APIs *could* be a viable alternative, if you
            // call setTintMode(). Previous attempts using those APIs failed without
            // the tint mode. However, those APIs have the overhead of mutating and wrapping
            // the drawable.
            selectableItemBackground.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    /**
     * Gets dialog type (Light/Dark) from current theme
     * @param context The context to use as reference for the boolean
     * @param current Default value to return if cannot resolve the attribute
     * @return true if dark mode, false if light.
     */
    public static boolean isDarkTheme(Context context, boolean current) {
        return resolveBoolean(context, R.attr.themeDark, current);
    }

    /**
     * Gets the required boolean value from the current context, if possible/available
     * @param context The context to use as reference for the boolean
     * @param attr Attribute id to resolve
     * @param fallback Default value to return if no value is specified in theme
     * @return the boolean value from current theme
     */
    private static boolean resolveBoolean(Context context, @AttrRes int attr, boolean fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getBoolean(0, fallback);
        } finally {
            a.recycle();
        }
    }
}
