package io.doist.material.res;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.LongSparseArray;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import io.doist.material.drawable.MaterialDrawableUtils;
import io.doist.material.reflection.ReflectionUtils;

public class MaterialResources {
    private static ArrayMap<Integer, MaterialResources> sInstances = new ArrayMap<>();
    private static WeakReference<Context> sApplicationContext = new WeakReference<>(null);

    private final Object mAccessLock = new Object();
    private TypedValue mTmpValue = new TypedValue();

    private WeakReference<Context> mContext = new WeakReference<>(null);
    private WeakReference<Resources> mResources = new WeakReference<>(null);
    private WeakHashMap<Resources, MaterialConfiguration> mConfiguration = new WeakHashMap<>();

    private LongSparseArray<WeakReference<Drawable.ConstantState>> mDrawableCache;
    private LongSparseArray<WeakReference<Drawable.ConstantState>> mColorDrawableCache;

    public static MaterialResources getInstance(Context context, Resources resources) {
        int themeResId = getThemeResId(context);
        MaterialResources instance = sInstances.get(themeResId);
        if (instance == null) {
            instance = new MaterialResources();
            sInstances.put(themeResId, instance);
        }

        // Ensure context.
        if (context != null) {
            if (sApplicationContext.get() == null) {
                sApplicationContext = new WeakReference<>(context.getApplicationContext());
            }

            if (instance.mContext.get() == null) {
                instance.mContext = new WeakReference<>(context);
            }
        }

        // Ensure resources.
        if (resources != null && instance.mResources.get() == null) {
            instance.mResources = new WeakReference<>(resources);
            instance.mConfiguration.put(resources, new MaterialConfiguration(resources));
        }

        return instance;
    }

