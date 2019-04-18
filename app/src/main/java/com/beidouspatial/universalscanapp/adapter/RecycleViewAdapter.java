package com.beidouspatial.universalscanapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beidouspatial.universalscanapp.R;
import com.beidouspatial.universalscanapp.db.entity.Pic;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MyHolder> {

    private Context context;
    private List<Pic> data;

    public RecycleViewAdapter(Context context, List<Pic> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycle_view_item, viewGroup, false);
        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        Pic pic = data.get(i);
        myHolder.tv_folder_name.setText(pic.getFolderName());
        myHolder.tv_pic_num.setText(String.valueOf(pic.getPicPath().split(",").length));
        myHolder.tv_create_time.setText(pic.getCreateTime());
        // TODO: 2019/4/11 解决冲突问题(不同库的jnilib文件夹必须一致，一个库不能乱加jnilib文件夹及复制so库)，并解决创建File对象太多问题
        Glide.with(context).load(new File(Environment.getExternalStorageDirectory() + "/1北斗资料归档/" + pic.getFolderName(), pic.getPicPath().split(",")[0] + ".jpg")).into(myHolder.iv);
//        Bitmap bitmap = decodeBitmap(Environment.getExternalStorageDirectory() + "/1北斗资料归档/" + pic.getFolderName() + "/" + pic.getPicPath().split(",")[0] + ".jpg", context);
//        myHolder.iv.setImageBitmap(bitmap);

        // 条目点击事件
        if (mItemClickListener != null) {
            myHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(i);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Pic> data) {
        this.data = data;
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tv_folder_name;
        TextView tv_create_time;
        TextView tv_pic_num;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv);
            tv_folder_name = itemView.findViewById(R.id.tv_folder_name);
            tv_create_time = itemView.findViewById(R.id.tv_create_time);
            tv_pic_num = itemView.findViewById(R.id.tv_pic_num);
        }
    }

    /**
     * 条目点击事件
     */
    private ItemClickListener mItemClickListener;

    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    /**
     * 加载本地大图片
     *
     * @param localPath
     * @param context
     * @return
     */
    public Bitmap decodeBitmap(String localPath, Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // 置为true,仅仅返回图片的分辨率
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(localPath, opts);
        // 得到原图的分辨率;
        int srcHeight = opts.outHeight;
        int srcWidth = opts.outWidth;
        // 得到设备的分辨率
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        // 通过比较得到合适的比例值;
        // 屏幕的 宽320 高 480 ,图片的宽3000 ,高是2262  3000/320=9  2262/480=5,,使用大的比例值
        int scale = 1;
        int sx = srcWidth / screenWidth;
        int sy = srcHeight / screenHeight;
        if (sx >= sy && sx > 1) {
            scale = sx;
        }
        if (sy >= sx && sy > 1) {
            scale = sy;
        }
        // 根据比例值,缩放图片,并加载到内存中;
        // 置为false,让BitmapFactory.decodeFile()返回一个图片对象
        opts.inJustDecodeBounds = false;
        // 可以把图片缩放为原图的1/scale * 1/scale
        opts.inSampleSize = scale;
        // 得到缩放后的bitmap
//        Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/lp.jpg", opts);
        Bitmap bm = BitmapFactory.decodeFile(localPath, opts);
        return bm;
    }

}
