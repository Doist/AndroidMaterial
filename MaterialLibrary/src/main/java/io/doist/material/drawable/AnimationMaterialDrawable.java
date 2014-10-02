package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.util.AttributeSet;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.doist.material.R;
import io.doist.material.res.MaterialResources;

public class AnimationMaterialDrawable extends AnimationDrawable {
    private static final String TAG = AnimationMaterialDrawable.class.getSimpleName();

    private WeakReference<Context> mContext;

    AnimationMaterialDrawable(Context context) {
        mContext = new WeakReference<>(context);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        TypedArray a = r.obtainAttributes(attrs, R.styleable.AnimationDrawable);

        inflateWithAttributes(r, parser, a, R.styleable.AnimationDrawable_android_visible);

        AnimationState mAnimationState = new AnimationState(this);

        mAnimationState.setVariablePadding(a.getBoolean(R.styleable.AnimationDrawable_android_variablePadding, false));

        mAnimationState.setOneShot(a.getBoolean(R.styleable.AnimationDrawable_android_oneshot, false));

        a.recycle();

        int type;

        final int innerDepth = parser.getDepth()+1;
        int depth;
        while ((type=parser.next()) != XmlPullParser.END_DOCUMENT &&
                ((depth = parser.getDepth()) >= innerDepth || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            if (depth > innerDepth || !parser.getName().equals("item")) {
                continue;
            }

            a = r.obtainAttributes(attrs, R.styleable.AnimationDrawableItem);
            int duration = a.getInt(
                    R.styleable.AnimationDrawableItem_android_duration, -1);
            if (duration < 0) {
                throw new XmlPullParserException(
                        parser.getPositionDescription()
                                + ": <item> tag requires a 'duration' attribute");
            }
            int drawableRes = a.getResourceId(
                    R.styleable.AnimationDrawableItem_android_drawable, 0);

            a.recycle();

            Context c = mContext.get();
            Drawable dr;
            if (drawableRes != 0) {
                dr = MaterialResources.getInstance(c, r).getDrawable(drawableRes);
            } else {
                while ((type=parser.next()) == XmlPullParser.TEXT) {
                    // Empty
                }
                if (type != XmlPullParser.START_TAG) {
                    throw new XmlPullParserException(parser.getPositionDescription() +
                                                             ": <item> tag requires a 'drawable' attribute or child tag" +
                                                             " defining a drawable");
                }
                dr = MaterialDrawableUtils.createFromXmlInner(c, r, parser, attrs);
            }

            mAnimationState.addFrame(dr, duration);
            if (dr != null) {
                dr.setCallback(this);
            }
        }

        setFrame(0, true, false);
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

    private void setFrame(int frame, boolean unschedule, boolean animate) {
        try {
            final Method setFrame = AnimationDrawable.class.getDeclaredMethod(
                    "setFrame",
                    int.class,
                    boolean.class,
                    boolean.class);
            setFrame.setAccessible(true);
            setFrame.invoke(this, frame, unschedule, animate);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            Log.w(TAG, e);
        }
    }

    /**
     * Helper class to manipulate internal member mAnimationState.
     */
    private static class AnimationState {
        Class<?> AnimationStateClass;
        Class<?> DrawableContainerStateClass;
        Object mAnimationState;

        public AnimationState(AnimationMaterialDrawable owner) {
            try {
                AnimationStateClass = Class.forName(AnimationDrawable.class.getName() + "$AnimationState");
            } catch (ClassNotFoundException e) {
                AnimationStateClass = null;
                Log.w(TAG, e);
            }

            try {
                DrawableContainerStateClass = Class.forName(DrawableContainer.class.getName() + "$DrawableContainerState");
            } catch (ClassNotFoundException e) {
                DrawableContainerStateClass = null;
                Log.w(TAG, e);
            }

            if (AnimationStateClass != null) {
                try {
                    Field mAnimationStateField = AnimationDrawable.class.getDeclaredField("mAnimationState");
                    mAnimationStateField.setAccessible(true);
                    mAnimationState = mAnimationStateField.get(owner);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    mAnimationState = null;
                    Log.w(TAG, e);
                }
            } else {
                mAnimationState = null;
            }
        }

        public void setVariablePadding(boolean variable) {
            if (DrawableContainerStateClass != null && mAnimationState != null) {
                try {
                    final Method setVariablePadding =
                            DrawableContainerStateClass.getDeclaredMethod("setVariablePadding", boolean.class);
                    setVariablePadding.setAccessible(true);
                    setVariablePadding.invoke(mAnimationState, variable);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    Log.w(TAG, e);
                }
            }
        }

        public void setOneShot(boolean oneShot) {
            if (AnimationStateClass != null && mAnimationState != null) {
                try {
                    final Field mOneShotField = AnimationStateClass.getDeclaredField("mOneShot");
                    mOneShotField.setAccessible(true);
                    mOneShotField.set(mAnimationState, oneShot);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    Log.w(TAG, e);
                }
            }
        }

        public void addFrame(Drawable dr, int dur) {
            if (AnimationStateClass != null && mAnimationState != null) {
                try {
                    final Method addFrame =
                            AnimationStateClass.getDeclaredMethod("addFrame", Drawable.class, int.class);
                    addFrame.setAccessible(true);
                    addFrame.invoke(mAnimationState, dr, dur);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }
}
