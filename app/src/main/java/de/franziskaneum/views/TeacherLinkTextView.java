package de.franziskaneum.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import de.franziskaneum.teacher.TeacherDetailActivity;
import de.franziskaneum.teacher.TeacherList;

/**
 * Created by Niko on 03.03.2016.
 */
public class TeacherLinkTextView extends TextView {

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TeacherLinkTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setTeacherList(@NonNull TeacherList teacherList) {
        this.teacherList = teacherList;
        setMovementMethod(new LinkMovementMethod());
        new LinkerTask().execute(getText());
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!text.toString().equals(getText().toString())) {
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

        public LinkerTask() {
            super();
            handler = new Handler(Looper.getMainLooper());
        }

        @Override
        protected Spannable doInBackground(CharSequence... text) {
            String stringText = text[0].toString();
            final Spannable spannableText;

            if (text[0] instanceof Spannable)
                spannableText = (Spannable) text[0];
            else
                spannableText = SpannableString.valueOf(text[0]);

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

                    if ((teacherShortcutIndex != -1 && ((teacherNameIndex != -1
                            && teacherShortcutIndex <= teacherNameIndex) || teacherNameIndex == -1))
                            && ((teacherShortcutIndex == 0
                            || (!Character.isLetter(stringText.charAt(teacherShortcutIndex - 1))
                            && stringText.charAt(teacherShortcutIndex - 1) != '.'))
                            && (teacherShortcutEnd == stringText.length()
                            || (!Character.isLetter(stringText.charAt(teacherShortcutEnd))
                            && stringText.charAt(teacherShortcutEnd) != '.')))
                            && teacherShortcutIndex != teacherShortcutEnd) { // if shortcut is null or empty
                        start = teacherShortcutIndex;
                        end = teacherShortcutEnd;
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
                        searchStart = end;
                    } else
                        break;

                    final ClickableSpan clickSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(view.getContext(),
                                    TeacherDetailActivity.class);
                            intent.putExtra(TeacherDetailActivity.EXTRA_TEACHER, teacher);
                            view.getContext().startActivity(intent);
                        }
                    };

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            spannableText.setSpan(clickSpan, start, end,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    });
                }
            }

            return spannableText;
        }

        @Override
        protected void onPostExecute(Spannable spannable) {
            super.onPostExecute(spannable);
            TeacherLinkTextView.this.setTextWithoutLinking(spannable);
        }
    }

}
