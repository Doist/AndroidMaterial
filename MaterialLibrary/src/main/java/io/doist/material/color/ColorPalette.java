package io.doist.material.color;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;

import io.doist.material.R;

public class ColorPalette {
    public static int resolveAccentColor(Context context) {
        TypedValue value = new TypedValue();
        int colorAccentAttrResId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                              android.R.attr.colorAccent :
                              R.attr.colorAccent;
        return context.getTheme().resolveAttribute(colorAccentAttrResId, value, true) ?
               value.data :
               context.getResources().getColor(R.color.accent_material_light); // TODO: Make this theme dependent.
    }
}
