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

    private static final Class<?> StyleableClass = ReflectionUtils.getClass("com.android.internal.R$styleable");

    public static final String STYLEABLE_VIEW = "View";
    public static final String STYLEABLE_IMAGE_VIEW = "ImageView";
    public static final String STYLEABLE_TEXT_VIEW = "TextView";
    public static final String STYLEABLE_COMPOUND_BUTTON = "CompoundButton";
    public static final String STYLEABLE_CHECKED_TEXT_VIEW = "CheckedTextView";
    public static final String STYLEABLE_FRAME_LAYOUT = "FrameLayout";
    public static final String STYLEABLE_POPUP_WINDOW = "PopupWindow";
    public static final String STYLEABLE_SPINNER = "Spinner";

    private static int[] sOriginalViewStyleable;
    private static int[] sHiddenViewStyleable;
    private static int[] sOriginalImageViewStyleable;
    private static int[] sHiddenImageViewStyleable;
    private static int[] sOriginalTextViewStyleable;
    private static int[] sHiddenTextViewStyleable;
    private static int[] sOriginalCompoundButtonStyleable;
    private static int[] sHiddenCompoundButtonStyleable;
    private static int[] sOriginalCheckedTextViewStyleable;
    private static int[] sHiddenCheckedTextViewStyleable;
    private static int[] sOriginalFrameLayoutStyleable;
    private static int[] sHiddenFrameLayoutStyleable;
    private static int[] sOriginalPopupWindowStyleable;
    private static int[] sHiddenPopupWindowStyleable;
    private static int[] sOriginalSpinnerStyleable;
    private static int[] sHiddenSpinnerStyleable;

    public static AttributeSet hideStyleableAttributes(AttributeSet set, String... styleables) {
        if (sSkip) {
            return set;
        }

        for (String styleable : styleables) {
            switch (styleable) {
                case STYLEABLE_VIEW:
                    if (sOriginalViewStyleable == null) {
                        // Keep original view styleable values.
                        sOriginalViewStyleable =
                                (int[]) ReflectionUtils.getDeclaredFieldValue(StyleableClass, STYLEABLE_VIEW, null);

                        sHiddenViewStyleable = createHiddenStyleable(sOriginalViewStyleable, "View_background");

                    }

                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass,
                            STYLEABLE_VIEW,
                            null,
                            sHiddenViewStyleable);
                    break;

                case STYLEABLE_IMAGE_VIEW:
                    if (sOriginalImageViewStyleable == null) {
                        // Keep original image view styleable values.
                        sOriginalImageViewStyleable =
                                (int[]) ReflectionUtils
                                        .getDeclaredFieldValue(StyleableClass, STYLEABLE_IMAGE_VIEW, null);

                        sHiddenImageViewStyleable = createHiddenStyleable(sOriginalImageViewStyleable, "ImageView_src");
                    }

                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass,
                            STYLEABLE_IMAGE_VIEW,
                            null,
                            sHiddenImageViewStyleable);
                    break;

                case STYLEABLE_TEXT_VIEW:
                    if (sOriginalTextViewStyleable == null) {
                        // Keep original image view styleable values.
                        sOriginalTextViewStyleable =
                                (int[]) ReflectionUtils
                                        .getDeclaredFieldValue(StyleableClass, STYLEABLE_TEXT_VIEW, null);

                        sHiddenTextViewStyleable = createHiddenStyleable(
                                sOriginalTextViewStyleable,
                                "TextView_drawableLeft",
                                "TextView_drawableTop",
                                "TextView_drawableRight",
                                "TextView_drawableBottom",
                                "TextView_drawableStart",
                                "TextView_drawableEnd",
                                "TextView_textCursorDrawable");
                    }

                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass,
                            STYLEABLE_TEXT_VIEW,
                            null,
                            sHiddenTextViewStyleable);
                    break;

                case STYLEABLE_COMPOUND_BUTTON:
                    if (sOriginalCompoundButtonStyleable == null) {
                        // Keep original button styleable values.
                        sOriginalCompoundButtonStyleable =
                                (int[]) ReflectionUtils
                                        .getDeclaredFieldValue(StyleableClass, STYLEABLE_COMPOUND_BUTTON, null);

                        sHiddenCompoundButtonStyleable =
                                createHiddenStyleable(sOriginalCompoundButtonStyleable, "CompoundButton_button");
                    }

                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass,
                            STYLEABLE_COMPOUND_BUTTON,
                            null,
                            sHiddenCompoundButtonStyleable);
                    break;

                case STYLEABLE_CHECKED_TEXT_VIEW:
                    if (sOriginalCheckedTextViewStyleable == null) {
                        // Keep original styleable values.
                        sOriginalCheckedTextViewStyleable =
                                (int[]) ReflectionUtils
                                        .getDeclaredFieldValue(StyleableClass, STYLEABLE_CHECKED_TEXT_VIEW, null);

                        sHiddenCheckedTextViewStyleable =
                                createHiddenStyleable(sOriginalCheckedTextViewStyleable, "CheckedTextView_checkMark");
                    }

                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass,
                            STYLEABLE_CHECKED_TEXT_VIEW,
                            null,
                            sHiddenCheckedTextViewStyleable);
                    break;

                case STYLEABLE_FRAME_LAYOUT:
                    if (sOriginalFrameLayoutStyleable == null) {
                        // Keep original styleable values.
                        sOriginalFrameLayoutStyleable =
                                (int[]) ReflectionUtils.getDeclaredFieldValue(StyleableClass, STYLEABLE_FRAME_LAYOUT,
                                                                              null);

                        sHiddenFrameLayoutStyleable =
                                createHiddenStyleable(sOriginalFrameLayoutStyleable, "FrameLayout_foreground");

                    }

                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass,
                            STYLEABLE_FRAME_LAYOUT,
                            null,
                            sHiddenFrameLayoutStyleable);
                    break;

                case STYLEABLE_POPUP_WINDOW:
                    if (sOriginalPopupWindowStyleable == null) {
                        // Keep original image view styleable values.
                        sOriginalPopupWindowStyleable =
                                (int[]) ReflectionUtils
                                        .getDeclaredFieldValue(StyleableClass, STYLEABLE_POPUP_WINDOW, null);

                        sHiddenPopupWindowStyleable =
                                createHiddenStyleable(sOriginalPopupWindowStyleable, "PopupWindow_popupBackground");
                    }

                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass,
                            STYLEABLE_POPUP_WINDOW,
                            null,
                            sHiddenPopupWindowStyleable);
                    break;

                case STYLEABLE_SPINNER:
                    if (sOriginalSpinnerStyleable == null) {
                        // Keep original image view styleable values.
                        sOriginalSpinnerStyleable =
                                (int[]) ReflectionUtils
                                        .getDeclaredFieldValue(StyleableClass, STYLEABLE_SPINNER, null);

                        sHiddenSpinnerStyleable =
                                createHiddenStyleable(sOriginalSpinnerStyleable, "Spinner_popupBackground");
                    }

                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass,
                            STYLEABLE_SPINNER,
                            null,
                            sHiddenSpinnerStyleable);
                    break;
            }
        }
        return set;
    }

    public static void restoreStyleableAttributes(String... styleables) {
        if (sSkip) {
            return;
        }

        for (String styleable : styleables) {
            switch (styleable) {
                case STYLEABLE_VIEW:
                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass, STYLEABLE_VIEW, null, sOriginalViewStyleable);
                    break;

                case STYLEABLE_IMAGE_VIEW:
                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass, STYLEABLE_IMAGE_VIEW, null, sOriginalImageViewStyleable);
                    break;

                case STYLEABLE_TEXT_VIEW:
                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass, STYLEABLE_TEXT_VIEW, null, sOriginalTextViewStyleable);
                    break;

                case STYLEABLE_COMPOUND_BUTTON:
                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass, STYLEABLE_COMPOUND_BUTTON, null, sOriginalCompoundButtonStyleable);
                    break;

                case STYLEABLE_CHECKED_TEXT_VIEW:
                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass, STYLEABLE_CHECKED_TEXT_VIEW, null, sOriginalCheckedTextViewStyleable);
                    break;

                case STYLEABLE_FRAME_LAYOUT:
                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass, STYLEABLE_FRAME_LAYOUT, null, sOriginalFrameLayoutStyleable);
                    break;

                case STYLEABLE_POPUP_WINDOW:
                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass, STYLEABLE_POPUP_WINDOW, null, sOriginalPopupWindowStyleable);
                    break;

                case STYLEABLE_SPINNER:
                    ReflectionUtils.setDeclaredFieldValue(
                            StyleableClass, STYLEABLE_SPINNER, null, sOriginalSpinnerStyleable);
                    break;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void init(View view, AttributeSet set, int defStyle, String[] styleables) {
        if (sSkip) {
            return;
        }

        Context context = themifyContext(view.getContext(), set);
        MaterialResources resources = MaterialResources.getInstance(context, context.getResources());
        for (String styleable : styleables) {
            switch (styleable) {
                case STYLEABLE_VIEW:
                    initViewAttributes(context, resources, view, set, defStyle);
                    break;

                case STYLEABLE_IMAGE_VIEW:
                    initImageViewAttributes(context, resources, (ImageView) view, set, defStyle);
                    break;

                case STYLEABLE_TEXT_VIEW:
                    initTextViewAttributes(context, resources, (TextView) view, set, defStyle);
                    break;

                case STYLEABLE_COMPOUND_BUTTON:
                    initCompoundButtonAttributes(context, resources, (CompoundButton) view, set, defStyle);
                    break;

                case STYLEABLE_CHECKED_TEXT_VIEW:
                    initCheckedTextViewAttributes(context, resources, (CheckedTextView) view, set, defStyle);
                    break;

                case STYLEABLE_FRAME_LAYOUT:
                    initFrameLayoutAttributes(context, resources, (FrameLayout) view, set, defStyle);
                    break;

                case STYLEABLE_POPUP_WINDOW:
                    // No support needed.
                    break;

                case STYLEABLE_SPINNER:
                    initSpinnerAttributes(context, resources, (Spinner) view, set, defStyle);
                    break;
            }
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

    private static void initViewAttributes(Context context, MaterialResources resources, View view,
                                           AttributeSet set, int defStyle) {
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

    private static void initImageViewAttributes(Context context, MaterialResources resources, ImageView imageView,
                                                AttributeSet set, int defStyle) {
        TypedArray ta = context.obtainStyledAttributes(set, R.styleable.MaterialImageView, defStyle, 0);
        try {
            if (ta.hasValue(R.styleable.MaterialImageView_android_src)) {
                Drawable drawable =
                        resources.getDrawable(ta.getResourceId(R.styleable.MaterialImageView_android_src, 0));
                // Init image drawable.
                imageView.setImageDrawable(drawable);
            }
        } finally {
            ta.recycle();
        }
    }

    private static void initTextViewAttributes(Context context, MaterialResources resources, TextView textView,
                                               AttributeSet set, int defStyle) {
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

        if (drawableLeft != null || drawableTop != null || drawableRight != null || drawableBottom != null) {
            textView.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
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

    private static void initCompoundButtonAttributes(Context context, MaterialResources resources,
                                                     CompoundButton compoundButton, AttributeSet set, int defStyle) {
        TypedArray ta = context.obtainStyledAttributes(set, R.styleable.MaterialCompoundButton, defStyle, 0);
        try {
            if (ta.hasValue(R.styleable.MaterialCompoundButton_android_button)) {
                Drawable drawable =
                        resources.getDrawable(ta.getResourceId(R.styleable.MaterialCompoundButton_android_button, 0));
                // Init button drawable.
                compoundButton.setButtonDrawable(drawable);
            }
        } finally {
            ta.recycle();
        }
    }

    private static void initCheckedTextViewAttributes(Context context, MaterialResources resources,
                                                      CheckedTextView checkedTextView, AttributeSet set, int defStyle) {
        TypedArray ta = context.obtainStyledAttributes(set, R.styleable.MaterialCheckedTextView, defStyle, 0);
        try {
            if (ta.hasValue(R.styleable.MaterialCheckedTextView_android_checkMark)) {
                Drawable drawable = resources.getDrawable(
                        ta.getResourceId(R.styleable.MaterialCheckedTextView_android_checkMark, 0));
                // Init checkmark.
                checkedTextView.setCheckMarkDrawable(drawable);
            }
        } finally {
            ta.recycle();
        }
    }

    private static void initFrameLayoutAttributes(Context context, MaterialResources resources, FrameLayout frameLayout,
                                                  AttributeSet set, int defStyle) {
        TypedArray ta = context.obtainStyledAttributes(set, R.styleable.MaterialFrameLayout, defStyle, 0);
        try {
            if (ta.hasValue(R.styleable.MaterialFrameLayout_android_foreground)) {
                Drawable drawable =
                        resources.getDrawable(ta.getResourceId(R.styleable.MaterialFrameLayout_android_foreground, 0));
                // Init foreground drawable.
                frameLayout.setForeground(drawable);
            }
        } finally {
            ta.recycle();
        }
    }

    private static void initSpinnerAttributes(Context context, MaterialResources resources, Spinner spinner,
                                              AttributeSet set, int defStyle) {
        TypedArray ta = context.obtainStyledAttributes(set, R.styleable.Spinner, defStyle, 0);
        try {
            if (ta.hasValue(R.styleable.Spinner_android_popupBackground)) {
                Drawable drawable = resources.getDrawable(
                        ta.getResourceId(R.styleable.Spinner_android_popupBackground, 0));
                // Init popupBackground.
                spinner.setPopupBackgroundDrawable(drawable);
            }
        } finally {
            ta.recycle();
        }
    }

    private static int[] createHiddenStyleable(int[] styleable, String... hiddenValues) {
        int[] newStyleable = new int[styleable.length];
        System.arraycopy(styleable, 0, newStyleable, 0, styleable.length);
        for (String hiddenValue : hiddenValues) {
            int hiddenIndex = (int) ReflectionUtils.getDeclaredFieldValue(StyleableClass, hiddenValue, null);
            newStyleable[hiddenIndex] = 0;
        }
        return newStyleable;
    }
}
