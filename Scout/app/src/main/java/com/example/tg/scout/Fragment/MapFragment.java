package com.example.tg.scout.Fragment;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.tg.scout.InterUtils;
import com.example.tg.scout.MainActivity;
import com.example.tg.scout.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MapFragment extends Fragment implements AMap.OnMapLoadedListener, View.OnClickListener{
    static final String POINT_URL = "http://119.23.8.24/wifi_yii/web/index.php?r=api/data/search";
    private MapView mapView;
    private AMap aMap;
    private Bundle tmpSavedInstanceState;
    private WindowManager windowManager;
    private MainActivity activity;
    private int[] windowSize;
    private Button markButton;
    private List<String> markedPoints = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tmpSavedInstanceState = savedInstanceState;
        activity = (MainActivity)getActivity();
        windowManager = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
        windowSize = getWindowsSize();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, null);
        mapView = view.findViewById(R.id.map);
        markButton = view.findViewById(R.id.search);

        initMapView();

        markButton.setOnClickListener(this);



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


    @Override
    public void onMapLoaded() {

    }

    private void initMapView(){
        mapView.onCreate(tmpSavedInstanceState);
        aMap = mapView.getMap();

        //定位小蓝点
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);

        aMap.setMyLocationEnabled(true);

        aMap.setOnMapLoadedListener(this);


    }

    private int[] getWindowsSize(){
        int[] size = new int[2];
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        size[0] = displayMetrics.widthPixels; //window width
        size[1] = displayMetrics.heightPixels; //height
        return size;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search:
                markPoints(windowSize);
                break;
        }
    }

    private void markPoints(int[] size){
        Marker leftMarker, rightMarker;
        LatLng leftP, rightP;
        final double leftLat, leftLng, rightLat, rightLng;



        leftP = adjustCameral(aMap);
        rightP = adjustCamerar(aMap, size[0], size[1]);
        leftLat = leftP.latitude; leftLng = leftP.longitude;
        rightLat = rightP.latitude; rightLng = rightP.longitude;

        Thread markThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Starting Mark Points
                JSONArray markJsons;
                String markBssid, markEssid, type;
                double markLat, markLng;
                int markChannel;

                markJsons = getMarkJsons(leftLat, leftLng, rightLat, rightLng);
                if(markJsons != null) {
                    try {
                        for (int n=0; n < markJsons.length(); n++){
                            JSONObject jsonObject = markJsons.getJSONObject(n);
                            markBssid = jsonObject.getString("bssid");

                            if (markedPoints.contains(markBssid))
                                continue;
                            markEssid = jsonObject.getString("ssid");
                            markLat = Double.valueOf(jsonObject.getString("Lantitude"));
                            markLng = Double.valueOf(jsonObject.getString("Longitude"));
                            markChannel = Integer.valueOf(jsonObject.getString("Channel"));
                            type = jsonObject.getString("Type");

                            markedPoints.add(markBssid);
                            LatLng mark = new LatLng(markLat, markLng);
                            Marker marker = aMap.addMarker(new MarkerOptions().position(mark)
                                    .title("Essid:"+markEssid).snippet("Bssid:"+markBssid+"\n"+"Channel:"+markChannel)
                                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), getPointColor(type)))));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        markThread.start();
    }

    protected LatLng adjustCamerar(AMap aMap,int width,int height){
        Projection projection = aMap.getProjection();
        Point right = new Point(width,height);
        LatLng rightlatlng = projection.fromScreenLocation(right);
        LatLngBounds bounds = LatLngBounds.builder().include(rightlatlng).build();
        aMap.getMapScreenMarkers();
        return rightlatlng;

    }

    private LatLng adjustCameral(AMap aMap){
        Projection projection = aMap.getProjection();
        Point left = new Point(0,0);
        LatLng leftlatlng = projection.fromScreenLocation(left);
        LatLngBounds bounds = LatLngBounds.builder().include(leftlatlng).build();
        aMap.getMapScreenMarkers();
        return leftlatlng;

    }

    private JSONArray getMarkJsons(double leftLat, double leftLong, double rightLat, double rightLong){

        JSONArray resultJsons = null;
        String resultData, postData;

        postData = "l1=" + String.valueOf(leftLat) + "&l2=" + String.valueOf(leftLong)  +
                "&r1=" + String.valueOf(rightLat) + "&r2=" + String.valueOf(rightLong);
        InterUtils.Post postInter = new InterUtils.Post(POINT_URL, postData);
        resultData = postInter.text;
        try {
            resultJsons = new JSONArray(resultData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultJsons;
    }

    private int getPointColor(String type) {
        if (type.equals("H"))
            return R.drawable.sred;
        else if (type.equals("M"))
            return R.drawable.sorange;
        else if (type.equals("L"))
            return R.drawable.sgray;
        return 0;

    }
}
