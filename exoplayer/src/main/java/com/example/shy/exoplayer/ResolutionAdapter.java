package com.example.shy.exoplayer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * 屏幕分辨率的adapter
 */
public class ResolutionAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> keys=new ArrayList<>();
    int select_key=0;
    public ResolutionAdapter(Context context, ArrayList<String> keys, String key) {
        this.context = context;
        this.keys.clear();
        this.keys.addAll(keys);
        for (int i=0;i<keys.size();i++){
            if (key.equals(keys.get(i))){
                select_key=i;
                return;
            }
        }
    }

    public void setSelect_key(int select_key) {
        this.select_key = select_key;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return keys.size();
    }

    @Override
    public Object getItem(int position) {
        return keys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_key, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.text.setText(keys.get(position));
        if (select_key==position){
            holder.text.setTextColor(Color.parseColor("#ea8b3c"));
        }else {
            holder.text.setTextColor(Color.parseColor("#ffffff"));
        }
        return convertView;

    }

    class ViewHolder {
        TextView text;
    }
}
