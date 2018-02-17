package com.readystatesoftware.chuck.internal.support;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by waleed on 17/02/2018.
 */

public class SearchHighlightUtil {

    public static SpannableStringBuilder format(String text, String criteria) {
        List<Integer> startIndexes = indexOf(text, criteria);
        return applySpannable(text, startIndexes, criteria.length());
    }

    private static List<Integer> indexOf(String text, String criteria) {
        List<Integer> startPositions = new ArrayList<>();
        int index = text.indexOf(criteria);
        do {
            startPositions.add(index);
            index = text.indexOf(criteria, index + 1);
        } while (index >= 0);
        return startPositions;
    }

    private static SpannableStringBuilder applySpannable(String text, List<Integer> indexes, int length) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        for (Integer position : indexes) {
            builder.setSpan(new UnderlineSpan(),
                    position, position + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new ForegroundColorSpan(Color.RED),
                    position, position + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new BackgroundColorSpan(Color.YELLOW),
                    position, position + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

}