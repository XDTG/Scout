package com.example.tg.scout;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class InterUtils {

    public static class Post {
        public int status = 0;
        public String text = "";

        public Post(String surl, String data) {

            try {
                URL url = new URL(surl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", data.length()+"");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(data.getBytes());
                status = connection.getResponseCode();

                //get results here
                if (status == HttpURLConnection.HTTP_OK) {
                    InputStream in = connection.getInputStream();
                    InputStreamReader inr = new InputStreamReader(in);
                    BufferedReader bufferedReader = new BufferedReader(inr);
                    String inputline = "";
                    while ((inputline = bufferedReader.readLine()) != null) {
                        text += inputline;
                    }
                }


                connection.disconnect();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
