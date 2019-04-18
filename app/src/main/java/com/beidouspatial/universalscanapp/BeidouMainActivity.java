package com.beidouspatial.universalscanapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class BeidouMainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beidou_main);
        // TODO: 2019/4/17 由于逻辑和MainActivity类似，应该将其抽取为BaseActivity
        recyclerView = findViewById(R.id.recyclerView);
    }

}
