package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.StateSet;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.doist.material.R;
import io.doist.material.res.MaterialResources;

public class StateListMaterialDrawable extends StateListDrawable {
    private static final String TAG = StateListMaterialDrawable.class.getSimpleName();

    private static final boolean DEFAULT_DITHER = true;

    private WeakReference<Context> mContext;

    StateListMaterialDrawable(Context context) {
        mContext = new WeakReference<>(context);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        TypedArray a = r.obtainAttributes(attrs, R.styleable.StateListDrawable);

        inflateWithAttributes(r, parser, a, R.styleable.StateListDrawable_android_visible);

        StateListState mStateListState = new StateListState(this);

        mStateListState.setVariablePadding(
                a.getBoolean(R.styleable.StateListDrawable_android_variablePadding, false));
        mStateListState.setConstantSize(a.getBoolean(
                R.styleable.StateListDrawable_android_constantSize, false));
        mStateListState.setEnterFadeDuration(a.getInt(
                R.styleable.StateListDrawable_android_enterFadeDuration, 0));
        mStateListState.setExitFadeDuration(a.getInt(
                R.styleable.StateListDrawable_android_exitFadeDuration, 0));

        setDither(a.getBoolean(R.styleable.StateListDrawable_android_dither, DEFAULT_DITHER));

        setAutoMirrored(a.getBoolean(R.styleable.StateListDrawable_android_autoMirrored, false));

        a.recycle();

        int type;

        final int innerDepth = parser.getDepth() + 1;
        int depth;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && ((depth = parser.getDepth()) >= innerDepth
                || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            if (depth > innerDepth || !parser.getName().equals("item")) {
                continue;
            }

            int drawableRes = 0;

            int i;
            int j = 0;
            final int numAttrs = attrs.getAttributeCount();
            int[] states = new int[numAttrs];
            for (i = 0; i < numAttrs; i++) {
                final int stateResId = attrs.getAttributeNameResource(i);
                if (stateResId == 0) {
                    break;
                }
                if (stateResId == android.R.attr.drawable) {
                    drawableRes = attrs.getAttributeResourceValue(i, 0);
                } else {
                    states[j++] = attrs.getAttributeBooleanValue(i, false)
                                  ? stateResId
                                  : -stateResId;
                }
            }
            states = StateSet.trimStateSet(states, j);

            Context c = mContext.get();
            Drawable dr;
            if (drawableRes != 0) {
                dr = MaterialResources.getInstance(c, r).getDrawable(drawableRes);
            } else {
                while ((type = parser.next()) == XmlPullParser.TEXT) {
                }
                if (type != XmlPullParser.START_TAG) {
                    throw new XmlPullParserException(
                            parser.getPositionDescription()
                                    + ": <item> tag requires a 'drawable' attribute or "
                                    + "child tag defining a drawable");
                }
                dr = MaterialDrawableUtils.createFromXmlInner(c, r, parser, attrs);
            }

            mStateListState.addStateSet(states, dr);
        }

        onStateChange(getState());
    }

    private void inflateWithAttributes(Resources r, XmlPullParser parser, TypedArray attrs, int visibleAttr) {
        try {
            final Method inflateWithAttributes = Drawable.class.getDeclaredMethod(
                    "inflateWithAttributes",
                    Resources.class,
                    XmlPullParser.class,
                    TypedArray.class,
                    int.class);

            inflateWithAttributes.setAccessible(true);
            inflateWithAttributes.invoke(
                    this,
                    r,
                    parser,
                    attrs,
                    visibleAttr);

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Log.w(TAG, e);
        }
    }

    /**
     * Helper class to manipulate internal member mStateListState.
     */
    private static class StateListState {
        Class<?> StateListStateClass;
        Object mStateListState;

        public StateListState(StateListMaterialDrawable owner) {
            try {
                StateListStateClass = Class.forName(StateListDrawable.class.getName() + "$StateListState");
            } catch (ClassNotFoundException e) {
                StateListStateClass = null;
                Log.w(TAG, e);
            }

            if (StateListStateClass != null) {
                try {
                    Field mStateListStateField = StateListDrawable.class.getDeclaredField("mStateListState");
                    mStateListStateField.setAccessible(true);
                    mStateListState = mStateListStateField.get(owner);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    mStateListState = null;
                    Log.w(TAG, e);
                }
            } else {
                mStateListState = null;
            }
        }

        public int addStateSet(int[] stateSet, Drawable drawable) {
            if (StateListStateClass != null && mStateListState != null) {
                try {
                    final Method addStateSet =
                            StateListStateClass.getDeclaredMethod("addStateSet", int[].class, Drawable.class);
                    addStateSet.setAccessible(true);
                    return (int) addStateSet.invoke(mStateListState, stateSet, drawable);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    Log.w(TAG, e);
                }
            }
            return 0;
        }

        public final void setVariablePadding(boolean variable) {
            if (StateListStateClass != null && mStateListState != null) {
                try {
                    final Method setVariablePadding =
                            StateListStateClass.getSuperclass().getDeclaredMethod("setVariablePadding", boolean.class);
                    setVariablePadding.setAccessible(true);
                    setVariablePadding.invoke(mStateListState, variable);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    Log.w(TAG, e);
                }
            }
        }

        public final void setConstantSize(boolean constant) {
            if (StateListStateClass != null && mStateListState != null) {
                try {
                    final Method setConstantSize =
                            StateListStateClass.getSuperclass().getDeclaredMethod("setConstantSize", boolean.class);
                    setConstantSize.setAccessible(true);
                    setConstantSize.invoke(mStateListState, constant);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    Log.w(TAG, e);
                }
            }
        }

        public final void setEnterFadeDuration(int duration) {
            if (StateListStateClass != null && mStateListState != null) {
                try {
                    final Method setEnterFadeDuration =
                            StateListStateClass.getSuperclass().getDeclaredMethod("setEnterFadeDuration", int.class);
                    setEnterFadeDuration.setAccessible(true);
                    setEnterFadeDuration.invoke(mStateListState, duration);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    Log.w(TAG, e);
                }
            }
        }

        public final void setExitFadeDuration(int duration) {
            if (StateListStateClass != null && mStateListState != null) {
                try {
                    final Method setExitFadeDuration =
                            StateListStateClass.getSuperclass().getDeclaredMethod("setExitFadeDuration", int.class);
                    setExitFadeDuration.setAccessible(true);
                    setExitFadeDuration.invoke(mStateListState, duration);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }
}
