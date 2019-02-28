package de.franziskaneum.utils;

import android.text.Editable;
import android.text.Html;

import org.xml.sax.XMLReader;

import java.util.Vector;

public class HtmlTagHandler implements Html.TagHandler {
    private Vector<String> mListParents = new Vector<>();
	private Vector<Integer> mListCounter = new Vector<>();

	@Override
	public void handleTag(final boolean opening, final String tag,
			Editable output, final XMLReader xmlReader) {

		if (tag.equals("ul") || tag.equals("ol")) {
			if (opening) {
				mListParents.add(mListParents.size(), tag);
				mListCounter.add(mListCounter.size(), 0);
			} else {
				mListParents.removeElementAt(mListParents.size() - 1);
				mListCounter.removeElementAt(mListCounter.size() - 1);
			}

		} else if (tag.equals("li") && opening) {
			handleListTag(output);
		}

	}

	private void handleListTag(Editable output) {
		if (mListParents.lastElement().equals("ul")) {
			if (output.length() != 0)
				output.append("\n");
			for (int i = 1; i < mListCounter.size(); i++)
				output.append("\t");

			output.append("• ");
		} else if (mListParents.lastElement().equals("ol")) {
            int mListItemCount = mListCounter.lastElement() + 1;
			if (output.length() != 0)
				output.append("\n");
			for (int i = 1; i < mListCounter.size(); i++)
				output.append("\t");
			output.append(String.valueOf(mListItemCount));
			output.append(". ");
			mListCounter.removeElementAt(mListCounter.size() - 1);
			mListCounter.add(mListCounter.size(), mListItemCount);
		}
	}

}