    private static int getThemeResId(Context context) {
        if (context instanceof ContextThemeWrapper) {
            try {
                return (int) ReflectionUtils.invokeDeclaredMethod(
                        ContextThemeWrapper.class,
                        "getThemeResId",
                        ReflectionUtils.EMPTY_TYPES,
                        context,
                        ReflectionUtils.EMPTY_PARAMETERS);
            } catch (Exception e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public Drawable getDrawable(int id) throws Resources.NotFoundException {
        final Context context = mContext.get() != null ? mContext.get() : sApplicationContext.get();
        final Resources resources = mResources.get();
        final MaterialConfiguration configuration = mConfiguration.get(resources);
        final int configChanges = configuration.updateConfiguration(resources);
        if (configChanges != 0) {
            clearDrawableCacheLocked(mDrawableCache, configChanges);
            clearDrawableCacheLocked(mColorDrawableCache, configChanges);
        }

        TypedValue value;
        synchronized (mAccessLock) {
            value = mTmpValue;
            if (value == null) {
                value = new TypedValue();
            } else {
                mTmpValue = null;
            }
            resources.getValue(id, value, true);
        }
        Drawable res = loadDrawable(context, resources, configuration, value, id);
        synchronized (mAccessLock) {
            if (mTmpValue == null) {
                mTmpValue = value;
            }
        }
        return res;
    }

    Drawable loadDrawable(Context context, Resources resources, MaterialConfiguration configuration, TypedValue value,
                          int id)
            throws Resources.NotFoundException {
        final boolean isColorDrawable;
        final LongSparseArray<WeakReference<Drawable.ConstantState>> cache;
        final long key;
        if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            isColorDrawable = true;
            if (mColorDrawableCache == null) {
                mColorDrawableCache = new LongSparseArray<>(1);
            }
            cache = mColorDrawableCache;
            key = value.data;
        } else {
            isColorDrawable = false;
            if (mDrawableCache == null) {
                mDrawableCache = new LongSparseArray<>(1);
            }
            cache = mDrawableCache;
            key = (((long) value.assetCookie) << 32) | value.data;
        }

        // First, check whether we have a cached version of this drawable.
        final Drawable cachedDrawable = getCachedDrawable(resources, cache, key);
        if (cachedDrawable != null) {
            return cachedDrawable;
        }

        // Drawable is not cached.

        final Drawable dr;
        if (isColorDrawable) {
            dr = new ColorDrawable(value.data);
        } else {
            dr = loadDrawableForCookie(context, resources, configuration, value, id);
        }

        // If we were able to obtain a drawable, store it in the appropriate
        // cache (either preload or themed).
        if (dr != null) {
            dr.setChangingConfigurations(value.changingConfigurations);
            cacheDrawable(cache, key, dr);
        }

        return dr;
    }

    /**
     * Loads a drawable from XML or resources stream.
     */
    private Drawable loadDrawableForCookie(Context context, Resources resources, MaterialConfiguration configuration,
                                           TypedValue value, int id) {
        if (value.string == null) {
            throw new Resources.NotFoundException(
                    "Resource \"" + resources.getResourceName(id) + "\" (" + Integer.toHexString(id)
                            + ")  is not a Drawable (color or path): " + value);
        }

        final String file = value.string.toString();

        final Drawable dr;

        try {
            if (file.endsWith(".xml")) {
                configuration.forceAssetsSdkVersion(resources.getAssets(), Build.VERSION_CODES.LOLLIPOP);
                final XmlResourceParser rp = resources.getXml(id);
                configuration.forceAssetsSdkVersion(resources.getAssets(), Build.VERSION.SDK_INT);
                dr = MaterialDrawableUtils.createFromXml(context, resources, rp);
                rp.close();
            } else {
                dr = resources.getDrawable(id);
            }
        } catch (Exception e) {
            final Resources.NotFoundException rnf = new Resources.NotFoundException(
                    "File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(e);
            throw rnf;
        }

        return dr;
    }

    private Drawable getCachedDrawable(Resources resources,
                                       LongSparseArray<WeakReference<Drawable.ConstantState>> cache,
                                       long key) {
        synchronized (mAccessLock) {
            if (cache != null) {
                final Drawable drawable = getCachedDrawableLocked(resources, cache, key);
                if (drawable != null) {
                    return drawable;
                }
            }

            // No cached drawable, we'll need to create a new one.
            return null;
        }
    }

    private void cacheDrawable(LongSparseArray<WeakReference<Drawable.ConstantState>> cache,
                               long key,
                               Drawable dr) {
        final Drawable.ConstantState cs = dr.getConstantState();

        if (cs == null) {
            return;
        }
        synchronized (mAccessLock) {
            cache.put(key, new WeakReference<>(cs));
        }
    }

    private Drawable.ConstantState getConstantStateLocked(
            LongSparseArray<WeakReference<Drawable.ConstantState>> drawableCache, long key) {
        final WeakReference<Drawable.ConstantState> wr = drawableCache.get(key);
        if (wr != null) {   // we have the key
            final Drawable.ConstantState entry = wr.get();
            if (entry != null) {
                return entry;
            } else {  // our entry has been purged
                drawableCache.delete(key);
            }
        }
        return null;
    }

    private Drawable getCachedDrawableLocked(Resources resources,
                                             LongSparseArray<WeakReference<Drawable.ConstantState>> drawableCache,
                                             long key) {
        final Drawable.ConstantState entry = getConstantStateLocked(drawableCache, key);
        if (entry != null) {
            return entry.newDrawable(resources);
        }
        return null;
    }

    private void clearDrawableCacheLocked(LongSparseArray<WeakReference<Drawable.ConstantState>> cache,
                                          int configChanges) {
        final int N = cache != null ? cache.size() : 0;
        for (int i = 0; i < N; i++) {
            final WeakReference<Drawable.ConstantState> ref = cache.valueAt(i);
            if (ref != null) {
                final Drawable.ConstantState cs = ref.get();
                if (cs != null) {
                    if (Configuration.needNewResources(
                            configChanges, cs.getChangingConfigurations())) {
                        cache.setValueAt(i, null);
                    }
                }
            }
        }
    }

    public static int[] CONFIG_NATIVE_BITS = new int[]{
            MaterialConfiguration.NATIVE_CONFIG_MNC,                    // MNC
            MaterialConfiguration.NATIVE_CONFIG_MCC,                    // MCC
            MaterialConfiguration.NATIVE_CONFIG_LOCALE,                 // LOCALE
            MaterialConfiguration.NATIVE_CONFIG_TOUCHSCREEN,            // TOUCH SCREEN
            MaterialConfiguration.NATIVE_CONFIG_KEYBOARD,               // KEYBOARD
            MaterialConfiguration.NATIVE_CONFIG_KEYBOARD_HIDDEN,        // KEYBOARD HIDDEN
            MaterialConfiguration.NATIVE_CONFIG_NAVIGATION,             // NAVIGATION
            MaterialConfiguration.NATIVE_CONFIG_ORIENTATION,            // ORIENTATION
            MaterialConfiguration.NATIVE_CONFIG_SCREEN_LAYOUT,          // SCREEN LAYOUT
            MaterialConfiguration.NATIVE_CONFIG_UI_MODE,                // UI MODE
            MaterialConfiguration.NATIVE_CONFIG_SCREEN_SIZE,            // SCREEN SIZE
            MaterialConfiguration.NATIVE_CONFIG_SMALLEST_SCREEN_SIZE,   // SMALLEST SCREEN SIZE
            MaterialConfiguration.NATIVE_CONFIG_DENSITY,                // DENSITY
            MaterialConfiguration.NATIVE_CONFIG_LAYOUTDIR,              // LAYOUT DIRECTION
    };

    private static class MaterialConfiguration {
        /** Configuration.KEYBOARDHIDDEN_SOFT is hidden */
        private static final int KEYBOARDHIDDEN_SOFT = 3;

        private static final int NATIVE_CONFIG_MCC = 0x0001;
        private static final int NATIVE_CONFIG_MNC = 0x0002;
        private static final int NATIVE_CONFIG_LOCALE = 0x0004;
        private static final int NATIVE_CONFIG_TOUCHSCREEN = 0x0008;
        private static final int NATIVE_CONFIG_KEYBOARD = 0x0010;
        private static final int NATIVE_CONFIG_KEYBOARD_HIDDEN = 0x0020;
        private static final int NATIVE_CONFIG_NAVIGATION = 0x0040;
        private static final int NATIVE_CONFIG_ORIENTATION = 0x0080;
        private static final int NATIVE_CONFIG_DENSITY = 0x0100;
        private static final int NATIVE_CONFIG_SCREEN_SIZE = 0x0200;
        // private static final int NATIVE_CONFIG_VERSION = 0x0400;
        private static final int NATIVE_CONFIG_SCREEN_LAYOUT = 0x0800;
        private static final int NATIVE_CONFIG_UI_MODE = 0x1000;
        private static final int NATIVE_CONFIG_SMALLEST_SCREEN_SIZE = 0x2000;
        private static final int NATIVE_CONFIG_LAYOUTDIR = 0x4000;

        private Class<?>[] mClasses;
        private Object[] mValues;
        private Configuration mConfiguration = new Configuration();

        public MaterialConfiguration(Resources resources) {
            mClasses = new Class<?>[]{int.class, int.class, String.class,
                                      int.class, int.class, int.class, int.class,
                                      int.class, int.class, int.class, int.class,
                                      int.class, int.class, int.class,
                                      int.class, int.class, int.class};
            mValues = new Object[mClasses.length];
            mConfiguration.setTo(resources.getConfiguration());
            updateValues(resources.getDisplayMetrics());
        }

        @SuppressLint("InlinedApi")
        public void updateValues(DisplayMetrics metrics) {
            String locale = null;
            if (mConfiguration.locale != null) {
                locale = mConfiguration.locale.getLanguage();
                if (mConfiguration.locale.getCountry() != null) {
                    locale += "-" + mConfiguration.locale.getCountry();
                }
            }
            int keyboardHidden = mConfiguration.keyboardHidden;
            if (keyboardHidden == Configuration.KEYBOARDHIDDEN_NO &&
                    mConfiguration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
                keyboardHidden = KEYBOARDHIDDEN_SOFT;
            }
            int densityDpi = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                             (int) (metrics.density * 160) :
                             mConfiguration.densityDpi;
            int width, height;
            if (metrics.widthPixels >= metrics.heightPixels) {
                width = metrics.widthPixels;
                height = metrics.heightPixels;
            } else {
                //noinspection SuspiciousNameCombination
                width = metrics.heightPixels;
                //noinspection SuspiciousNameCombination
                height = metrics.widthPixels;
            }

            mValues[0] = mConfiguration.mcc;
            mValues[1] = mConfiguration.mnc;
            mValues[2] = locale;
            mValues[3] = mConfiguration.orientation;
            mValues[4] = mConfiguration.touchscreen;
            mValues[5] = densityDpi;
            mValues[6] = mConfiguration.keyboard;
            mValues[7] = keyboardHidden;
            mValues[8] = mConfiguration.navigation;
            mValues[9] = width;
            mValues[10] = height;
            mValues[11] = mConfiguration.smallestScreenWidthDp;
            mValues[12] = mConfiguration.screenWidthDp;
            mValues[13] = mConfiguration.screenHeightDp;
            mValues[14] = mConfiguration.screenLayout;
            mValues[15] = mConfiguration.uiMode;
        }

        public int updateConfiguration(Resources r) {
            int configChanges = 0;
            Configuration configuration = r.getConfiguration();
            if (mConfiguration.compareTo(configuration) != 0) {
                configChanges = mConfiguration.updateFrom(configuration);
                configChanges = activityInfoConfigToNative(configChanges);

                // Configuration changed. Update values.
                updateValues(r.getDisplayMetrics());
            }
            return configChanges;
        }

        private int activityInfoConfigToNative(int input) {
            int output = 0;
            for (int i = 0; i < CONFIG_NATIVE_BITS.length; i++) {
                if ((input & (1 << i)) != 0) {
                    output |= CONFIG_NATIVE_BITS[i];
                }
            }
            return output;
        }

        /**
         * Forces {@link android.content.res.AssetManager} to fetch resources for a specific SDK version.
         * <p>
         * When drawables contain new attributes, Android aapt creates multiple versions of those drawables, that are
         * separated by SDK version, and placed in different folders (drawable/, drawable-v17/, drawable-v21/...).
         * <p>
         * Drawables of older SDK versions, don't include the new attributes (they are stripped on build time). Only
         * the drawables placed in new SDK folders include the new attributes.
         * <p>
         * This method changes the {@link android.content.res.AssetManager} configuration, making it possible to access
         * the new SDK version drawables on older devices (with older SDK versions).
         *
         * @param version the forced {@link android.os.Build.VERSION.RESOURCES_SDK_INT RESOURCES_SDK_INT} version.
         */
        @SuppressWarnings("JavadocReference")
        public void forceAssetsSdkVersion(AssetManager assetManager, int version) {
            mValues[16] = version;
            ReflectionUtils.invokeDeclaredMethod(
                    AssetManager.class,
                    "setConfiguration",
                    mClasses,
                    assetManager,
                    mValues);
        }
    }
}
