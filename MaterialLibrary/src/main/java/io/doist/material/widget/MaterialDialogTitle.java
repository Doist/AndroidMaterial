package io.doist.material.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.TypedValue;

import io.doist.material.R;

/**
 * Adapted from <code>com.android.internal.widget.DialogTitle</code>.
 *
 * Used by dialogs to change the font size and number of lines to try to fit
 * the text to the available space.
 */
public class MaterialDialogTitle extends RobotoTextView {

    public MaterialDialogTitle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MaterialDialogTitle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MaterialDialogTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialDialogTitle(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final Layout layout = getLayout();
        if (layout != null) {
            final int lineCount = layout.getLineCount();
            if (lineCount > 0) {
                final int ellipsisCount = layout.getEllipsisCount(lineCount - 1);
                if (ellipsisCount > 0) {
                    setSingleLine(false);
                    setMaxLines(2);

                    final TypedArray a = getContext().obtainStyledAttributes(null,
                                                                         R.styleable.TextAppearance,
                                                                         android.R.attr.textAppearanceMedium,
                                                                         R.style.TextAppearance_AppCompat_Medium);
                    final int textSize = a.getDimensionPixelSize(
                            R.styleable.TextAppearance_android_textSize, 0);
                    if (textSize != 0) {
                        // textSize is already expressed in pixels
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                    }
                    a.recycle();

                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
        }
    }
}
