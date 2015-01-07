package io.doist.material.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class FloatingActionButton extends View {
    public FloatingActionButton(Context context) {
        super(context);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRGB(12,12,12);
    }
}
