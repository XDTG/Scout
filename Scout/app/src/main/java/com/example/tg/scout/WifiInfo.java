package com.example.tg.scout;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class WifiInfo extends Activity{

    public static final double A = 28.0, N = 3.25;

    private List<ScanResult> scanResults;
    private StringBuilder scanBuilder = new StringBuilder();
    private ArrayList<HashMap<String, String>> infoList = new ArrayList<>();
    private ArrayList<String> macList = new ArrayList<>();

    public JSONArray getWifiInfo(Context context){
        String ssid, bssid, capability, timestamp;
        int level, channel; //should be channel, consider of background server, edit later
        double distance;
        JSONObject temWifiJson = null;
        macList.clear();//清空macList
        JSONArray wifiJson = new JSONArray();

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        //检测 Wi-Fi 开关
        if (!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }

        wifiManager.startScan();
        scanResults = wifiManager.getScanResults();
        ResultSort(scanResults);
        scanBuilder.delete(0, scanBuilder.length());
        infoList.clear();

        for (ScanResult scanResult : scanResults) {
            Date currentDate = new Date(System.currentTimeMillis());

            ssid = scanResult.SSID;
            bssid = scanResult.BSSID;
            level = scanResult.level;
            capability = scanResult.capabilities;
            channel = Frequency_To_Channel(scanResult.frequency);
            distance = Level_To_Distance(scanResult.level);
            scanBuilder.append(
                    "\nSSID:"+ssid
                    +"\nlevel:"+level
                    +"\nbssid:"+bssid
                    +"\n加密方案:"+capability
                    +"\ndistance:"+distance//+" "+distance
                    +"\nchannel:"+channel
                    +"\n"
            );
            //Log.d("wifiInfo",scanBuilder.toString());

            temWifiJson = new JSONObject();
            try {
                temWifiJson.put("ssid", ssid);
                temWifiJson.put("channel", channel);
                temWifiJson.put("bssid", bssid);
                temWifiJson.put("capability", capability);
                temWifiJson.put("level", level);
                temWifiJson.put("distance", distance);

                wifiJson.put(temWifiJson);
            }catch (Exception e) {
                e.printStackTrace();
            }

            macList.add(bssid);
        }
        return wifiJson;
    }

    private void ResultSort(List<ScanResult> scanResults) {
        Collections.sort(scanResults, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult r1, ScanResult r2) {
                if (r1.level < r2.level) return 1;
                else if (r1.level > r2.level) return -1;
                else return 0;
            }
        });
    }

    private int Frequency_To_Channel(int frequency){
        int channel;
        if (frequency <= 2472 && frequency >= 2412) {
            channel = (frequency - 2407) / 5;
        }else if (frequency == 2484){
            channel = 14;
        }else if(frequency >= 5180 && frequency <= 5825) {
            channel = 36 + (frequency - 5180) / 5;
        }else {
            channel = 0;
            Log.d("channel",String.valueOf(frequency));
        }
        return channel;
    }

    private double Level_To_Distance(int level) {
        double distance;
        distance = Math.pow(10.0, (Math.abs(level) - A) / (10.0 * N));
        return distance;
    }

    /*
    private void querySharedWifi(String lat, String lng){
        String data = null;
        String result = null;
        try {
            data = "lat="+ URLEncoder.encode(lat, "utf-8")+"&lng="+URLEncoder.encode(lng, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.d("error","encode error");
        }
        InterUtils.Post postInter = new InterUtils.Post("http://47.100.124.2/sharedWifi.php", data);
        result = postInter.text;
        Log.d("sharedResult",result);

    }
    */

    public ArrayList<String> getMacList() {
        return macList;
    }
}
