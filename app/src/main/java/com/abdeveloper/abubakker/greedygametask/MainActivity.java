package com.abdeveloper.abubakker.greedygametask;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.abdeveloper.abubakker.greedygametask.adapter.TimeLineAdapter;
import com.abdeveloper.abubakker.greedygametask.model.ResponseMinute;
import com.abdeveloper.abubakker.greedygametask.model.ResponseRawData;
import com.abdeveloper.abubakker.greedygametask.model.ResponseSecond;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.maxmind.geoip2.DatabaseReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public GoogleMap googleMap;
    private TimeLineAdapter mTimeLineAdapter;
    private List<ResponseRawData> rawDataModelList = new ArrayList<>();
    private List<ResponseMinute> minuteArrayList = new ArrayList<>();


    @BindView(R.id.progress)
    ProgressBar progress;
  
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.splash_container)
    RelativeLayout splash_container;

    DatabaseReader reader;
 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        makingSomeMagic();

        //Initializing Map Fragment
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //making status bar translucent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        mRecyclerView.setLayoutManager(getLinearLayoutManager());

        //Set recyclerview to fix size for better performance if it is not going to change
        mRecyclerView.setHasFixedSize(true);



        //Getting GeoLite database and Ip address data from asset and running  in background
        GetFromAsset asset = new GetFromAsset(this, rawDataModelList);
        asset.execute();

    }

    private void makingSomeMagic() {

        //a small and temperory splash screen
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Do some stuff
                        splash_container.setVisibility(View.GONE);

                    }
                });
            }
        };
        thread.start();

    }

    private void initializingList() {

        for(int i=0;i<60;i++){
            //initilaizing 60 seconds
            ArrayList<ResponseSecond> secondModels = new ArrayList<>();
            for(int j=0;j<60;j++){
                //initilaizing 60 seconds with default lat longs
                List<String> latLngs = new ArrayList<>();
                secondModels.add(new ResponseSecond(latLngs));
            }
            if(i==0) {
                //setting first item in recyclerview selected and clickable
                minuteArrayList.add(new ResponseMinute(secondModels, true));
            }else{
                minuteArrayList.add(new ResponseMinute(secondModels, false));
            }
        }
    }
    
    private LinearLayoutManager getLinearLayoutManager() {
        return new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap=googleMap;
    }
    
    private void initView() {
        mTimeLineAdapter = new TimeLineAdapter(minuteArrayList,googleMap,MainActivity.this,reader);
        mRecyclerView.setAdapter(mTimeLineAdapter);
    }

    private class BackgroundDataFetch extends AsyncTask<Void,Integer,Void>
    {
        List<ResponseMinute> minuteArrayList;
        List<ResponseRawData> dataModels;
        TimeLineAdapter adapter;
        Context mContext;


        private BackgroundDataFetch(Context mainActivity, List<ResponseRawData> dataModels, TimeLineAdapter mTimeLineAdapter, List<ResponseMinute> minuteArrayList) {
            this.dataModels=dataModels;
            this.adapter=mTimeLineAdapter;
            this.minuteArrayList=minuteArrayList;
            this.mContext=mainActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                int localMinute=0;
                for(int i=0;i<dataModels.size();i++){
                    String Time =dataModels.get(i).getTime();

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Calendar calendar = Calendar.getInstance();

                    calendar.setTime(simpleDateFormat.parse(Time));
                    int minute = calendar.get(Calendar.MINUTE);
                    int second = calendar.get(Calendar.SECOND);


                    //Code for notifiying recyclerview about data has changed
                    if((localMinute+1)==minute){
                        publishProgress(localMinute);
                        localMinute=minute;
                        Log.d("Update List ","Called");
                    }


                    //adding Ip addresses for particular minute and particular second
                    minuteArrayList.get(minute).getSeconds().get(second).getLatLongs().add(dataModels.get(i).getIpaddress());


                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //notifying data has changed for Particular item
            adapter.notifyItemChanged(values[0]);

        }
    }

    private class GetFromAsset extends AsyncTask<Void,Void,Void>
    {
        List<ResponseRawData> dataModels;
        Context mContext;

        private GetFromAsset(Context mainActivity, List<ResponseRawData> dataModels) {
            this.dataModels=dataModels;
            this.mContext=mainActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            //Getting Database from Asset
            AssetManager assetManagerDatabase = getAssets();
            InputStream inputStreamDatabase = null;
            try {
                inputStreamDatabase = assetManagerDatabase.open("GeoLite2-City.mmdb");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                //Database Reader object to get LatLng from Ip Address
                reader = new DatabaseReader.Builder(inputStreamDatabase).build();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Getting text file data Available from asset
            AssetManager assetManagerData = mContext.getAssets();
            InputStream inputStreamData = null;
            try {
                inputStreamData = assetManagerData.open("test_ip_ts.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert inputStreamData != null;
            BufferedReader readingLineData = new BufferedReader(new InputStreamReader(inputStreamData));
            String str;
            try {
                while ((str = readingLineData.readLine()) != null) {
                    String[] strings = str.split(",");
                    String time = strings[1].substring(0, 19);

                    //Adding each line seperating by coma
                    dataModels.add(new ResponseRawData(strings[0],time));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //Initializing Recyclerview
            initView();

            //Initializing Minute and Hour data to zero
            initializingList();

            progress.setVisibility(View.GONE);

            //Getting data using background thread
            BackgroundDataFetch thread = new BackgroundDataFetch(MainActivity.this,dataModels,mTimeLineAdapter,minuteArrayList);
            thread.execute();
        }

    }

}

