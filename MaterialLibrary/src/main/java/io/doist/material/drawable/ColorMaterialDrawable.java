package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.doist.material.R;
import io.doist.material.reflection.ReflectionUtils;

public class ColorMaterialDrawable extends ColorDrawable {
    private final WeakReference<Context> mContext;

    ColorMaterialDrawable(Context context) {
        super();
        mContext = new WeakReference<>(context);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        // Don't call super, because it would crash on a theme attribute in android:color.

        Context c = mContext.get();
        if (c == null) {
            return;
        }

        final TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ColorDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
    }

    /**
     * Updates the constant state from the values in the typed array.
     */
    private void updateStateFromTypedArray(TypedArray a) {
        final ColorState state = new ColorState(this);

        int baseColor = a.getColor(R.styleable.ColorDrawable_android_color, state.getBaseColor());
        state.setBaseColor(baseColor);
        state.setUseColor(baseColor);
    }

    /**
     * Helper class to manipulate internal member mColorState.
     */
    private static class ColorState {
        final Class<?> ColorStateClass;
        final Object mState;

        public ColorState(ColorMaterialDrawable receiver) {
            ColorStateClass = ReflectionUtils.getClass(ColorDrawable.class.getName() + "$ColorState");
            mState = ReflectionUtils.getDeclaredFieldValue(ColorDrawable.class, "mState", receiver);
        }

        public int getBaseColor() {
            Object result = ReflectionUtils.getDeclaredFieldValue(ColorStateClass, "mBaseColor", mState);
            return result != null ? (int) result : 0;
        }

        public void setBaseColor(int baseColor) {
            ReflectionUtils.setDeclaredFieldValue(ColorStateClass, "mBaseColor", mState, baseColor);
        }

        public void setUseColor(int useColor) {
            ReflectionUtils.setDeclaredFieldValue(ColorStateClass, "mUseColor", mState, useColor);
        }
    }
}
