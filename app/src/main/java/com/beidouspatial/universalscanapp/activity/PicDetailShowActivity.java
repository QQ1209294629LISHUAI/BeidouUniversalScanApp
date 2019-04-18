package com.beidouspatial.universalscanapp.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beidouspatial.universalscanapp.CameraActivity;
import com.beidouspatial.universalscanapp.MyApplication;
import com.beidouspatial.universalscanapp.R;
import com.beidouspatial.universalscanapp.adapter.PicDetailAdapter;
import com.beidouspatial.universalscanapp.db.entity.Pic;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class PicDetailShowActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_back;
    private TextView tv_title;
    private Pic pic;
    private RecyclerView rv;
    private PicDetailAdapter adapter;
    private TextView tv_select;
    private LinearLayout ll_delete;
    private boolean ifDelete;
    private ImageView iv_capture;
    private Toolbar toolbar;
    private ImageView iv_delete;
    private static final int RC_CAMERA = 222;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_detail_show);

        ifDelete = false;

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        pic = (Pic) bundle.get("pic");

        iv_back = findViewById(R.id.iv_back);
        tv_title = findViewById(R.id.tv_title);
        rv = findViewById(R.id.rv);
        tv_select = findViewById(R.id.tv_select);
        ll_delete = findViewById(R.id.ll_delete);
        iv_capture = findViewById(R.id.iv_capture);
        toolbar = findViewById(R.id.toolbar);
        iv_delete = findViewById(R.id.iv_delete);

        iv_delete.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        iv_capture.setOnClickListener(this);
        tv_title.setText(pic.getFolderName());

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        rv.setLayoutManager(layoutManager);
        List<String> picList = new ArrayList<>();
        String[] pics = pic.getPicPath().split(",");
        for (int i = 0; i < pics.length; i++) {
            picList.add(pics[i]);
        }
        adapter = new PicDetailAdapter(this, picList, pic.getFolderName());
        adapter.setOnItemClickListener(new PicDetailAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public void onItemLongClick(int position) {
                ifDelete = true;
                changeState();
            }
        });
        rv.setAdapter(adapter);

        changeState();
    }

    @Override
    public void onBackPressed() {
        if (ifDelete) {
            ifDelete = false;
            changeState();
        } else {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(-1, R.anim.zoom_out);
    }

    private void changeState() {
        if (ifDelete) {
//            tv_select.setVisibility(View.VISIBLE);
            ll_delete.setVisibility(View.VISIBLE);
            iv_capture.setVisibility(View.GONE);
            toolbar.setBackgroundColor(getResources().getColor(R.color.red_light));
            tv_title.setText("选择要删除的照片");
            adapter.setIfDelete(ifDelete);
            adapter.notifyDataSetChanged();
        } else {
//            tv_select.setVisibility(View.GONE);
            ll_delete.setVisibility(View.GONE);
            iv_capture.setVisibility(View.VISIBLE);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            tv_title.setText(pic.getFolderName());
            adapter.setIfDelete(ifDelete);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                if (ifDelete) {
                    ifDelete = false;
                    changeState();
                } else {
                    finish();
                }
                break;
            case R.id.iv_delete:
                List<String> picList = new ArrayList<>();
                String[] pics = pic.getPicPath().split(",");
                for (int i = 0; i < pics.length; i++) {
                    picList.add(pics[i]);
                }

                ArrayList<String> newPicList = new ArrayList<>();
                ArrayList<String> deletePicList = new ArrayList<>();
                for (int position = 0; position < adapter.getItemCount(); position++) {
                    View child = rv.getChildAt(position);
                    CheckBox cb = child.findViewById(R.id.cb);
                    if (cb.isChecked()) {
                        //需要删除
                        deletePicList.add(picList.get(position));
                    } else {
                        //无需删除
                        newPicList.add(picList.get(position));
                    }
                }

                String picPath = "";
                for (int i = 0; i < newPicList.size(); i++) {
                    picPath += newPicList.get(i) + ",";
                }
                pic.setPicPath(picPath);
                Observable
                        .create(emitter -> {
                            MyApplication.db.picDao().insertAll(pic);
                            emitter.onNext(1);
                            emitter.onComplete();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> {
                            adapter.setData(newPicList);
                            adapter.notifyDataSetChanged();
                        });

                Toast.makeText(this, "照片删除成功！", Toast.LENGTH_LONG).show();
                break;
            case R.id.iv_capture:
                startCamera();
                break;
            default:
                break;
        }
    }

    @AfterPermissionGranted(RC_CAMERA)
    public void startCamera() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            Intent intent = new Intent();
            intent.setClass(this, CameraActivity.class);
            intent.putExtra("pic_folder_path", Environment.getExternalStorageDirectory().getAbsolutePath() + "/1北斗资料归档/" + pic.getFolderName());
            intent.putExtra("create_time", pic.getCreateTime());
            intent.putExtra("continue_capture", 1);
            Bundle bundle = new Bundle();
            bundle.putSerializable("pic", pic);
            intent.putExtras(bundle);
            startActivityForResult(intent, 0);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "需要调用相机及读取存储卡的权限！", RC_CAMERA, perms);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int uid = data.getIntExtra("uid", -1);
        refreshRecycleView(uid);
    }

    private void refreshRecycleView(int uid) {
        Observable
                .create(emitter -> {
                    List<Pic> picList = MyApplication.db.picDao().loadAllByIds(new int[]{uid});
                    emitter.onNext(picList);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    List<Pic> picList = (List<Pic>) o;
                    if (picList.size() > 0) {
                        pic = picList.get(0);
                        List<String> list = new ArrayList<>();
                        String[] pics = pic.getPicPath().split(",");
                        for (int i = 0; i < pics.length; i++) {
                            list.add(pics[i]);
                        }
                        adapter.setData(list);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

}
