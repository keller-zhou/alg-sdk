package com.slicejobs.algsdk.algtasklibrary.view;

import android.support.v7.widget.CardView;

public interface ICardView {
    int MAX_ELEVATION_FACTOR = 4;

    float getBaseElevation();

    CardView getCardViewAt(int position);

    int getCount();
}
