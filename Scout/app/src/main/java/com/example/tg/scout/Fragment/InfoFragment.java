package com.example.tg.scout.Fragment;


import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tg.scout.CellPhoneInfo;
import com.example.tg.scout.InterUtils;
import com.example.tg.scout.MainActivity;
import com.example.tg.scout.MultiExpandAdapter;
import com.example.tg.scout.MyService;
import com.example.tg.scout.Position;
import com.example.tg.scout.R;
import com.example.tg.scout.WifiInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class InfoFragment extends Fragment {

    private TextView acinfoView, locationView, gpsInfoView, precisionView, sourceView;
    private Button refreshListButton;
    private Position position;
    private CellPhoneInfo cellPhoneInfo;
    private MainActivity activity;
    private Switch powerSwitch, serviceSwitch;
    private ListView apList;

    private Chronometer chronometer;

    private State state = new State();
    private int systemState, uploadState;

    private WifiInfo wifiInfo;
    private JSONArray wifiJsons;
    private List<String> sharedMac = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();

        //权限申请
        while (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }


        //location 对象
        position = new Position(activity);


        cellPhoneInfo = new CellPhoneInfo();

        wifiInfo = new WifiInfo();
        wifiJsons = wifiInfo.getWifiInfo(activity);


        //Intent startIntent = new Intent(activity, MyService.class);

        uploadThread();



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, null);

        locationView = view.findViewById(R.id.location);
        acinfoView = view.findViewById(R.id.acinfo);
        powerSwitch = view.findViewById(R.id.power);
        serviceSwitch = view.findViewById(R.id.service);
        chronometer = view.findViewById(R.id.chronometer);
        gpsInfoView = view.findViewById(R.id.gpsStatus);
        precisionView = view.findViewById(R.id.precision);
        sourceView = view.findViewById(R.id.source);
        refreshListButton = view.findViewById(R.id.refresh);
        apList = view.findViewById(R.id.apList);


        //采集开关
        powerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    //计时开始
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();

                    acinfoView.setText("Connecting");
                    locationView.setText("正在启动定位");

                    //gpsInfo.setText("GPS Status: ");
                    position.mLocationClient.startLocation();

                    systemState = state.OPENED;
                    refreshList();
                } else {
                    //计时停止
                    chronometer.stop();

                    acinfoView.setText("Connection Stopped");
                    locationView.setText("定位停止");
                    gpsInfoView.setText(" ");
                    precisionView.setText(" ");
                    sourceView.setText(" ");

                    position.mLocationClient.stopLocation();
                    position.latitude = 0.0;
                    position.longitude = 0.0;

                    systemState = state.CLOSED;
                    refreshList();

                }
            }
        });

        //后台服务开关
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    Intent startIntent = new Intent(activity, MyService.class);
                    activity.startService(startIntent);
                } else {
                    Intent stopIntent = new Intent(activity, MyService.class);
                    activity.stopService(stopIntent);
                }
            }
        });

        //刷新状态UI
        refreshUI();

        //刷新ap列表按钮
        refreshListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                refreshList();

                Log.d("post",String.valueOf(position.longitude));

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final int status = uploadInfo(wifiInfo.getWifiInfo(activity), position.latitude, position.longitude,
                                cellPhoneInfo.getTimeStamp(), cellPhoneInfo.getStringMac(),wifiInfo.getMacList());
                        if(status == 200){
                            Log.d("upload","列表数据上传成功");
                            //Toast.makeText(activity, "列表数据上传成功", Toast.LENGTH_SHORT).show();
                        } else {
                            //Toast.makeText(activity, "列表数据上传失败 Error "+status, Toast.LENGTH_SHORT).show();
                            Log.d("upload","列表数据上传失败 Error "+status);
                        }
                    }
                });
                t.start();

            }
        });

        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public void refreshUI(){
        Thread uiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pushUI();
                        }
                    });

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        uiThread.start();
    }

    private void pushUI(){
        if(position.latitude != 0.0 && systemState == state.OPENED){
            locationView.setText(String.valueOf(position.latitude)+"\n"+String.valueOf(position.longitude)+"\n"+position.address);
            gpsInfoView.setText("GPS Status: " + getGpsStatus(position.gpsStatus));
            precisionView.setText("定位精度: " + String.valueOf(position.accuracy));
            sourceView.setText("定位来源: " + getLocSrc(position.locationSource));
            acinfoView.setText("上传状态 " + String.valueOf(uploadState));
        }

    }


    public void refreshList(){
        TimeCount timeCount = new TimeCount(7*1000, 1000);
        Thread listThread = new Thread(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pushList();
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        listThread.start();
        if(systemState == state.OPENED)
            timeCount.start();


    }

    public void pushList() throws JSONException {
        JSONArray wifiJsons = wifiInfo.getWifiInfo(activity);
        Log.d("length",String.valueOf(wifiJsons.length()));
        ArrayList<HashMap<String,String>> data = new ArrayList<>();
        if(systemState == state.OPENED) {
            JSONObject jsonOb = new JSONObject();
            Log.d("length",String.valueOf(wifiJsons.length()));
            for (int j=0; j<wifiJsons.length(); j++){
                jsonOb = wifiJsons.getJSONObject(j);
                HashMap<String,String> item = new HashMap<>();
                String bssid = jsonOb.getString("bssid");

                item.put("ssid", jsonOb.getString("ssid"));
                item.put("channel", jsonOb.getString("channel"));
                item.put("bssid", bssid);
                item.put("capability", jsonOb.getString("capability"));
                item.put("level", jsonOb.getString("level"));
                item.put("distance", jsonOb.getString("distance"));
                item.put("isShared","N");
                if (sharedMac!=null)
                if (sharedMac.contains(bssid))
                    item.put("isShared","Y");
                data.add(item);
            }
        }
        MultiExpandAdapter adapter = new MultiExpandAdapter(activity, data);
        apList.setAdapter(adapter);

    }


    private class State{
        final static int OPENED = 1;
        final static int CLOSED = 0;
    }

    public String getGpsStatus(int gpsStatus) {
        if (Double.valueOf(gpsStatus)==1.0){
            return "信号强";
        }
        else if (Double.valueOf(gpsStatus)==0.0) {
            return "信号弱";
        }
        else if (Double.valueOf(gpsStatus)==-1.0){
            return "无信号";
        }
        return "未知";
    }

    public String getLocSrc(int locSrc){
        if (Double.valueOf(locSrc)==6.0){
            return "基站";
        }
        else if(Double.valueOf(locSrc)==4.0){
            return "上次相同环境缓存";
        }
        else if(Double.valueOf(locSrc)==1.0){
            return "GPS";
        }
        else if(Double.valueOf(locSrc)==9.0){
            return "最后位置缓存";
        }
        else if(Double.valueOf(locSrc)==2.0){
            return "离线";
        }
        else if(Double.valueOf(locSrc)==5.0){
            return "传感器";
        }
        else if(Double.valueOf(locSrc)==8.0){
            return "Wi-Fi";
        }
        else {
            return "";
        }
    }


    class TimeCount extends CountDownTimer {
        public TimeCount(long totalTime, long interval){
            super(totalTime, interval);

        }
        @Override
        public void onTick(long millisUntilFinished){
            refreshListButton.setEnabled(false);
            refreshListButton.setText(String.valueOf(millisUntilFinished/1000)+"S");
        }
        @Override
        public void onFinish(){
            refreshListButton.setEnabled(true);
            refreshListButton.setText("刷新");
        }
    }

    public void uploadThread() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String timeStamp, mac;
                double longitude, latitude;
                int respondCode;
                JSONArray wifiJsons = new JSONArray();
                ArrayList<String> macList = new ArrayList<>();


                mac = cellPhoneInfo.getStringMac();

                //首次定位等待
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                while(true) {
                    latitude = position.latitude;
                    longitude = position.longitude;
                    wifiJsons = wifiInfo.getWifiInfo(activity);
                    macList = wifiInfo.getMacList();
                    timeStamp = cellPhoneInfo.getTimeStamp();
                    respondCode = uploadInfo(wifiJsons, latitude, longitude, timeStamp, mac, macList);
                    Log.d("PostRespondCode", String.valueOf(respondCode));

                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        t.start();
    }

    private int uploadInfo(JSONArray wifiJsons, double latitude, double longitude, String timeStamp, String mac, ArrayList<String> macList){
        String data = null;
        String shareData = null;
        int sharedState;
        if (systemState == state.OPENED){

            if ((longitude == 0.0) || (latitude == 0.0))
                return 0;

            try {
                data = "json_data=" + URLEncoder.encode(wifiJsons.toString(), "utf-8") +
                        "&lantitude=" + URLEncoder.encode(String.valueOf(latitude), "utf-8") +
                        "&longitude=" + URLEncoder.encode(String.valueOf(longitude), "utf-8") +
                        "&sign=" + new String(Base64.encode(mac.getBytes(), Base64.DEFAULT)) +
                        "&timestamp=" + timeStamp;
                data = data.replace("\n", "");
                data = data.replace("\r", "");

                shareData = "around=" + URLEncoder.encode(macList.toString(), "utf-8") +
                            "&lat=" + URLEncoder.encode(String.valueOf(latitude), "utf-8") +
                            "&lng=" + URLEncoder.encode(String.valueOf(longitude), "utf-8");
                //Log.d("PostData", data);


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e("JsonData", "JsonData wrong");
                Log.e("ShareData","ShareData wrong");
            }
            InterUtils.Post postInter = new InterUtils.Post
                    ("http://119.23.8.24/wifi_yii/web/index.php?r=api/ap/ap", data);
            InterUtils.Post sharePostInter = new InterUtils.Post
                    ("http://47.100.124.2/sharedWifi/sharedWifi.php",shareData);

            uploadState = postInter.status;
            sharedState = sharePostInter.status;


            sharedMac = Arrays.asList(sharePostInter.text.trim().split("<br>"));
            //Log.d("sharedMacInfo",sharePostInter.text);

            Log.d("postrefresh",String.valueOf(uploadState));
            Log.d("SharedWifi_upload",String.valueOf(sharedState));
        }
        return uploadState;

    }

}

