package de.franziskaneum.views;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.franziskaneum.teacher.TeacherDetailActivity;
import de.franziskaneum.teacher.TeacherList;

/**
 * Created by Niko on 03.03.2016.
 */
public class TeacherLinkTextView extends android.support.v7.widget.AppCompatTextView {

    private TeacherList teacherList;

    public TeacherLinkTextView(Context context) {
        super(context);
    }

    public TeacherLinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TeacherLinkTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTeacherList(@NonNull TeacherList teacherList) {
        this.teacherList = teacherList;
        setMovementMethod(new LinkMovementMethod());
        new LinkerTask().execute(getText());
    }

    @Override
    public void setText(@Nullable CharSequence text, BufferType type) {
        if (text != null && getText() != null && !text.toString().equals(getText().toString())) {
            super.setText(text, type);
            if (teacherList != null)
                new LinkerTask().execute(text);
        } else {
            super.setText(text, type);
        }
    }

    private void setTextWithoutLinking(CharSequence text) {
        super.setText(text, BufferType.SPANNABLE);
    }

    private class LinkerTask extends AsyncTask<CharSequence, Void, Spannable> {

        private Handler handler;

        LinkerTask() {
            super();
            handler = new Handler(Looper.getMainLooper());
        }

        @Override
        protected Spannable doInBackground(CharSequence... text) {
            final String stringText = text[0].toString();
            final Spannable spannableText;

            if (text[0] instanceof Spannable)
                spannableText = (Spannable) text[0];
            else
                spannableText = SpannableString.valueOf(text[0]);

            final List<Object[]> spans = new ArrayList<>();

            for (final TeacherList.TeacherData teacher : teacherList) {
                final String teacherShortcut = teacher.getShortcut() == null ? "" :
                        teacher.getShortcut();
                final String teacherName = teacher.getName() == null ? "" : teacher.getName();

                int searchStart = 0;

                while (true) {
                    int teacherShortcutIndex = stringText.indexOf(teacherShortcut, searchStart);
                    int teacherNameIndex = stringText.indexOf(teacherName, searchStart);

                    int teacherShortcutEnd = teacherShortcutIndex + teacherShortcut.length();
                    int teacherNameEnd = teacherNameIndex + teacherName.length();

                    final int end;
                    final int start;

                    final ClickableSpan clickSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(view.getContext(),
                                    TeacherDetailActivity.class);
                            intent.putExtra(TeacherDetailActivity.EXTRA_TEACHER, teacher);
                            view.getContext().startActivity(intent);
                        }
                    };

                    if ((teacherShortcutIndex != -1 && (teacherNameIndex == -1 || teacherShortcutIndex <= teacherNameIndex))
                            && ((teacherShortcutIndex == 0
                            || (!Character.isLetter(stringText.charAt(teacherShortcutIndex - 1))
                            && stringText.charAt(teacherShortcutIndex - 1) != '.'))
                            && (teacherShortcutEnd == stringText.length()
                            || (!Character.isLetter(stringText.charAt(teacherShortcutEnd))
                            && stringText.charAt(teacherShortcutEnd) != '.')))
                            && teacherShortcutIndex != teacherShortcutEnd) { // if shortcut is null or empty
                        start = teacherShortcutIndex;
                        end = teacherShortcutEnd;

                        if (stringText.length() >= end + 3 && stringText.charAt(end) == ' ' && stringText.charAt(end + 1) == '(' && stringText.charAt(end + 3) == ')' && Character.isDigit(stringText.charAt(end + 2))) {
                            ClickableSpan clickableSpan = new ClickableSpan() {
                                @Override
                                public void onClick(View widget) {
                                    clickSpan.onClick(widget);
                                }

                                @Override
                                public void updateDrawState(TextPaint ds) {
                                    super.updateDrawState(ds);
                                    ds.setUnderlineText(false);
                                }
                            };

                            spans.add(new Object[] {clickableSpan, start, end + 4});
                            spans.add(new Object[] {new UnderlineSpan(), start, end});
                        } else {
                            spans.add(new Object[] {clickSpan, start, end});
                        }

                        searchStart = end;
                    } else if (teacherNameIndex != -1
                            && ((teacherNameIndex == 0
                            || (!Character.isLetter(stringText.charAt(teacherNameIndex - 1))
                            && stringText.charAt(teacherNameIndex - 1) != '.'))
                            && (teacherNameEnd == stringText.length()
                            || (!Character.isLetter(stringText.charAt(teacherNameEnd))
                            && stringText.charAt(teacherNameEnd) != '.')))
                            && teacherNameIndex != teacherNameEnd) { // if name is null or empty
                        start = teacherNameIndex;
                        end = teacherNameEnd;

                        if (stringText.length() >= end + 3 && stringText.charAt(end) == ' ' && stringText.charAt(end + 1) == '(' && stringText.charAt(end + 3) == ')' && Character.isDigit(stringText.charAt(end + 2))) {
                            ClickableSpan clickableSpan = new ClickableSpan() {
                                @Override
                                public void onClick(View widget) {
                                    clickSpan.onClick(widget);
                                }

                                @Override
                                public void updateDrawState(TextPaint ds) {
                                    super.updateDrawState(ds);
                                    ds.setUnderlineText(false);
                                }
                            };

                            spans.add(new Object[] {clickableSpan, start, end + 4});
                            spans.add(new Object[] {new UnderlineSpan(), start, end});
                        } else {
                            spans.add(new Object[] {clickSpan, start, end});
                        }

                        searchStart = end;
                    } else
                        break;
                }
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < spans.size(); i++) {
                        Object[] span = spans.get(i);

                        spannableText.setSpan(span[0], (Integer) span[1], (Integer) span[2],
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            });

            return spannableText;
        }

        @Override
        protected void onPostExecute(Spannable spannable) {
            super.onPostExecute(spannable);
            TeacherLinkTextView.this.setTextWithoutLinking(spannable);
        }
    }

}
