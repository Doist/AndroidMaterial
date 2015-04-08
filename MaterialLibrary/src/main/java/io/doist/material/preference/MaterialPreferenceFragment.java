package io.doist.material.preference;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.doist.material.R;

/**
 * A {@link PreferenceFragment} with a simplified layout on pre-Lollipop.
 */
public class MaterialPreferenceFragment extends PreferenceFragment {
    private static final boolean SKIP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (SKIP) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        return inflater.inflate(R.layout.preference_fragment, container, false);
    }
}
