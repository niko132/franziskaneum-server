package de.franziskaneum.utils;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niko on 19.01.2017.
 */

public class ProgressOutputStream extends OutputStream {
    private OutputStream underlying;
    @Nullable
    private Listener listener;
    private long completed;
    private long totalSize;

    public ProgressOutputStream(long totalSize, @Nullable OutputStream underlying, Listener listener) {
        this.underlying = underlying;
        this.listener = listener;
        this.completed = 0;
        this.totalSize = totalSize;
    }

    @Override
    public void write(byte[] data, int off, int len) throws IOException {
        this.underlying.write(data, off, len);
        track(len);
    }

    @Override
    public void write(byte[] data) throws IOException {
        this.underlying.write(data);
        track(data.length);
    }

    @Override
    public void write(int c) throws IOException {
        this.underlying.write(c);
        track(1);
    }

    private void track(int len) {
        this.completed += len;
        if (listener != null)
            this.listener.progress(this.completed, this.totalSize);
    }

    public interface Listener {
        void progress(long completed, long totalSize);
    }
}