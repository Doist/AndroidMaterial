package io.doist.material.widget.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Array;

import io.doist.material.R;
import io.doist.material.reflection.ReflectionUtils;
import io.doist.material.res.MaterialResources;

public class MaterialWidgetHandler {
    private static final boolean sSkip = Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT;

    public static AttributeSet hideStyleableAttributes(AttributeSet set, Styleable... styleables) {
        if (sSkip) {
            return set;
        }

        for (Styleable styleable : styleables) {
            styleable.hide();
        }

        return set;
    }

    public static void restoreStyleableAttributes(Styleable... styleables) {
        if (sSkip) {
            return;
        }

        for (Styleable styleable : styleables) {
            styleable.restore();
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void init(View view, AttributeSet set, int defStyle, Styleable[] styleables) {
        if (sSkip) {
            return;
        }

        Context context = themifyContext(view.getContext(), set);
        MaterialResources resources = MaterialResources.getInstance(context, context.getResources());
        for (Styleable styleable : styleables) {
            styleable.initAttributes(context, resources, view, set, defStyle);
        }
    }

    /**
     * Applies {@code android:theme} to {@code context} by wrapping it in a {@link ContextThemeWrapper}.
     */
    public static Context themifyContext(Context context, AttributeSet attrs) {
        if (sSkip) {
            return context;
        }

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.View, 0, 0);
        int themeResId = a.getResourceId(R.styleable.View_android_theme, 0);
        a.recycle();

        if (themeResId != 0 && themeResId != MaterialResources.getThemeResId(context)) {
            context = new ContextThemeWrapper(context, themeResId);
        }
        return context;
    }

    public enum Styleable {
        VIEW("View", "background") {
            @Override
            public void initAttributes(Context context, MaterialResources resources, View view, AttributeSet set,
                                       int defStyle) {
                TypedArray ta = context.obtainStyledAttributes(set, R.styleable.MaterialView, defStyle, 0);
                try {
                    if (ta.hasValue(R.styleable.MaterialView_android_background)) {
                        Drawable drawable =
                                resources.getDrawable(ta.getResourceId(R.styleable.MaterialView_android_background, 0));

                        final int paddingLeft = view.getPaddingLeft();
                        final int paddingTop = view.getPaddingTop();
                        final int paddingRight = view.getPaddingRight();
                        final int paddingBottom = view.getPaddingBottom();

                        // Init background.
                        view.setBackground(drawable);

                        // Maintain horizontal and vertical padding.
                        if (paddingLeft > 0 && paddingRight > 0) {
                            view.setPadding(
                                    paddingLeft,
                                    view.getPaddingTop(),
                                    paddingRight,
                                    view.getPaddingBottom());
                        }

                        if (paddingTop > 0 && paddingBottom > 0) {
                            view.setPadding(
                                    view.getPaddingLeft(),
                                    paddingTop,
                                    view.getPaddingRight(),
                                    paddingBottom);
                        }
                    }
                } finally {
                    ta.recycle();
                }
            }
        },

        IMAGE_VIEW("ImageView", "src") {
            @Override
            public void initAttributes(Context context, MaterialResources resources, View view, AttributeSet set,
                                       int defStyle) {
                TypedArray ta = context.obtainStyledAttributes(set, R.styleable.MaterialImageView, defStyle, 0);
                try {
                    if (ta.hasValue(R.styleable.MaterialImageView_android_src)) {
                        Drawable drawable =
                                resources.getDrawable(ta.getResourceId(R.styleable.MaterialImageView_android_src, 0));
                        // Init image drawable.
                        ((ImageView) view).setImageDrawable(drawable);
                    }
                } finally {
                    ta.recycle();
                }
            }
        },

        TEXT_VIEW("TextView", "drawableLeft", "drawableTop", "drawableRight", "drawableBottom", "drawableStart",
                  "drawableEnd", "textCursorDrawable") {
            @Override
            public void initAttributes(Context context, MaterialResources resources, View view, AttributeSet set,
                                       int defStyle) {
                Drawable drawableLeft, drawableTop, drawableRight, drawableBottom, drawableStart, drawableEnd;
                drawableLeft = drawableTop = drawableRight = drawableBottom = drawableStart = drawableEnd = null;

                Drawable drawableTextCursor = null;
                int drawableTextCursorResId = 0;

                TypedArray ta = context.obtainStyledAttributes(set, R.styleable.MaterialTextView, defStyle, 0);
                try {
                    int N = ta.getIndexCount();
                    for (int i = 0; i < N; i++) {
                        int attr = ta.getIndex(i);
                        if (attr == R.styleable.MaterialTextView_android_drawableLeft) {
                            drawableLeft = resources.getDrawable(ta.getResourceId(attr, 0));
                        } else if (attr == R.styleable.MaterialTextView_android_drawableTop) {
                            drawableTop = resources.getDrawable(ta.getResourceId(attr, 0));
                        } else if (attr == R.styleable.MaterialTextView_android_drawableRight) {
                            drawableRight = resources.getDrawable(ta.getResourceId(attr, 0));
                        } else if (attr == R.styleable.MaterialTextView_android_drawableBottom) {
                            drawableBottom = resources.getDrawable(ta.getResourceId(attr, 0));
                        } else if (attr == R.styleable.MaterialTextView_android_drawableStart) {
                            drawableStart = resources.getDrawable(ta.getResourceId(attr, 0));
                        } else if (attr == R.styleable.MaterialTextView_android_drawableEnd) {
                            drawableEnd = resources.getDrawable(ta.getResourceId(attr, 0));
                        } else if (attr == R.styleable.MaterialTextView_android_textCursorDrawable) {
                            drawableTextCursorResId = ta.getResourceId(attr, 0);
                            drawableTextCursor = resources.getDrawable(drawableTextCursorResId);
                        }
                    }
                } finally {
                    ta.recycle();
                }

                TextView textView = (TextView) view;

                if (drawableLeft != null || drawableTop != null || drawableRight != null || drawableBottom != null) {
                    textView.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight,
                                                                     drawableBottom);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (drawableStart != null || drawableEnd != null) {
                        Drawable[] drawablesRelative = textView.getCompoundDrawablesRelative();
                        if (drawableStart == null) {
                            drawableStart = drawablesRelative[0];
                        }
                        if (drawableEnd == null) {
                            drawableEnd = drawablesRelative[2];
                        }
                        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableStart, drawablesRelative[1],
                                                                                 drawableEnd, drawablesRelative[2]);
                    }
                }

                if (drawableTextCursor != null) {
                    Object editor = ReflectionUtils.getDeclaredFieldValue(TextView.class, "mEditor", textView);
                    if (editor != null) {
                        // Replace cursor drawables in TextView's Editor.
                        Object cursorDrawables = ReflectionUtils.getDeclaredFieldValue(
                                ReflectionUtils.getClass("android.widget.Editor"),
                                "mCursorDrawable",
                                editor);
                        Array.set(cursorDrawables, 0, drawableTextCursor);
                        Array.set(cursorDrawables, 1, drawableTextCursor.getConstantState().newDrawable());

                        // Also set TextView#mCursorDrawableRes; Editor skips drawing the cursor if it's 0.
                        ReflectionUtils.setDeclaredFieldValue(
                                TextView.class,
                                "mCursorDrawableRes",
                                textView,
                                drawableTextCursorResId);
                    }
                }
            }
        },

