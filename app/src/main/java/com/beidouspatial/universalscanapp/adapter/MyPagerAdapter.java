package com.beidouspatial.universalscanapp.adapter;

import android.content.Context;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beidouspatial.universalscanapp.R;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class MyPagerAdapter extends PagerAdapter {

    private Context context;
    private String folderName;
    private List<String> pic_list;

    public MyPagerAdapter(Context context, String folderName, List<String> pic_list) {
        this.folderName = folderName;
        this.pic_list = pic_list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return pic_list.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_viewpager, null);
        ImageView iv = view.findViewById(R.id.iv);
        TextView tv_pic_name = view.findViewById(R.id.tv_pic_name);
        tv_pic_name.setText(pic_list.get(position));
        Glide.with(context).load(new File(Environment.getExternalStorageDirectory() + "/1北斗资料归档/" + folderName, tv_pic_name.getText().toString() + ".jpg")).into(iv);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
