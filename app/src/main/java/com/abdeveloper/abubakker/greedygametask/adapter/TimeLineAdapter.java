package com.abdeveloper.abubakker.greedygametask.adapter;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abdeveloper.abubakker.greedygametask.MainActivity;
import com.abdeveloper.abubakker.greedygametask.R;
import com.abdeveloper.abubakker.greedygametask.holder.TimeLineViewHolder;
import com.abdeveloper.abubakker.greedygametask.model.ResponseMinute;
import com.abdeveloper.abubakker.greedygametask.model.ResponseSecond;
import com.appyvet.rangebar.RangeBar;
import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder> {

    private List<ResponseMinute> mFeedList;
    private Context mContext;
    private GoogleMap googleMap;
    private DatabaseReader reader;
    private MyCountDownTimer countDownTimer;
    private Boolean isRunning = false;

    public TimeLineAdapter(List<ResponseMinute> feedList, GoogleMap googleMap, MainActivity mainActivity, DatabaseReader reader) {
        mFeedList = feedList;
        this.googleMap = googleMap;
        this.reader = reader;
        isRunning = false;
    }

    @Override
    public TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
        View view;
        view = mLayoutInflater.inflate(R.layout.timeline_item_layout, parent, false);
        return new TimeLineViewHolder(view, viewType);

    }

    @Override
    public void onBindViewHolder(final TimeLineViewHolder holder, int position) {

        //Fetching Minute
        ResponseMinute minute = mFeedList.get(position);
        //Fetching Seconds
        final List<ResponseSecond> seconds = minute.getSeconds();


        final Boolean clickable = mFeedList.get(position).getClickable();

        //Intializing value of RangeBar
        holder.rangeSeekbar.setSeekPinByValue(0);
        holder.rangeSeekbar.setSeekPinByIndex(0);

        //Setting Time Duration
        holder.mDate.setText(getDate(1, position, 0) + " - " + getDate(1, position, 59));


        //Making single item enable at a time
        if (clickable) {
            holder.mDate.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            holder.rangeSeekbar.setVisibility(View.VISIBLE);
            holder.play_button.setVisibility(View.VISIBLE);
            holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_timeline_marker_active));

        } else {
            holder.mDate.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
            holder.rangeSeekbar.setVisibility(View.GONE);
            holder.play_button.setVisibility(View.GONE);
            holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_timeline_marker));

        }

        //AutoPlay Button
        holder.play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Checking if autoPlay is not already Running and is Item Clickable
                if (clickable && !isRunning) {

                    holder.play_button.playAnimation();
                    googleMap.clear();

                    //adding delay to cover all latlong so considering 70 secs instead of 60
                    startTimer(reader, holder, 70000, 1000, seconds);

                } else {

                    isRunning = false;
                    googleMap.clear();
                    countDownTimer.cancel();
                    holder.play_button.reverseAnimation();
                }
            }
        });


        //Enabling single item with Click
        holder.main_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRunning) {
                    countDownTimer.cancel();
                }
                googleMap.clear();

                //Removing Previously Selected Item
                removeAllClicks();
                mFeedList.get(holder.getAdapterPosition()).setClickable(true);
                notifyItemChanged(holder.getAdapterPosition());
                notifyItemRangeChanged(0, getItemCount());

            }
        });

        holder.rangeSeekbar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                if (clickable) {

                    //Clearing google map show location on map for each sec , we can remove this to show location for whole minute

                    googleMap.clear();
                    if (seconds.get(rightPinIndex).getLatLongs().size() > 0) {
                        initMapSettings(getLatLongs(reader, seconds.get(rightPinIndex).getLatLongs()), googleMap);
                    }
                }
            }
        });


    }

    private void removeAllClicks() {
        for (int i = 0; i < 60; i++) {
            mFeedList.get(i).setClickable(false);
        }
    }

    private void initMapSettings(List<LatLng> locations, GoogleMap googleMap) {

        //Initializing HeatMap
        HeatmapTileProvider mProvider;

        //Adding Locations
        mProvider = new HeatmapTileProvider.Builder().data(locations).build();
        mProvider.setRadius(HeatmapTileProvider.DEFAULT_RADIUS);

        //Adding to Map
        googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

        //As their are number of locations in list , the animation of map should cover all LatLongs
        //Using LatLngBound for this

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng marker : locations) {
            builder.include(marker);
        }
        LatLngBounds bounds = builder.build();
        int padding = 50; // offset from edges of the map in pixels
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, width, padding);
        googleMap.moveCamera(cu);
    }

    @Override
    public int getItemCount() {
        return (mFeedList != null ? mFeedList.size() : 0);
    }


    private void startTimer(DatabaseReader reader, TimeLineViewHolder holder, long time, long interval, List<ResponseSecond> seconds) {

        //CountDown Timer for 60 sec
        countDownTimer = new MyCountDownTimer(reader, holder, time, interval, seconds);
        countDownTimer.start();
    }

    private List<LatLng> getLatLongs(DatabaseReader reader, List<String> ips) {

        //Reading LatLongs From Ip Address Using GeoLite2 Database
        //Database has been stored in asset

        List<LatLng> latLngs = new ArrayList<>();
        for (int i = 0; i < ips.size(); i++) {
            InetAddress ipAddress = null;
            try {
                ipAddress = InetAddress.getByName(ips.get(i));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            try {
                assert ipAddress != null;
                CityResponse response = reader.city(ipAddress);
                latLngs.add(new LatLng(response.getLocation().getLatitude(), response.getLocation().getLongitude()));
            } catch (IOException | GeoIp2Exception e) {
                e.printStackTrace();
            }
        }
        return latLngs;
    }

    private class MyCountDownTimer extends CountDownTimer {

        TimeLineViewHolder holder;
        DatabaseReader reader;
        List<ResponseSecond> seconds = new ArrayList<>();
        int count = 0;

        private MyCountDownTimer(DatabaseReader reader, TimeLineViewHolder holder, long startTime, long interval, List<ResponseSecond> seconds) {
            super(startTime, interval);
            this.holder = holder;
            this.seconds = seconds;
            this.reader = reader;
        }

        @Override
        public void onFinish() {
            isRunning = false;
            holder.play_button.reverseAnimation();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            isRunning = true;
            if (count < 60) {
                holder.rangeSeekbar.setSeekPinByValue(count);
            }
            count++;
        }
    }


    private static String getDate(int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return simpleDateFormat.format(cal.getTime());
    }

}
