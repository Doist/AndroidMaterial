package io.doist.material.res;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.TypedValue;

/**
 * Helper class for methods of {@link TypedArray} working with {@link MaterialResources}.
 */
public class MaterialTypedArray {
    private MaterialTypedArray() {
    }

    /**
     * @see TypedArray#getDrawable(int)
     */
    @Nullable
    public static Drawable getDrawable(Context context, Resources resources, TypedArray typedArray, int index) {
        final TypedValue value = new TypedValue();
        if (typedArray.getValue(index, value)) {
            if (value.type == TypedValue.TYPE_ATTRIBUTE) {
                throw new RuntimeException("Failed to resolve attribute at index " + index);
            }
            MaterialResources materialResources = MaterialResources.getInstance(context, resources);
            return materialResources.loadDrawable(context, resources, value, value.resourceId);
        }
        return null;
    }
}
