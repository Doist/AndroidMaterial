package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.StateSet;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.doist.material.R;
import io.doist.material.reflection.ReflectionUtils;
import io.doist.material.res.MaterialResources;

public class StateListMaterialDrawable extends StateListDrawable {
    private static final boolean DEFAULT_DITHER = true;

    private final WeakReference<Context> mContext;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setAutoMirrored(a.getBoolean(R.styleable.StateListDrawable_android_autoMirrored, false));
        }

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
        ReflectionUtils.invokeDeclaredMethod(
                Drawable.class,
                "inflateWithAttributes",
                new Class<?>[]{Resources.class, XmlPullParser.class, TypedArray.class, int.class},
                this,
                new Object[]{r, parser, attrs, visibleAttr});
    }

    /**
     * Helper class to manipulate internal member mStateListState.
     */
    private static class StateListState {
        final Class<?> StateListStateClass;
        final Object mStateListState;

        public StateListState(StateListMaterialDrawable receiver) {
            StateListStateClass =
                    ReflectionUtils.getClass(StateListDrawable.class.getName() + "$StateListState");

            mStateListState = ReflectionUtils.getDeclaredFieldValue(
                    StateListStateClass,
                    "mStateListState",
                    receiver);
        }

        public int addStateSet(int[] stateSet, Drawable drawable) {
            Object result = ReflectionUtils.invokeDeclaredMethod(
                    StateListStateClass,
                    "addStateSet",
                    new Class<?>[] {int[].class, Drawable.class},
                    mStateListState,
                    new Object[] {stateSet, drawable});
            return result != null ? (int) result : 0;
        }

        public final void setVariablePadding(boolean variable) {
            ReflectionUtils.invokeDeclaredMethod(
                    StateListStateClass,
                    "setVariablePadding",
                    new Class<?>[] {boolean.class},
                    mStateListState,
                    new Object[] {variable});
        }

        public final void setConstantSize(boolean constant) {
            ReflectionUtils.invokeDeclaredMethod(
                    StateListStateClass,
                    "setConstantSize",
                    new Class<?>[] {boolean.class},
                    mStateListState,
                    new Object[] {constant});
        }

        public final void setEnterFadeDuration(int duration) {
            ReflectionUtils.invokeDeclaredMethod(
                    StateListStateClass,
                    "setEnterFadeDuration",
                    new Class<?>[] {int.class},
                    mStateListState,
                    new Object[] {duration});
        }

        public final void setExitFadeDuration(int duration) {
            ReflectionUtils.invokeDeclaredMethod(
                    StateListStateClass,
                    "setExitFadeDuration",
                    new Class<?>[] {int.class},
                    mStateListState,
                    new Object[] {duration});
        }
    }
}
