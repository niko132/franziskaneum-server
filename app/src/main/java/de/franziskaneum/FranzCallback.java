package de.franziskaneum;

import android.support.annotation.Nullable;

/**
 * Created by Niko on 22.12.2016.
 */

public interface FranzCallback {
    void onCallback(int status, Object... objects);
}
