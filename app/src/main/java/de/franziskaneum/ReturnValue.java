package de.franziskaneum;

/**
 * Created by Niko on 25.12.2016.
 */

public class ReturnValue {
    public int status;
    public Object[] objects;

    public ReturnValue(int status, Object... objects) {
        this.status = status;
        this.objects = objects;
    }
}
