package com.slicejobs.algsdk.algtasklibrary.utils;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.widget.TextView;


import com.slicejobs.algsdk.algtasklibrary.ui.widget.ClickableTextViewOnTouchListener;
import com.slicejobs.algsdk.algtasklibrary.ui.widget.MyURLSpan;

import java.util.regex.Pattern;

/**
 * Created by nlmartian on 8/23/15.
 */
public class TextUtil {
    public static final int INDEX_NOT_FOUND = -1;

    public static final Pattern WEB_URL = Pattern
            .compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]");
    public static final String WEB_SCHEME = "http://";

    public static SpannableString convertNormalStringToSpannableString(String txt) {
        //hack to fix android imagespan bug,see http://stackoverflow.com/questions/3253148/imagespan-is-cut-off-incorrectly-aligned
        //if string only contains emotion tags,add a empty char to the end
        String hackTxt;
        if (txt.startsWith("[") && txt.endsWith("]")) {
            hackTxt = txt + " ";
        } else {
            hackTxt = txt;
        }
        SpannableString value = SpannableString.valueOf(hackTxt);
        Linkify.addLinks(value, WEB_URL, WEB_SCHEME);

        URLSpan[] urlSpans = value.getSpans(0, value.length(), URLSpan.class);
        MyURLSpan weiboSpan = null;
        for (URLSpan urlSpan : urlSpans) {
            weiboSpan = new MyURLSpan(urlSpan.getURL());
            int start = value.getSpanStart(urlSpan);
            int end = value.getSpanEnd(urlSpan);
            value.removeSpan(urlSpan);
            value.setSpan(weiboSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return value;
    }

    public static Spannable formatFromHtml(Spanned s) {
        Spannable result = new SpannableStringBuilder(s);
        // replace URLSpan
        URLSpan[] urlSpans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan urlSpan : urlSpans) {
            int start = s.getSpanStart(urlSpan);
            int end = s.getSpanEnd(urlSpan);
            result.removeSpan(urlSpan);
            result.setSpan(new MyURLSpan(urlSpan.getURL()), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return result;
    }

    public static String html(String content) {
        if (content == null) return "";
        String html = content;
        html = replace(html, "&lt;", "<");
        html = replace(html, "&gt;", ">");
        html = replace(html, "&apos;", "'");
        html = replace(html, "&quot;", "\"");
        html = replace(html, "&nbsp;&nbsp;", "\t");// 替换跳格
        html = replace(html, "&nbsp;", " ");// 替换空格
        html = replace(html, "&amp;", "&");
        return html;
    }

    public static String replaceBr(String content) {
        if (content == null) return "";
        String newContent = replace(content, "\n", "<br/>");
        return newContent;
    }

    public static String replace(final String text, final String searchString, final String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    public static String replace(final String text, final String searchString, final String replacement, int max) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        final int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = increase < 0 ? 0 : increase;
        increase *= max < 0 ? 16 : max > 64 ? 64 : max;
        final StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text.substring(start, end)).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static void textViewDecode(TextView textView, String content) {
        if (StringUtil.isNotBlank(content)) {
            String strHtml = TextUtil.html(content);
            String strHtmlWithBr = TextUtil.replaceBr(strHtml);
            Spannable htmlSpannable = TextUtil.formatFromHtml(Html.fromHtml(strHtmlWithBr));
            textView.setText(htmlSpannable);
        }
        textView.setOnTouchListener(new ClickableTextViewOnTouchListener(textView));

    }

    public static String replaceEnter(String content) {
        if (content == null) return "";
        String newContent = replace(content, "\n", ",");
        return newContent;
    }

    public static String replaceTranslation(String sourceContent) {//替换部分转译
        if (sourceContent == null) return "";
        String content = sourceContent;
        content = replace(content, "\t", " ");
        content = replace(content, "\n", ",");
        content = replace(content, "\\\\", ",");
        content = replace(content, "\"", " ");
        return content;
    }


}
