package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.doist.material.R;
import io.doist.material.reflection.ReflectionUtils;

public class GradientMaterialDrawable extends GradientDrawable {
    private final WeakReference<Context> mContext;

    private ColorStateList mSolidColor;

    public GradientMaterialDrawable(Context context) {
        super();
        mContext = new WeakReference<>(context);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        final GradientState st = new GradientState(this);

        final Context context = mContext.get();

        TypedArray a = obtainAttributes(context, r, attrs, R.styleable.GradientDrawable);

        inflateWithAttributes(r, parser, a, R.styleable.GradientDrawable_android_visible);

        int shapeType = a.getInt(R.styleable.GradientDrawable_android_shape, RECTANGLE);
        boolean dither = a.getBoolean(R.styleable.GradientDrawable_android_dither, false);

        if (shapeType == RING) {
            int innerRadius = a.getDimensionPixelSize(R.styleable.GradientDrawable_android_innerRadius, -1);
            st.setInnerRadius(innerRadius);
            if (innerRadius == -1) {
                st.setInnerRadiusRatio(a.getFloat(R.styleable.GradientDrawable_android_innerRadiusRatio, 3.0f));
            }

            int thickness = a.getDimensionPixelSize(R.styleable.GradientDrawable_android_thickness, -1);
            st.setThickness(thickness);
            if (thickness == -1) {
                st.setThicknessRatio(a.getFloat(R.styleable.GradientDrawable_android_thicknessRatio, 9.0f));
            }
            st.setUseLevelForShape(a.getBoolean(R.styleable.GradientDrawable_android_useLevel, true));
        }

        a.recycle();

        setShape(shapeType);
        setDither(dither);

        int type;

        final int innerDepth = parser.getDepth() + 1;
        int depth;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && ((depth = parser.getDepth()) >= innerDepth
                || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            if (depth > innerDepth) {
                continue;
            }

            String name = parser.getName();

            if (name.equals("size")) {
                a = obtainAttributes(context, r, attrs, R.styleable.GradientDrawableSize);
                int width = a.getDimensionPixelSize(R.styleable.GradientDrawableSize_android_width, -1);
                int height = a.getDimensionPixelSize(R.styleable.GradientDrawableSize_android_height, -1);
                a.recycle();
                setSize(width, height);
            } else if (name.equals("gradient")) {
                a = obtainAttributes(context, r, attrs, R.styleable.GradientDrawableGradient);
                int startColor = a.getColor(R.styleable.GradientDrawableGradient_android_startColor, 0);
                boolean hasCenterColor = a.hasValue(R.styleable.GradientDrawableGradient_android_centerColor);
                int centerColor = a.getColor(R.styleable.GradientDrawableGradient_android_centerColor, 0);
                int endColor = a.getColor(R.styleable.GradientDrawableGradient_android_endColor, 0);
                int gradientType = a.getInt(R.styleable.GradientDrawableGradient_android_type, LINEAR_GRADIENT);

                float centerX = getFloatOrFraction(
                        a,
                        R.styleable.GradientDrawableGradient_android_centerX,
                        0.5f);
                st.setCenterX(centerX);

                float centerY = getFloatOrFraction(
                        a,
                        R.styleable.GradientDrawableGradient_android_centerY,
                        0.5f);
                st.setCenterY(centerY);

                st.setUseLevel(a.getBoolean(R.styleable.GradientDrawableGradient_android_useLevel, false));
                st.setGradient(gradientType);

                if (gradientType == LINEAR_GRADIENT) {
                    int angle = (int) a.getFloat(R.styleable.GradientDrawableGradient_android_angle, 0);
                    angle %= 360;
                    if (angle % 45 != 0) {
                        throw new XmlPullParserException(a.getPositionDescription()
                                                                 + "<gradient> tag requires 'angle' attribute to "
                                                                 + "be a multiple of 45");
                    }

                    switch (angle) {
                        case 0:
                            st.setOrientation(Orientation.LEFT_RIGHT);
                            break;
                        case 45:
                            st.setOrientation(Orientation.BL_TR);
                            break;
                        case 90:
                            st.setOrientation(Orientation.BOTTOM_TOP);
                            break;
                        case 135:
                            st.setOrientation(Orientation.BR_TL);
                            break;
                        case 180:
                            st.setOrientation(Orientation.RIGHT_LEFT);
                            break;
                        case 225:
                            st.setOrientation(Orientation.TR_BL);
                            break;
                        case 270:
                            st.setOrientation(Orientation.TOP_BOTTOM);
                            break;
                        case 315:
                            st.setOrientation(Orientation.TL_BR);
                            break;
                    }
                } else {
                    TypedValue tv = a.peekValue(R.styleable.GradientDrawableGradient_android_gradientRadius);
                    if (tv != null) {
                        boolean radiusRel = tv.type == TypedValue.TYPE_FRACTION;
                        st.setGradientRadius(radiusRel ? tv.getFraction(1.0f, 1.0f) : tv.getFloat());
                    } else if (gradientType == RADIAL_GRADIENT) {
                        throw new XmlPullParserException(
                                a.getPositionDescription()
                                        + "<gradient> tag requires 'gradientRadius' "
                                        + "attribute with radial type");
                    }
                }

                a.recycle();

                if (hasCenterColor) {
                    int[] colors = new int[3];
                    colors[0] = startColor;
                    colors[1] = centerColor;
                    colors[2] = endColor;
                    st.setColors(colors);

                    float[] positions = new float[3];
                    positions[0] = 0.0f;
                    // Since 0.5f is default value, try to take the one that isn't 0.5f
                    positions[1] = centerX != 0.5f ? centerX : centerY;
                    positions[2] = 1f;
                    st.setPositions(positions);
                } else {
                    int[] colors = new int[2];
                    colors[0] = startColor;
                    colors[1] = endColor;
                    st.setColors(colors);
                }

            } else if (name.equals("solid")) {
                a = obtainAttributes(context, r, attrs, R.styleable.GradientDrawableSolid);
                mSolidColor = a.getColorStateList(R.styleable.GradientDrawableSolid_android_color);
                a.recycle();
                setColor(mSolidColor.getDefaultColor());
            } else if (name.equals("stroke")) {
                a = obtainAttributes(context, r, attrs, R.styleable.GradientDrawableStroke);
                int width = a.getDimensionPixelSize(R.styleable.GradientDrawableStroke_android_width, 0);
                int color = a.getColor(R.styleable.GradientDrawableStroke_android_color, 0);
                float dashWidth = a.getDimension(R.styleable.GradientDrawableStroke_android_dashWidth, 0);
                if (dashWidth != 0.0f) {
                    float dashGap = a.getDimension(R.styleable.GradientDrawableStroke_android_dashGap, 0);
                    setStroke(width, color, dashWidth, dashGap);
                } else {
                    setStroke(width, color);
                }
                a.recycle();
            } else if (name.equals("corners")) {
                a = obtainAttributes(context, r, attrs, R.styleable.DrawableCorners);
                int radius = a.getDimensionPixelSize(
                        R.styleable.DrawableCorners_android_radius, 0);
                setCornerRadius(radius);
                int topLeftRadius = a.getDimensionPixelSize(
                        R.styleable.DrawableCorners_android_topLeftRadius, radius);
                int topRightRadius = a.getDimensionPixelSize(
                        R.styleable.DrawableCorners_android_topRightRadius, radius);
                int bottomLeftRadius = a.getDimensionPixelSize(
                        R.styleable.DrawableCorners_android_bottomLeftRadius, radius);
                int bottomRightRadius = a.getDimensionPixelSize(
                        R.styleable.DrawableCorners_android_bottomRightRadius, radius);
                if (topLeftRadius != radius || topRightRadius != radius ||
                        bottomLeftRadius != radius || bottomRightRadius != radius) {
                    // The corner radii are specified in clockwise order (see Path.addRoundRect())
                    setCornerRadii(new float[]{
                            topLeftRadius, topLeftRadius,
                            topRightRadius, topRightRadius,
                            bottomRightRadius, bottomRightRadius,
                            bottomLeftRadius, bottomLeftRadius
                    });
                }
                a.recycle();
            } else if (name.equals("padding")) {
                a = obtainAttributes(context, r, attrs, R.styleable.GradientDrawablePadding);
                Rect padding = new Rect(
                        a.getDimensionPixelOffset(R.styleable.GradientDrawablePadding_android_left, 0),
                        a.getDimensionPixelOffset(R.styleable.GradientDrawablePadding_android_top, 0),
                        a.getDimensionPixelOffset(R.styleable.GradientDrawablePadding_android_right, 0),
                        a.getDimensionPixelOffset(R.styleable.GradientDrawablePadding_android_bottom, 0));
                setPadding(padding);
                a.recycle();

                st.setPadding(padding);
            } else {
                Log.w("drawable", "Bad element under <shape>: " + name);
            }
        }

        st.computeOpacity();
    }