        COMPOUND_BUTTON("CompoundButton", "button") {
            @Override
            public void initAttributes(Context context, MaterialResources resources, View view, AttributeSet set,
                                       int defStyle) {
                TypedArray ta = context.obtainStyledAttributes(set, R.styleable.MaterialCompoundButton, defStyle, 0);
                try {
                    if (ta.hasValue(R.styleable.MaterialCompoundButton_android_button)) {
                        Drawable drawable =
                                resources.getDrawable(
                                        ta.getResourceId(R.styleable.MaterialCompoundButton_android_button, 0));
                        // Init button drawable.
                        ((CompoundButton) view).setButtonDrawable(drawable);
                    }
                } finally {
                    ta.recycle();
                }
            }
        },

        CHECKED_TEXT_VIEW("CheckedTextView", "checkMark") {
            @Override
            public void initAttributes(Context context, MaterialResources resources, View view, AttributeSet set,
                                       int defStyle) {
                TypedArray ta = context.obtainStyledAttributes(set, R.styleable.MaterialCheckedTextView, defStyle, 0);
                try {
                    if (ta.hasValue(R.styleable.MaterialCheckedTextView_android_checkMark)) {
                        Drawable drawable = resources.getDrawable(
                                ta.getResourceId(R.styleable.MaterialCheckedTextView_android_checkMark, 0));
                        // Init checkmark.
                        ((CheckedTextView) view).setCheckMarkDrawable(drawable);
                    }
                } finally {
                    ta.recycle();
                }
            }
        },

