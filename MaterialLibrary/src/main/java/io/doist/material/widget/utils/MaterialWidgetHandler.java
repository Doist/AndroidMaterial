package io.doist.material.widget.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;

import io.doist.material.R;
import io.doist.material.reflection.ReflectionUtils;
import io.doist.material.res.MaterialResources;

public class MaterialWidgetHandler {
    private static final boolean SKIP = Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT;

    private static final Class<?> StyleableClass = ReflectionUtils.getClass("com.android.internal.R$styleable");

    private static int[] sOriginalViewStyleable;
    private static int[] sOriginalImageViewStyleable;
    private static int[] sOriginalTextViewStyleable;

    public static AttributeSet hideStyleableAttributes(AttributeSet set, int... attrs) {
        if (SKIP) {
            return set;
        }

        for (int attr : attrs) {
            switch (attr) {
                case android.R.attr.background:
                    if (sOriginalViewStyleable == null) {
                        // Keep original view styleable values.
                        sOriginalViewStyleable =
                                (int[]) ReflectionUtils.getDeclaredFieldValue(StyleableClass, "View", null);
                    }

                    int viewBackgroundIndex =
                            (int) ReflectionUtils.getDeclaredFieldValue(StyleableClass, "View_background", null);

                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass,
                            "View",
                            null,
                            hideValue(sOriginalViewStyleable, viewBackgroundIndex));
                    break;

                case android.R.attr.src:
                    if (sOriginalImageViewStyleable == null) {
                        // Keep original image view styleable values.
                        sOriginalImageViewStyleable =
                                (int[]) ReflectionUtils.getDeclaredFieldValue(StyleableClass, "ImageView", null);
                    }

                    int imageViewSrcIndex =
                            (int) ReflectionUtils.getDeclaredFieldValue(StyleableClass, "ImageView_src", null);

                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass,
                            "ImageView",
                            null,
                            hideValue(sOriginalImageViewStyleable, imageViewSrcIndex));
                    break;

                case android.R.attr.textCursorDrawable:
                    if (sOriginalTextViewStyleable == null) {
                        // Keep original text view styleable values.
                        sOriginalTextViewStyleable =
                                (int[]) ReflectionUtils.getDeclaredFieldValue(StyleableClass, "TextView", null);
                    }

                    int textViewTextCursorDrawableIndex = (int) ReflectionUtils
                            .getDeclaredFieldValue(StyleableClass, "TextView_textCursorDrawable", null);

                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass,
                            "TextView",
                            null,
                            hideValue(sOriginalTextViewStyleable, textViewTextCursorDrawableIndex));
                    break;
            }
        }
        return set;
    }

    public static void restoreStyleableAttributes(int... attrs) {
        if (SKIP) {
            return;
        }

        for (int attr : attrs) {
            switch (attr) {
                case android.R.attr.background:
                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass, "View", null, sOriginalViewStyleable);
                    break;

                case android.R.attr.src:
                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass, "ImageView", null, sOriginalImageViewStyleable);
                    break;

                case android.R.attr.textCursorDrawable:
                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass, "TextView", null, sOriginalTextViewStyleable);
                    break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static void init(View view, AttributeSet set, int defStyle, int[] attrs) {
        if (SKIP) {
            return;
        }

        final Context context = themifyContext(view.getContext(), set);
        final TypedArray ta = context.obtainStyledAttributes(set, attrs, defStyle, 0);
        try {
            MaterialResources res = MaterialResources.getInstance(context, context.getResources());
            for (int i = 0; i < attrs.length; i++) {
                Drawable d = getDrawable(res, ta, i);
                if (d != null) {
                    final int attr = attrs[i];
                    switch (attr) {
                        case android.R.attr.background:
                            final int paddingLeft = view.getPaddingLeft();
                            final int paddingTop = view.getPaddingTop();
                            final int paddingRight = view.getPaddingRight();
                            final int paddingBottom = view.getPaddingBottom();

                            // Init background.
                            view.setBackground(d);

                            // Maintain horizontal and vertical padding.
                            if (paddingLeft > 0 && paddingRight > 0) {
                                view.setPadding(
                                        paddingLeft,
                                        view.getPaddingTop(),
                                        paddingRight,
                                        view.getPaddingBottom());
                            }

                            if (paddingTop > 0 && paddingBottom > 0) {
                                view.setPadding(
                                        view.getPaddingLeft(),
                                        paddingTop,
                                        view.getPaddingRight(),
                                        paddingBottom);
                            }

                            break;

                        case android.R.attr.src:
                            // Init image drawable.
                            if (view instanceof ImageView) {
                                ((ImageView) view).setImageDrawable(d);
                            }
                            break;

                        case android.R.attr.textCursorDrawable:
                            if (view instanceof TextView) {
                                // Replace cursor drawables in TextView's Editor.
                                Object cursorDrawables = ReflectionUtils.getDeclaredFieldValue(
                                        ReflectionUtils.getClass("android.widget.Editor"),
                                        "mCursorDrawable",
                                        ReflectionUtils.getDeclaredFieldValue(TextView.class, "mEditor", view));
                                Array.set(cursorDrawables, 0, d);
                                Array.set(cursorDrawables, 1, d.getConstantState().newDrawable());

                                // Also set TextView#mCursorDrawableRes; if it's 0 Editor skips drawing the cursor.
                                ReflectionUtils.setDeclaredFieldValue(
                                        TextView.class,
                                        "mCursorDrawableRes",
                                        view,
                                        ta.getResourceId(i, 0));
                            }
                            break;
                    }
                }
            }
        } finally {
            ta.recycle();
        }
    }

    private static int[] hideValue(int[] array, int index) {
        final int[] newArray = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = i == index ? 0 : array[i];
        }
        return newArray;
    }

    private static Drawable getDrawable(MaterialResources r, TypedArray ta, int index) {
        if (ta.hasValue(index)) {
            TypedValue v = ta.peekValue(index);
            if (v.type >= TypedValue.TYPE_FIRST_COLOR_INT && v.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                // Color value.
                return r.loadDrawable(v, 0);
            } else {
                // Resource.
                int resId = ta.getResourceId(index, 0);
                if (resId != 0) {
                    return r.getDrawable(resId);
                }
            }
        }
        return null;
    }

    /**
     * Applies {@code android:theme} to {@code context} by wrapping it in a {@link MaterialContextThemeWrapper}.
     */
    public static Context themifyContext(Context context, AttributeSet attrs) {
        if (SKIP) {
            return context;
        }

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.View, 0, 0);
        int themeId = a.getResourceId(R.styleable.View_android_theme, 0);
        a.recycle();

        if (themeId != 0 && (!(context instanceof MaterialContextThemeWrapper)
                || ((MaterialContextThemeWrapper) context).getThemeResId() != themeId)) {
            context = new MaterialContextThemeWrapper(context, themeId);
        }
        return context;
    }
}
