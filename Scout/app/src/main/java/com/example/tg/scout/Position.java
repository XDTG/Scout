package com.example.tg.scout;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

public class Position extends Activity {

    public double latitude,longitude;
    public int gpsStatus, locationSource;
    public float accuracy;
    public String address;

    public Context context = null;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if(aMapLocation.getErrorCode() == 0){
                latitude = aMapLocation.getLatitude();
                longitude = aMapLocation.getLongitude();
                gpsStatus = aMapLocation.getGpsAccuracyStatus();
                accuracy = aMapLocation.getAccuracy();
                locationSource = aMapLocation.getLocationType();
                address = aMapLocation.getAddress();

            }else{
                Log.e("AmapError","location Error. ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    };
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    public Position(Context context){
        this.context = context;
        //初始化定位
        mLocationClient = new AMapLocationClient(context);

        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setInterval(3000);
        mLocationOption.setLocationCacheEnable(false);


        mLocationClient.setLocationOption(mLocationOption);

        //mLocationClient.startLocation();

    }



}
