package com.beidouspatial.universalscanapp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.beidouspatial.universalscanapp.activity.PicDetailShowActivity;
import com.beidouspatial.universalscanapp.adapter.RecycleViewAdapter;
import com.beidouspatial.universalscanapp.db.entity.Pic;
import com.beidouspatial.universalscanapp.entity.OneParcelModel;
import com.beidouspatial.universalscanapp.util.ExcelUtil;
import com.beidouspatial.universalscanapp.util.TimeUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private FloatingActionButton fab_camera;
    private ImageView iv_capture;
    private RecyclerView recyclerView;
    private static final int RC_CAMERA = 222;
    private RecycleViewAdapter recycleViewAdapter;
    /**
     * 维护列表数据
     */
    private List<Pic> picList;
    private ImageView iv_excel;
    private TextView tv_beidou;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab_camera = findViewById(R.id.fab_camera);
        fab_camera.setOnClickListener(this);
        iv_capture = findViewById(R.id.iv_capture);
        iv_capture.setOnClickListener(this);
        recyclerView = findViewById(R.id.recyclerView);
        initRecyclerView();
        iv_excel = findViewById(R.id.iv_excel);
        iv_excel.setOnClickListener(this);
        tv_beidou = findViewById(R.id.tv_beidou);
        tv_beidou.setOnClickListener(this);
    }

    private void initRecyclerView() {
        picList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);

        recycleViewAdapter = new RecycleViewAdapter(this, picList);
        recycleViewAdapter.setOnItemClickListener(position -> {
            Intent intent = new Intent(MainActivity.this, PicDetailShowActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("pic", (Serializable) picList.get(position));
            intent.putExtras(bundle);
            MainActivity.this.startActivity(intent);
            overridePendingTransition(R.anim.zoom_in, -1);
        });
        recyclerView.setAdapter(recycleViewAdapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        refreshRecycleView();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_camera:
            case R.id.iv_capture:
                startCamera();
                break;
            case R.id.iv_excel:
                try {
                    //将Excel中的数据读取成实体
                    ExcelUtil reader = new ExcelUtil(Environment.getExternalStorageDirectory().getAbsolutePath() + "/宗地属性表.xlsx");
                    List<Map<String, String>> list = reader.getDatas(500, 0, 0);
                    ArrayList<OneParcelModel> models = new ArrayList<>();
                    if (list != null && list.size() > 0) {
                        for (Map<String, String> map : list) {
                            models.add(new OneParcelModel(map.get("县区代码"), map.get("地籍区"), map.get("地籍子区"), map.get("宗地代码"), map.get("土地权利人")));
                        }
                    }
                    //

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.tv_beidou:
                Intent intent = new Intent(this, BeidouMainActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @AfterPermissionGranted(RC_CAMERA)
    public void startCamera() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            //创建照片文件夹
            String createTime = TimeUtil.getCurrentTime().replace(":", ".");
            String picFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/1北斗资料归档/新文档 " + createTime;
            File picFolder = new File(picFolderPath);
            if (!picFolder.exists()) {
                picFolder.mkdirs();
            }
            // Already have permission, do the thing
            Intent intent = new Intent();
            intent.setClass(this, CameraActivity.class);
            intent.putExtra("pic_folder_path", picFolderPath);
            intent.putExtra("create_time", createTime);
            intent.putExtra("continue_capture", 0);
            startActivityForResult(intent, 0);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "需要调用相机及读取存储卡的权限！", RC_CAMERA, perms);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshRecycleView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshRecycleView();
    }

    /**
     * 刷新照片列表
     */
    private void refreshRecycleView() {
        Observable
                .create(emitter -> {
                    List<Pic> picList = MyApplication.db.picDao().getAll();
                    emitter.onNext(picList);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    picList = (List<Pic>) o;
                    recycleViewAdapter.setData(picList);
                    recycleViewAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
//        startCamera();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // Some permissions have been denied
    }

}
