package io.doist.material.res;

import org.xmlpull.v1.XmlPullParser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.util.LongSparseArray;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import io.doist.material.drawable.MaterialDrawableUtils;
import io.doist.material.reflection.ReflectionUtils;

public class MaterialResources {
    private static MaterialResources sInstance;

    final Object mAccessLock = new Object();
    TypedValue mTmpValue = new TypedValue();

    WeakReference<Context> mApplicationContext = new WeakReference<>(null);
    WeakReference<Context> mContext = new WeakReference<>(null);
    WeakReference<Resources> mResources = new WeakReference<>(null);
    WeakHashMap<Resources, MaterialConfiguration> mConfigurations = new WeakHashMap<>();

    private static final LongSparseArray<Drawable.ConstantState>[] sDrawableCache;
    static {
        //noinspection unchecked
        sDrawableCache = new LongSparseArray[2];
        sDrawableCache[0] = new LongSparseArray<>();
        sDrawableCache[1] = new LongSparseArray<>();
    }
    final static LongSparseArray<Drawable.ConstantState> sColorDrawableCache = new LongSparseArray<>();

    public static MaterialResources getInstance(Context context, Resources resources) {
        if (sInstance == null) {
            sInstance = new MaterialResources();
        }

        // Ensure context.
        if (context != null) {
            if (sInstance.mApplicationContext.get() == null) {
                sInstance.mApplicationContext = new WeakReference<>(context.getApplicationContext());
            }

            if (sInstance.mContext.get() == null) {
                sInstance.mContext = new WeakReference<>(context);
            }
        }

        // Ensure resources.
        if (resources != null && sInstance.mResources.get() == null) {
            sInstance.mResources = new WeakReference<>(resources);
            sInstance.mConfigurations.put(resources, new MaterialConfiguration(resources));
        }

        return sInstance;
    }

    public Drawable getDrawable(int id) throws Resources.NotFoundException {
        final Context c = mContext.get() != null ? mContext.get() : mApplicationContext.get();
        final Resources r = mResources.get();
        final MaterialConfiguration configuration = mConfigurations.get(r);
        final int configChanges = configuration.updateConfiguration(r);
        if (configChanges != 0) {
            clearDrawableCachesLocked(sDrawableCache, configChanges);
            clearDrawableCacheLocked(sColorDrawableCache, configChanges);
        }

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
        Drawable res = loadDrawable(c, r, configuration, value, id);
        synchronized (mAccessLock) {
            if (mTmpValue == null) {
                mTmpValue = value;
            }
        }
        return res;
    }

    @SuppressLint("NewApi")
    Drawable loadDrawable(Context c, Resources r, MaterialConfiguration configuration, TypedValue value, int id)
            throws Resources.NotFoundException {

        boolean isColorDrawable = false;
        if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            isColorDrawable = true;
        }
        final long key = isColorDrawable ? value.data :
                         (((long) value.assetCookie) << 32) | value.data;

        LongSparseArray<Drawable.ConstantState> drawableCache;
        if (isColorDrawable) {
            drawableCache = sColorDrawableCache;
        } else {
            int layoutDirection = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                                  0 :
                                  r.getConfiguration().getLayoutDirection();

            drawableCache = sDrawableCache[layoutDirection];
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
                    configuration.forceAssetsSdkVersion(r.getAssets(), Build.VERSION_CODES.LOLLIPOP);
                    XmlPullParser parser = r.getXml(id);
                    configuration.forceAssetsSdkVersion(r.getAssets(), Build.VERSION.SDK_INT);
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
                        sColorDrawableCache.put(key, cs);
                    } else {
                        int layoutDirection = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                                              0 :
                                              r.getConfiguration().getLayoutDirection();

                        sDrawableCache[layoutDirection].put(key, cs);
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

    private static void clearDrawableCachesLocked(LongSparseArray<Drawable.ConstantState>[] caches,
                                                  int configChanges) {
        for (LongSparseArray<Drawable.ConstantState> cache : caches) {
            clearDrawableCacheLocked(cache, configChanges);
        }
    }

    private static void clearDrawableCacheLocked(LongSparseArray<Drawable.ConstantState> cache,
                                                 int configChanges) {
        final int N = cache.size();
        for (int i = 0; i < N; i++) {
            final Drawable.ConstantState cs = cache.valueAt(i);
            if (cs != null) {
                if (Configuration.needNewResources(
                        configChanges, cs.getChangingConfigurations())) {
                    cache.setValueAt(i, null);
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
                             (int)(metrics.density*160) :
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
