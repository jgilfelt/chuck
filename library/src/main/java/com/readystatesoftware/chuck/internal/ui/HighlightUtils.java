package com.readystatesoftware.chuck.internal.ui;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;

import java.text.Collator;

class HighlightUtils {

    static void highlight(TextView textView, String word) {
        CharSequence currentText = textView.getText();
        if (word == null || TextUtils.isEmpty(currentText)) {
            return;
        }
        textView.setText(createHighlightedText(word, currentText));
    }

    private static SpannableString createHighlightedText(String word, CharSequence currentText) {
        String originalRawText = currentText.toString();
        SpannableString highlightedText = new SpannableString(currentText);
        for (BackgroundColorSpan span : highlightedText.getSpans(0, highlightedText.length(), BackgroundColorSpan.class)) {
            highlightedText.removeSpan(span);
        }

        if (!word.isEmpty()) {
            Collator collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);
            collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);

            for (int wordStart = 0; wordStart <= currentText.length() - word.length(); wordStart++) {
                final int wordEnd = wordStart + word.length();

                if (collator.equals(word, originalRawText.substring(wordStart, wordEnd))) {
                    highlightedText.setSpan(new BackgroundColorSpan(Color.YELLOW), wordStart, wordEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return highlightedText;
    }

}
