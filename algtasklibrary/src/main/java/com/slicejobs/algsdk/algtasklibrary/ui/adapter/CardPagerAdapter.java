package com.slicejobs.algsdk.algtasklibrary.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.search.route.TransitRouteLine;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.view.ICardView;

import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter implements ICardView {
    private List<CardView> mViews;
    private List<TransitRouteLine> mData;
    private float mBaseElevation;

    public CardPagerAdapter() {
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
    }

    public void addCardItem(TransitRouteLine item) {
        mViews.add(null);
        mData.add(item);
    }

    public void clearCardItem() {
        mViews.clear();
        mData.clear();
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_bus_route_plan, container, false);
        container.addView(view);
        bind(mData.get(position), view);
        CardView cardView = (CardView) view.findViewById(R.id.cardView);

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

    private void bind(TransitRouteLine item, View view) {
        TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        TextView contentTextView = (TextView) view.findViewById(R.id.contentTextView);
        TextView durationTextView = (TextView) view.findViewById(R.id.route_duration);
        TextView distanceTextView = (TextView) view.findViewById(R.id.route_distance);
        List<TransitRouteLine.TransitStep> allStep = item.getAllStep();
        int walkDistance = 0;
        StringBuilder transitStopSb = new StringBuilder();//站点信息
        StringBuilder transitLineSb = new StringBuilder();//路线信息
        for (int i = 0; i < allStep.size(); i++) {
            TransitRouteLine.TransitStep transitStep = allStep.get(i);
            if(transitStep.getStepType() == TransitRouteLine.TransitStep.TransitRouteStepType.WAKLING){
                walkDistance += transitStep.getDistance();
            }
            if(transitStep.getStepType() == TransitRouteLine.TransitStep.TransitRouteStepType.SUBWAY || transitStep.getStepType() == TransitRouteLine.TransitStep.TransitRouteStepType.BUSLINE){
                if(transitStep.getEntrance() != null && StringUtil.isNotBlank(transitStep.getEntrance().getTitle())){
                    if(!transitStopSb.toString().contains(transitStep.getEntrance().getTitle())){
                        transitStopSb.append(transitStep.getEntrance().getTitle() + "-");
                    }
                }
                if(transitStep.getExit() != null && StringUtil.isNotBlank(transitStep.getExit().getTitle())){
                    if(!transitStopSb.toString().contains(transitStep.getExit().getTitle())){
                        transitStopSb.append(transitStep.getExit().getTitle() + "-");
                    }
                }
                String instruction = transitStep.getInstructions();
                if(StringUtil.isNotBlank(instruction)){
                    String[] instructionArr = instruction.split(",");
                    if(instructionArr != null && instructionArr.length != 0){
                        if(instructionArr[0].contains("乘坐")){
                            transitLineSb.append(instructionArr[0].substring(2) + "-");
                        }
                    }
                }
            }
        }
        String transitStopStr = transitStopSb.toString().trim();
        if(transitStopStr.endsWith("-")){
            transitStopStr = transitStopStr.substring(0,transitStopStr.length() - 1);
        }
        String transitLineStr = transitLineSb.toString().trim();
        if(transitLineStr.endsWith("-")){
            transitLineStr = transitLineStr.substring(0,transitLineStr.length() - 1);
        }
        titleTextView.setText(transitLineStr);
        durationTextView.setText(item.getDuration() / 60 + "分钟");
        distanceTextView.setText("步行" + walkDistance + "米");
        contentTextView.setText(transitStopStr);
    }
}
