package io.doist.material.widget.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import io.doist.material.reflection.ReflectionUtils;
import io.doist.material.res.MaterialResources;

public class MaterialWidgetHandler {
    private static final boolean sSkip = Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT;

    final private static Class<?> StyleableClass = ReflectionUtils.getClass("com.android.internal.R$styleable");

    private static int[] sOriginalViewStyleable;
    private static int[] sOriginalImageViewStyleable;

    public static AttributeSet hideStyleableAttributes(AttributeSet set, int... attrs) {
        if (sSkip) return set;

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
            }
        }
        return set;
    }

    public static void restoreStyleableAttributes(int... attrs) {
        if (sSkip) return;

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
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static void init(View view, AttributeSet set, int defStyle, int[] attrs) {
        if (sSkip) return;

        final Context context = view.getContext();
        final Resources resources = view.getResources();

        final TypedArray ta = context.obtainStyledAttributes(set, attrs, defStyle, 0);
        try {
            for (int i = 0; i < attrs.length; i++) {
                final int resId = ta.getResourceId(i, 0);
                if (resId != 0) {
                    final MaterialResources r = MaterialResources.getInstance(context, resources);
                    final Drawable d = r.getDrawable(resId);

                    if (d != null) {
                        final int attr = attrs[i];
                        switch (attr) {
                            case android.R.attr.background:
                                final int paddingLeft = view.getPaddingLeft();
                                final int paddingTop = view.getPaddingTop();
                                final int paddingRight = view.getPaddingRight();
                                final int paddingBottom = view.getPaddingBottom();

                                // Init background.
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                    view.setBackgroundDrawable(d);
                                } else {
                                    view.setBackground(d);
                                }

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
                        }
                    }
                }
            }

        }
        finally {
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
}
