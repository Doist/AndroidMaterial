package io.doist.material.res;

import org.xmlpull.v1.XmlPullParser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.util.LongSparseArray;
import android.util.SparseArray;
import android.util.TypedValue;

import java.lang.ref.WeakReference;

import io.doist.material.drawable.MaterialDrawableUtils;
import io.doist.material.widget.utils.MaterialContextThemeWrapper;

public class MaterialResources {
    private static SparseArray<MaterialResources> sInstances = new SparseArray<>();

    final Object mAccessLock = new Object();
    TypedValue mTmpValue = new TypedValue();

    WeakReference<Context> mApplicationContext = new WeakReference<>(null);
    WeakReference<Context> mContext = new WeakReference<>(null);
    WeakReference<Resources> mResources = new WeakReference<>(null);

    private final LongSparseArray<Drawable.ConstantState>[] mDrawableCache = new LongSparseArray[2];
    private final LongSparseArray<Drawable.ConstantState> mColorDrawableCache = new LongSparseArray<>(0);

    private MaterialResources() {
        mDrawableCache[0] = new LongSparseArray<>();
        mDrawableCache[1] = new LongSparseArray<>();
    }

    public static MaterialResources getInstance(Context context, Resources resources) {
        int themeResId = 0;
        if (context instanceof MaterialContextThemeWrapper) {
            themeResId = ((MaterialContextThemeWrapper) context).getThemeResId();
        }

        MaterialResources instance = sInstances.get(themeResId);
        if (instance == null) {
            instance = new MaterialResources();
            sInstances.put(themeResId, instance);
        }

        // Ensure context.
        if (instance.mApplicationContext.get() == null) {
            instance.mApplicationContext = new WeakReference<>(context.getApplicationContext());
        }

        if (instance.mContext.get() == null) {
            instance.mContext = new WeakReference<>(context);
        }

        // Ensure resources.
        if (instance.mResources.get() == null) {
            instance.mResources = new WeakReference<>(resources);
        }

        return instance;
    }

    public Drawable getDrawable(int id) throws Resources.NotFoundException {
        final Resources r = mResources.get();

        TypedValue value;
        synchronized (mAccessLock) {
            value = mTmpValue;
            if (value == null) {
                value = new TypedValue();
            } else {
                mTmpValue = null;
            }
            r.getValue(id, value, true);
        }
        Drawable res = loadDrawable(value, id);
        synchronized (mAccessLock) {
            if (mTmpValue == null) {
                mTmpValue = value;
            }
        }
        return res;
    }

    @SuppressLint("NewApi")
    public Drawable loadDrawable(TypedValue value, int id) throws Resources.NotFoundException {
        final Context c = mContext.get() != null ? mContext.get() : mApplicationContext.get();
        final Resources r = mResources.get();

        boolean isColorDrawable = false;
        if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            isColorDrawable = true;
        }
        final long key = isColorDrawable ? value.data :
                         (((long) value.assetCookie) << 32) | value.data;

        LongSparseArray<Drawable.ConstantState> drawableCache;
        if (isColorDrawable) {
            drawableCache = mColorDrawableCache;
        } else {
            int layoutDirection = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                                  0 :
                                  r.getConfiguration().getLayoutDirection();

            drawableCache = mDrawableCache[layoutDirection];
        }

        Drawable dr = getCachedDrawable(r, drawableCache, key);

        if (dr != null) {
            return dr;
        }

        // Drawable is not cached.

        if (isColorDrawable) {
            dr = new ColorDrawable(value.data);
        } else {
            if (value.string == null) {
                throw new Resources.NotFoundException("Resource is not a Drawable (color or path): " + value);
            }

            String file = value.string.toString();

            if (file.endsWith(".xml")) {
                try {
                    XmlPullParser parser = r.getXml(id);
                    dr = MaterialDrawableUtils.createFromXml(c, r, parser);
                } catch (Exception e) {
                    Resources.NotFoundException rnf = new Resources.NotFoundException(
                            "File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
                    rnf.initCause(e);
                    throw rnf;
                }
            } else {
                try {
                    dr = r.getDrawable(id);
                } catch (Exception e) {
                    Resources.NotFoundException rnf = new Resources.NotFoundException(
                            "File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
                    rnf.initCause(e);
                    throw rnf;
                }
            }
        }

        if (dr != null) {
            dr.setChangingConfigurations(value.changingConfigurations);
            Drawable.ConstantState cs = dr.getConstantState();
            if (cs != null) {
                synchronized (mAccessLock) {
                    if (isColorDrawable) {
                        mColorDrawableCache.put(key, cs);
                    } else {
                        int layoutDirection = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                                              0 :
                                              r.getConfiguration().getLayoutDirection();

                        mDrawableCache[layoutDirection].put(key, cs);
                    }
                }
            }
        }

        return dr;
    }

    private Drawable getCachedDrawable(Resources r, LongSparseArray<Drawable.ConstantState> drawableCache, long key) {
        synchronized (mAccessLock) {
            Drawable.ConstantState cs = drawableCache.get(key);
            if (cs != null) {
                return cs.newDrawable(r);
            }
        }
        return null;
    }
}
