package io.doist.material.widget.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.doist.material.res.MaterialResources;

public class MaterialWidgetHandler {
    private static final String TAG = MaterialWidgetHandler.class.getSimpleName();

    private static int[] sViewStyleable;

    public static AttributeSet discardStyleableAttributes(AttributeSet attrs) {
        try {
            Class<?> StyleableClass = Class.forName("com.android.internal.R$styleable");

            Field viewBackgroundField = StyleableClass.getField("View_background");
            viewBackgroundField.setAccessible(true);
            int viewBackground = (int) viewBackgroundField.get(null);

            Field viewStyleableField = StyleableClass.getField("View");
            viewStyleableField.setAccessible(true);
            sViewStyleable = (int[]) viewStyleableField.get(null);
            viewStyleableField.set(null, removeFromArray(sViewStyleable, viewBackground));
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            Log.w(TAG, e);
        }

        return attrs;
    }

    private static void restoreStyleableAttributes() {
        try {
            Class<?> StyleableClass = Class.forName("com.android.internal.R$styleable");

            Field viewStyleableField = StyleableClass.getField("View");
            viewStyleableField.setAccessible(true);
            viewStyleableField.set(null, sViewStyleable);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            Log.w(TAG, e);
        }
    }

    @SuppressWarnings("deprecation")
    public static void init(View view, AttributeSet set, int defStyle) {
        // First restore styleable attributes.
        MaterialWidgetHandler.restoreStyleableAttributes();

        final Context context = view.getContext();
        final Resources resources = view.getResources();

        final int[] attrs = new int[] {android.R.attr.background};
        final TypedArray ta = context.obtainStyledAttributes(set, attrs, defStyle, 0);
        try {
            final int resId = ta.getResourceId(0, 0);
            if (resId != 0) {
                final MaterialResources r = MaterialResources.getInstance(context, resources);
                final Drawable d = r.getDrawable(resId);
                if (d != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        view.setBackgroundDrawable(d);
                    } else {
                        view.setBackground(d);
                    }
                }
            }
        }
        finally {
            ta.recycle();
        }
    }

    private static int[] removeFromArray(int[] array, Integer... remove) {
        array = Arrays.copyOf(array, array.length);

        Set<Integer> removeSet = new HashSet<>(Arrays.asList(remove));

        for (int i = 0, j = 0; i < array.length; i++) {
            array[j++] = !removeSet.contains(i) ? array[i] : 0;
        }

        return array;
    }
}
