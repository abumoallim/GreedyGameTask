package com.abdeveloper.abubakker.greedygametask.holder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abdeveloper.abubakker.greedygametask.R;
import com.airbnb.lottie.LottieAnimationView;
import com.appyvet.rangebar.RangeBar;
import com.github.vipulasri.timelineview.TimelineView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by abubakker on 18/5/17.
 */

public class TimeLineViewHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.text_timeline_date)
    public TextView mDate;

    @BindView(R.id.rangeSeekbar1)
    public RangeBar rangeSeekbar;

    @BindView(R.id.time_marker)
    public TimelineView mTimelineView;

    @BindView(R.id.main_container)
    public LinearLayout main_container;

    @BindView(R.id.card_container)
    public CardView card_container;

    @BindView(R.id.play_button)
    public LottieAnimationView play_button;

    public TimeLineViewHolder(View itemView, int viewType) {
        super(itemView);

        ButterKnife.bind(this, itemView);
        mTimelineView.initLine(viewType);

    }

}