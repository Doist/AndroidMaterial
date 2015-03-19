package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Xml;

import java.io.IOException;

public class MaterialDrawableUtils {
    public static Drawable createFromXml(Context c, Resources r, XmlPullParser parser)
            throws XmlPullParserException, IOException, NoSuchMethodException {
        AttributeSet attrs = Xml.asAttributeSet(parser);

        int type;
        while ((type=parser.next()) != XmlPullParser.START_TAG &&
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
        } else if (name.equals("animation-list")) {
            drawable = new AnimationMaterialDrawable(c);
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
        } else if (name.equals("ripple")) {
            drawable = new RippleMaterialDrawable(c);
        } else {
            drawable = null;
        }

        if (drawable != null) {
            drawable.inflate(r, parser, attrs);
        } else {
            drawable = Drawable.createFromXmlInner(r, parser, attrs);
        }

        return drawable;
    }
}
