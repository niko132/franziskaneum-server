package de.franziskaneum.views;

import android.content.Context;
import android.os.Parcelable;
import android.support.design.widget.NavigationView;
import android.util.AttributeSet;

/**
 * Created by Niko on 11.04.2016.
 */
public class FixedNavigationView extends NavigationView {

    public FixedNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FixedNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedNavigationView(Context context) {
        super(context);
    }

    @Override
    final protected void onRestoreInstanceState(Parcelable savedState) {
        if (savedState != null) {
            SavedState state = (SavedState) savedState;

            if (state.menuState != null)
                state.menuState.setClassLoader(getContext().getClass().getClassLoader());
        }

        super.onRestoreInstanceState(savedState);
    }
}