        FRAME_LAYOUT("FrameLayout", "foreground") {
            @Override
            public void initAttributes(Context context, MaterialResources resources, View view, AttributeSet set,
                                       int defStyle) {
                TypedArray ta = context.obtainStyledAttributes(set, R.styleable.MaterialFrameLayout, defStyle, 0);
                try {
                    if (ta.hasValue(R.styleable.MaterialFrameLayout_android_foreground)) {
                        Drawable drawable =
                                resources.getDrawable(ta.getResourceId(R.styleable.MaterialFrameLayout_android_foreground, 0));
                        // Init foreground drawable.
                        ((FrameLayout) view).setForeground(drawable);
                    }
                } finally {
                    ta.recycle();
                }
            }
        },

        POPUP_WINDOW("PopupWindow", "popupBackground") {
            @Override
            public void initAttributes(Context context, MaterialResources resources, View view, AttributeSet set,
                                       int defStyle) {
                // No support needed.
            }
        },

        SPINNER("Spinner", "popupBackground") {
            @Override
            public void initAttributes(Context context, MaterialResources resources, View view, AttributeSet set,
                                       int defStyle) {
                TypedArray ta = context.obtainStyledAttributes(set, R.styleable.Spinner, defStyle, 0);
                try {
                    if (ta.hasValue(R.styleable.Spinner_android_popupBackground)) {
                        Drawable drawable = resources.getDrawable(
                                ta.getResourceId(R.styleable.Spinner_android_popupBackground, 0));
                        // Init popupBackground.
                        ((Spinner) view).setPopupBackgroundDrawable(drawable);
                    }
                } finally {
                    ta.recycle();
                }
            }
        };

        private static final Class<?> StyleableClass = ReflectionUtils.getClass("com.android.internal.R$styleable");

        private String mName;
        private String[] mHiddenValues;
        private int[] mOriginalStyleable;
        private int[] mHiddenStyleable;

        Styleable(String name, String... hiddenValues) {
            mName = name;
            mHiddenValues = new String[hiddenValues.length];
            for (int i = 0; i < hiddenValues.length; i++) {
                mHiddenValues[i] = name + "_" + hiddenValues[i];
            }
        }

        public void hide() {
            ensureStyleables();
            ReflectionUtils.setDeclaredFieldValue(
                    StyleableClass,
                    mName,
                    null,
                    mHiddenStyleable);
        }

        public void restore() {
            ensureStyleables();
            ReflectionUtils.setDeclaredFieldValue(StyleableClass, mName, null, mOriginalStyleable);
        }

        private void ensureStyleables() {
            if (mOriginalStyleable == null) {
                // Keep original styleable values.
                mOriginalStyleable = (int[]) ReflectionUtils.getDeclaredFieldValue(StyleableClass, mName, null);

                mHiddenStyleable = createHiddenStyleable(mOriginalStyleable, mHiddenValues);
            }
        }

        private static int[] createHiddenStyleable(int[] styleable, String... hiddenValues) {
            int[] newStyleable = new int[styleable.length];
            System.arraycopy(styleable, 0, newStyleable, 0, styleable.length);
            for (String hiddenValue : hiddenValues) {
                int hiddenIndex = (int) ReflectionUtils.getDeclaredFieldValue(StyleableClass, hiddenValue, null);
                // Replace the styleable's attribute references for hidden attributes.
                // Previously, the value used to replace those references was 0.
                // However, 0 is the attr reference for the 'style' attribute.
                // Through trial and error, android.R.attr.value was picked,
                // as it is ignored by Theme#obtainStyledAttributes.
                newStyleable[hiddenIndex] = android.R.attr.value;
            }
            return newStyleable;
        }

        public abstract void initAttributes(Context context, MaterialResources resources, View view, AttributeSet set,
                                            int defStyle);
    }
}