    ColorStateList getSolidColor() {
        return mSolidColor;
    }

    private TypedArray obtainAttributes(Context context, Resources r, AttributeSet set, int[] attrs) {
        return context != null ?
               context.obtainStyledAttributes(set, attrs) :
               r.obtainAttributes(set, attrs);
    }

    private void inflateWithAttributes(Resources r, XmlPullParser parser, TypedArray attrs, int visibleAttr) {
        ReflectionUtils.invokeDeclaredMethod(
                Drawable.class,
                "inflateWithAttributes",
                new Class<?>[]{Resources.class, XmlPullParser.class, TypedArray.class, int.class},
                this,
                new Object[]{r, parser, attrs, visibleAttr});
    }

    private static float getFloatOrFraction(TypedArray a, int index, float defaultValue) {
        TypedValue tv = a.peekValue(index);
        float v = defaultValue;
        if (tv != null) {
            boolean vIsFraction = tv.type == TypedValue.TYPE_FRACTION;
            v = vIsFraction ? tv.getFraction(1.0f, 1.0f) : tv.getFloat();
        }
        return v;
    }

    private void setPadding(Rect padding) {
        ReflectionUtils.setDeclaredFieldValue(
                GradientDrawable.class,
                "mPadding",
                this,
                padding);
    }

