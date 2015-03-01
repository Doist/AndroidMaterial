package io.doist.material.text.style;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import io.doist.material.utils.Roboto;

public class RobotoTypefaceSpan extends MetricAffectingSpan {
    private final Context mContext;
    private final String mFamily;

    public RobotoTypefaceSpan(Context context, String family) {
        mContext = context;
        mFamily = family;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        apply(ds, mContext, mFamily);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        apply(paint, mContext, mFamily);
    }

    private static void apply(Paint paint, Context context, String family) {
        int oldStyle;

        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }

        Typeface tf = Roboto.getTypeface(context, family, oldStyle);
        int fake = oldStyle & ~tf.getStyle();

        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(tf);
    }
}
