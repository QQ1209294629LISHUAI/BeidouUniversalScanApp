package com.beidouspatial.universalscanapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.beidouspatial.universalscanapp.R;
import com.beidouspatial.universalscanapp.activity.BigPicActivity;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class PicDetailAdapter extends RecyclerView.Adapter<PicDetailAdapter.MyHolder> {

    private Context context;
    private List<String> data;
    private String folderName;
    private boolean ifDelete;

    public PicDetailAdapter(Context context, List<String> data, String folderName) {
        this.context = context;
        this.data = data;
        this.folderName = folderName;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pic_detail_item, viewGroup, false);
        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        myHolder.tv_pic_name.setText(data.get(i));
        Glide.with(context).load(new File(Environment.getExternalStorageDirectory() + "/1北斗资料归档/" + folderName, data.get(i) + ".jpg")).into(myHolder.iv);
        if (ifDelete) {
            myHolder.cb.setVisibility(View.VISIBLE);
            myHolder.cb.setChecked(false);
        } else {
            myHolder.cb.setVisibility(View.GONE);
            myHolder.cb.setChecked(false);
        }
        // 条目点击事件
        if (mItemClickListener != null) {
            myHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(i);
                    if (ifDelete) {
                        myHolder.cb.setChecked(!myHolder.cb.isChecked());
                    } else {
                        //照片大图展示
                        Intent intent = new Intent(context, BigPicActivity.class);
                        intent.putExtra("pic_list", (Serializable) data);
                        intent.putExtra("folderName",folderName);
                        intent.putExtra("click_position",i);
                        context.startActivity(intent);
                    }
                }
            });
            myHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mItemClickListener.onItemLongClick(i);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public void setIfDelete(boolean ifDelete) {
        this.ifDelete = ifDelete;
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tv_pic_name;
        CheckBox cb;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv);
            tv_pic_name = itemView.findViewById(R.id.tv_pic_name);
            cb = itemView.findViewById(R.id.cb);
        }
    }

    /**
     * 条目点击事件
     */
    private ItemClickListener mItemClickListener;

    public interface ItemClickListener {
        void onItemClick(int position);

        void onItemLongClick(int position);
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

}