    /**
     * Helper class to manipulate internal member mGradientState.
     */
    private static class GradientState {
        final Class<?> GradientStateClass;
        final Class<?> DrawableContainerStateClass;
        final Object mGradientState;

        public GradientState(GradientMaterialDrawable receiver) {
            GradientStateClass =
                    ReflectionUtils.getClass(GradientDrawable.class.getName() + "$GradientState");
            DrawableContainerStateClass =
                    ReflectionUtils.getClass(DrawableContainer.class.getName() + "$DrawableContainerState");

            mGradientState = ReflectionUtils.getDeclaredFieldValue(
                    GradientDrawable.class,
                    "mGradientState",
                    receiver);
        }

        public void setInnerRadius(int innerRadius) {
            setField("mInnerRadius", innerRadius);
        }

        public void setInnerRadiusRatio(float innerRadiusRatio) {
            setField("mInnerRadiusRatio", innerRadiusRatio);
        }

        public void setThickness(int thickness) {
            setField("mThickness", thickness);
        }

        public void setThicknessRatio(float thicknessRatio) {
            setField("mThicknessRatio", thicknessRatio);
        }

        public void setUseLevelForShape(boolean useLevelForShape) {
            setField("mUseLevelForShape", useLevelForShape);
        }

        public void setCenterX(float centerX) {
            setField("mCenterX", centerX);
        }

        public void setCenterY(float centerY) {
            setField("mCenterY", centerY);
        }

        public void setUseLevel(boolean useLevel) {
            setField("mUseLevel", useLevel);
        }

        public void setGradient(int gradient) {
            setField("mGradient", gradient);
        }

        public void setOrientation(Orientation orientation) {
            setField("mOrientation", orientation);
        }

        public void setGradientRadius(float gradientRadius) {
            setField("mGradientRadius", gradientRadius);
        }

        public void setColors(int[] colors) {
            setField("mColors", colors);
        }

        public void setPositions(float[] positions) {
            setField("mPositions", positions);
        }

        public void setPadding(Rect padding) {
            setField("mPadding", padding);
        }

        public void computeOpacity() {
            ReflectionUtils.invokeDeclaredMethod(
                    GradientStateClass,
                    "computeOpacity",
                    ReflectionUtils.EMPTY_TYPES,
                    mGradientState,
                    ReflectionUtils.EMPTY_PARAMETERS);
        }

        private void setField(String fieldName, Object value) {
            ReflectionUtils.setDeclaredFieldValue(GradientStateClass, fieldName, mGradientState, value);
        }
    }
}
