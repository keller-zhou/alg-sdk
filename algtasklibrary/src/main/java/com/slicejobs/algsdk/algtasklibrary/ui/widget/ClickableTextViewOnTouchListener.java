package com.slicejobs.algsdk.algtasklibrary.ui.widget;

import android.text.Layout;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


public class ClickableTextViewOnTouchListener implements View.OnTouchListener {

    private boolean find = false;
    private ClickableSpan matchedSpan = null;
    private TextView widget;

    public ClickableTextViewOnTouchListener(TextView widget) {
        this.widget = widget;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Layout layout = ((TextView) v).getLayout();

        if (layout == null) {
            return false;
        }

        int x = (int) event.getX();
        int y = (int) event.getY();

        int line = layout.getLineForVertical(y);
        int offset = layout.getOffsetForHorizontal(line, x);

        TextView tv = (TextView) v;
        SpannableString value = SpannableString.valueOf(tv.getText());
        ClickableSpan[] urlSpans = value.getSpans(0, value.length(), ClickableSpan.class);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                for (ClickableSpan urlSpan : urlSpans) {
                    int start = value.getSpanStart(urlSpan);
                    int end = value.getSpanEnd(urlSpan);
                    if (start <= offset && offset <= end) {
                        matchedSpan = urlSpan;
                        find = true;
                        break;
                    }
                }
                float lineWidth = layout.getLineWidth(line);
                find &= (lineWidth >= x);
                return find;
            case MotionEvent.ACTION_UP:
                if (find) {
                    for (ClickableSpan urlSpan : urlSpans) {
                        int start = value.getSpanStart(urlSpan);
                        int end = value.getSpanEnd(urlSpan);
                        if (start <= offset && offset <= end) {
                            if (urlSpan.equals(matchedSpan)) {
                                urlSpan.onClick(widget);
                                find = false;
                            }
                            break;
                        }
                    }
                }
                return find;
            default:
                return false;
        }
    }
}
