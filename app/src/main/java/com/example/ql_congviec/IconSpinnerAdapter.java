package com.example.ql_congviec;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class IconSpinnerAdapter extends BaseAdapter {
    private Context context;
    private int[] iconResIds;

    public IconSpinnerAdapter(Context context, int[] iconResIds) {
        this.context = context;
        this.iconResIds = iconResIds;
    }

    @Override
    public int getCount() {
        return iconResIds.length;
    }

    @Override
    public Object getItem(int position) {
        return iconResIds[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // View hiện tại (khi spinner chưa bung dropdown)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(80, 80)); // kích thước icon hiển thị
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(iconResIds[position]);
        return imageView;
    }

    // View hiển thị trong dropdown list
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setPadding(16, 16, 16, 16);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    150)); // chiều cao dropdown item
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(iconResIds[position]);
        return imageView;
    }
}


