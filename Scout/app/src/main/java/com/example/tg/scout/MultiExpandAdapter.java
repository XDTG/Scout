package com.example.tg.scout;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiExpandAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String,String>> list;
    private boolean[] showControl;

    public MultiExpandAdapter(Context context,ArrayList<HashMap<String,String>> list){
        super();
        this.context = context;
        this.list = list;
        showControl = new boolean[list.size()];

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        SpannableString spannableString = null;
        String color = null;
        Double rssi ;


        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item2,parent,false);
            holder = new ViewHolder();

            holder.showArea = (LinearLayout) convertView.findViewById(R.id.layout_showArea);
            holder.bssid = (TextView) convertView.findViewById(R.id.bssid);
            holder.essid = (TextView) convertView.findViewById(R.id.essid);
            holder.channel = (TextView) convertView.findViewById(R.id.channel);
            holder.cap = (TextView) convertView.findViewById(R.id.cap);
            holder.level = (TextView) convertView.findViewById(R.id.level);
            holder.hideArea = (RelativeLayout) convertView.findViewById(R.id.layout_hideArea);
            holder.distance = (TextView)convertView.findViewById(R.id.distance);
            holder.isShared = (TextView)convertView.findViewById(R.id.isShared);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }
        final HashMap<String,String> item = list.get(position);
        // 注意：我们在此给响应点击事件的区域（我的例子里是 showArea 的线性布局）添加Tag，
        // 为了记录点击的 position，我们正好用position 设置 Tag
        holder.showArea.setTag(position);
        if (item.containsKey("isShared"))
        if(item.get("isShared").equals("Y")) {
            holder.showArea.setBackgroundColor(Color.parseColor("#2894FF"));
        } else {
            holder.showArea.setBackgroundColor(Color.parseColor("#999999"));
        }

        if(!item.get("ssid").equals("")){
            holder.essid.setText(item.get("ssid"));

            rssi = Double.valueOf(item.get("level"));
            if(rssi > -50)
                color = "#A52A2A";
            else if(rssi > -80)
                 color = "#F0E68C";
            else
                color = "#696969";
            spannableString = new SpannableString(item.get("ssid"));
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(color)),
                    0,spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            spannableString = new SpannableString("[ 隐藏SSID ]");
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#AFEEEE")),
                    0,spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        holder.essid.setText(spannableString);
        holder.bssid.setText(item.get("bssid"));
        holder.channel.setText(item.get("channel"));
        holder.level.setText(item.get("level"));
        holder.cap.setText(item.get("capability"));
        holder.distance.setText(item.get("distance"));
        holder.isShared.setText(item.get("isShared"));
        // list依次加载每个item，加载的同时查看showControl控制数组中对应位置的true/false
        // true显示隐藏部分
        // false不显示
        if(showControl[position]){
            holder.hideArea.setVisibility(View.VISIBLE);
        }
        else{
            holder.hideArea.setVisibility(View.GONE);
        }

        holder.showArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tag = (Integer) view.getTag();
                if (showControl[tag]){
                    showControl[tag] = false;
                }
                else{
                    showControl[tag] = true;
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    private static class ViewHolder{
        private LinearLayout showArea;
        private TextView bssid;
        private TextView essid;
        private TextView channel;
        private TextView level;
        private TextView distance;
        private TextView cap;
        private RelativeLayout hideArea;
        private TextView isShared;

    }
}
