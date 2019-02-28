package de.franziskaneum.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import de.franziskaneum.utils.LeadingMarginSpan;

/**
 * Created by Niko on 22.01.2017.
 */

public class FlowTextView extends TextView {
    private int mImageWidth = 0;
    private int mImageHeight = 0;

    private boolean needsMeasure = false;
    private SpannableString spannable;

    public FlowTextView(Context context) {
        super(context);
    }

    public FlowTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FlowTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setImageSize(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        needsMeasure = true;
        spannable = new SpannableString(text);
        super.setText(text, type);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (needsMeasure) {
            int width = getMeasuredWidth();

            int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width - mImageWidth, MeasureSpec.EXACTLY);
            int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

            super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);

            float lineHeight = getPaint().getTextSize();
            int lines = (int) Math.ceil(mImageHeight / lineHeight);

            int linesCount = getLayout().getLineCount();

            for (LeadingMarginSpan span : spannable.getSpans(0, spannable.length(), LeadingMarginSpan.class))
                spannable.removeSpan(span);

            if (linesCount <= lines) {
                spannable.setSpan(new LeadingMarginSpan(lines, mImageWidth), 0, spannable.length(), 0);
                setSpannableText(spannable);
            } else {
                // find the breakpoint where to break the String.
                int breakpoint = getLayout().getLineEnd(lines - 1);

                Spannable s1 = new SpannableStringBuilder(spannable, 0, breakpoint);
                s1.setSpan(new LeadingMarginSpan(lines, mImageWidth), 0, s1.length(), 0);
                Spannable s2 = new SpannableStringBuilder(System.getProperty("line.separator"));
                Spannable s3 = new SpannableStringBuilder(spannable, breakpoint, spannable.length());
                // It is needed to set a zero-margin span on for the text under the image to prevent the space on the right!
                s3.setSpan(new LeadingMarginSpan(0, 0), 0, s3.length(), 0);
                setSpannableText(TextUtils.concat(s1, s2, s3));
            }

            needsMeasure = false;
        }
    }

    private void setSpannableText(CharSequence text) {
        super.setText(text, BufferType.SPANNABLE);
    }
}
