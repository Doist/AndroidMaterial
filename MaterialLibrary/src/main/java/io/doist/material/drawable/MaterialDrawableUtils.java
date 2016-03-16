package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Xml;

import java.io.IOException;

public class MaterialDrawableUtils {

    private MaterialDrawableUtils() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static Drawable createFromXml(Context c, Resources r, XmlPullParser parser)
            throws XmlPullParserException, IOException, NoSuchMethodException {
        AttributeSet attrs = Xml.asAttributeSet(parser);

        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG &&
                type != XmlPullParser.END_DOCUMENT) {
            // Empty loop.
        }

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        Drawable drawable = createFromXmlInner(c, r, parser, attrs);

        if (drawable == null) {
            throw new RuntimeException("Unknown initial tag: " + parser.getName());
        }

        return drawable;
    }

    @SuppressWarnings("deprecation")
    public static Drawable createFromXmlInner(Context c, Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        Drawable drawable;

        final String name = parser.getName();

        // TODO: add the remaining drawables that are not covered.
        if (name.equals("selector")) {
            drawable = new StateListMaterialDrawable(c);
        //} else if (name.equals("animated-selector")) {
        //} else if (name.equals("level-list")) {
        } else if (name.equals("layer-list")) {
            drawable = new LayerMaterialDrawable(c);
        //} else if (name.equals("transition")) {
        } else if (name.equals("ripple")) {
            drawable = new RippleMaterialDrawable(c);
        } else if (name.equals("color")) {
            drawable = new ColorMaterialDrawable(c);
        } else if (name.equals("shape")) {
            drawable = new GradientMaterialDrawable(c);
        //} else if (name.equals("vector")) {
        //} else if (name.equals("animated-vector")) {
        //} else if (name.equals("scale")) {
        //} else if (name.equals("clip")) {
        //} else if (name.equals("rotate")) {
        //} else if (name.equals("animated-rotate")) {
        } else if (name.equals("animation-list")) {
            drawable = new AnimationMaterialDrawable(c);
        } else if (name.equals("inset")) {
            drawable = new InsetMaterialDrawable(c);
        } else if (name.equals("bitmap")) {
            drawable = new BitmapDrawable();
            if (r != null) {
                ((BitmapDrawable) drawable).setTargetDensity(r.getDisplayMetrics());
            }
            drawable = new TintDrawable(c, drawable);
        } else if (name.equals("nine-patch")) {
            drawable = new NinePatchDrawable(null, null);
            if (r != null) {
                ((NinePatchDrawable) drawable).setTargetDensity(r.getDisplayMetrics());
            }
            drawable = new TintDrawable(c, drawable);
        } else {
            drawable = null;
        }

        if (drawable != null) {
            drawable.inflate(r, parser, attrs);

            if (drawable instanceof GradientMaterialDrawable) {
                // Before Lollipop, GradientDrawable does not support a ColorStateList solid color.
                // In the case of a stateful solid color, enclose the drawable inside a TintDrawable.
                ColorStateList solidColor = ((GradientMaterialDrawable) drawable).getSolidColor();
                if (solidColor != null && solidColor.isStateful()) {
                    drawable = new TintDrawable(c, drawable, solidColor);
                }
            }
        } else {
            drawable = Drawable.createFromXmlInner(r, parser, attrs);
        }

        return drawable;
    }
}
