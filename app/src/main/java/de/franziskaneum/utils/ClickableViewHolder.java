package de.franziskaneum.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Niko on 11.01.2017.
 */

public abstract class ClickableViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public ClickableViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
    }

    @Override
    public final void onClick(View v) {
        onClick(getAdapterPosition(), v.getContext());
    }

    public abstract void onClick(int position, @Nullable Context context);
}
