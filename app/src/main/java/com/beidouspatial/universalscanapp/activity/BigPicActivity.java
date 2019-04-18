package com.beidouspatial.universalscanapp.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.beidouspatial.universalscanapp.R;
import com.beidouspatial.universalscanapp.adapter.MyPagerAdapter;

import java.util.List;

public class BigPicActivity extends AppCompatActivity implements View.OnClickListener {

    private List<String> pic_list;
    private String folderName;
    private ImageView iv_delete;
    private ImageView iv_re_capture;
    private ImageView iv_back;
    private ViewPager vp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_pic);

        Intent intent = getIntent();
        pic_list = (List<String>) intent.getSerializableExtra("pic_list");
        folderName = intent.getStringExtra("folderName");
        int click_position = intent.getIntExtra("click_position", -1);

        iv_delete = findViewById(R.id.iv_delete);
        iv_re_capture = findViewById(R.id.iv_re_capture);
        iv_back = findViewById(R.id.iv_back);
        vp = findViewById(R.id.vp);
        iv_delete.setOnClickListener(this);
        iv_re_capture.setOnClickListener(this);
        iv_back.setOnClickListener(this);

        vp.setAdapter(new MyPagerAdapter(this,folderName,pic_list));
        vp.setCurrentItem(click_position);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_delete:
                Toast.makeText(this, "删除成功！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_re_capture:
                Toast.makeText(this, "重拍成功！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }
}
