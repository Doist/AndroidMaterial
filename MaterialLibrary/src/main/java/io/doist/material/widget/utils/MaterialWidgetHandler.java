package io.doist.material.widget.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Field;

import io.doist.material.res.MaterialResources;

public class MaterialWidgetHandler {
    private static final String TAG = MaterialWidgetHandler.class.getSimpleName();

    private static int[] sOriginalViewStyleable;
    private static int[] sOriginalImageViewStyleable;

    public static AttributeSet hideStyleableAttributes(AttributeSet set, int... attrs) {
        try {
            Class<?> StyleableClass = Class.forName("com.android.internal.R$styleable");

            for (int attr : attrs) {
                switch (attr) {
                    case android.R.attr.background:
                        Field viewBackgroundField = StyleableClass.getField("View_background");
                        viewBackgroundField.setAccessible(true);
                        int viewBackground = (int) viewBackgroundField.get(null);

                        Field viewStyleableField = StyleableClass.getField("View");
                        viewStyleableField.setAccessible(true);
                        // Keep original styleable values.
                        if (sOriginalViewStyleable == null) {
                            sOriginalViewStyleable = (int[]) viewStyleableField.get(null);
                        }
                        viewStyleableField.set(null, hideValue(sOriginalViewStyleable, viewBackground));
                        break;

                    case android.R.attr.src:
                        Field imageViewSrcField = StyleableClass.getField("ImageView_src");
                        imageViewSrcField.setAccessible(true);
                        int imageViewSrc = (int) imageViewSrcField.get(null);

                        Field imageViewStyleableField = StyleableClass.getField("ImageView");
                        imageViewStyleableField.setAccessible(true);
                        // keep original styleable values.
                        if (sOriginalImageViewStyleable == null) {
                            sOriginalImageViewStyleable = (int[]) imageViewStyleableField.get(null);
                        }
                        imageViewStyleableField.set(null, hideValue(sOriginalImageViewStyleable, imageViewSrc));
                        break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            Log.w(TAG, e);
        }

        return set;
    }

    public static void restoreStyleableAttributes(int... attrs) {
        try {
            Class<?> StyleableClass = Class.forName("com.android.internal.R$styleable");

            for (int attr : attrs) {
                switch (attr) {
                    case android.R.attr.background:
                        Field viewStyleableField = StyleableClass.getField("View");
                        viewStyleableField.setAccessible(true);
                        viewStyleableField.set(null, sOriginalViewStyleable);
                        break;

                    case android.R.attr.src:
                        Field imageViewStyleableField = StyleableClass.getField("ImageView");
                        imageViewStyleableField.setAccessible(true);
                        imageViewStyleableField.set(null, sOriginalImageViewStyleable);
                        break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            Log.w(TAG, e);
        }
    }

    @SuppressWarnings("deprecation")
    public static void init(View view, AttributeSet set, int defStyle, int[] attrs) {
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
                                // Init background.
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                    view.setBackgroundDrawable(d);
                                } else {
                                    view.setBackground(d);
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
