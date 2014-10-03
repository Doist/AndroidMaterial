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

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.doist.material.R;
import io.doist.material.reflection.ReflectionUtils;
import io.doist.material.res.MaterialResources;

public class AnimationMaterialDrawable extends AnimationDrawable {
    private final WeakReference<Context> mContext;

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
        ReflectionUtils.invokeDeclaredMethod(
                Drawable.class,
                "inflateWithAttributes",
                new Class<?>[]{Resources.class, XmlPullParser.class, TypedArray.class, int.class},
                this,
                new Object[]{r, parser, attrs, visibleAttr});
    }

    private void setFrame(int frame, boolean unschedule, boolean animate) {
        ReflectionUtils.invokeDeclaredMethod(
                AnimationDrawable.class,
                "setFrame",
                new Class<?>[] {int.class, boolean.class, boolean.class},
                this,
                new Object[] {frame, unschedule, animate});
    }

    /**
     * Helper class to manipulate internal member mAnimationState.
     */
    private static class AnimationState {
        final Class<?> AnimationStateClass;
        final Class<?> DrawableContainerStateClass;
        final Object mAnimationState;

        public AnimationState(AnimationMaterialDrawable receiver) {
            AnimationStateClass =
                    ReflectionUtils.getClass(AnimationDrawable.class.getName() + "$AnimationState");
            DrawableContainerStateClass =
                    ReflectionUtils.getClass(DrawableContainer.class.getName() + "$DrawableContainerState");

            mAnimationState = ReflectionUtils.getDeclaredFieldValue(
                    AnimationStateClass,
                    "mAnimationState",
                    receiver);
        }

        public void setVariablePadding(boolean variable) {
            ReflectionUtils.invokeDeclaredMethod(
                    DrawableContainerStateClass,
                    "setVariablePadding",
                    new Class<?>[] {boolean.class},
                    mAnimationState,
                    new Object[] {variable});
        }

        public void setOneShot(boolean oneShot) {
            ReflectionUtils.setDeclaredFieldValue(
                    AnimationStateClass,
                    "mOneShot",
                    mAnimationState,
                    oneShot);
        }

        public void addFrame(Drawable dr, int dur) {
            ReflectionUtils.invokeDeclaredMethod(
                    AnimationStateClass,
                    "addFrame",
                    new Class<?>[] {Drawable.class, int.class},
                    mAnimationState,
                    new Object[] {dr, dur});
        }
    }
}
