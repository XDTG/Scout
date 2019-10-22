package com.example.tg.scout;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CellPhoneInfo {

    //获取本设备mac地址
    public String getStringMac(){
        String mac = "00-00-00";
        try {
            mac = ConvertMacAddressBytesToString(getMac());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
            Log.d("mac","fail");
        }
        return mac;
    }

    private byte[] getMac() throws SocketException {
        NetworkInterface networkInterface;
        networkInterface = NetworkInterface.getByName("wlan0");
        return networkInterface.getHardwareAddress();
    }
    private String ConvertMacAddressBytesToString(byte[] mac) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < mac.length; i++){
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
        }
        return sb.toString();
    }

    //获取时间戳
    public String getTimeStamp(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd   HH:mm:ss");
        Date curData = new Date(System.currentTimeMillis());
        String timestamp = formatter.format(curData);
        return timestamp;
    }
}
