package com.beidouspatial.universalscanapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

import com.beidouspatial.universalscanapp.Event.PhotoEvent;

import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.lib.detector.Line2D;
import net.doo.snap.ui.EditPolygonImageView;
import net.doo.snap.ui.MagnifierView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by ls on 2017/8/9.
 * 拍完照图片展示界面
 */
public class CutImageActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView resultImageView;
    private EditPolygonImageView editPolygonView;
    private MagnifierView magnifierView;
    private ImageView iv_back;
    private ImageView iv_add;
    private ImageView iv_confirm;
    public static final int IV_ADD = 0;
    public static final int IV_CONFIRM = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut_image);
        resultImageView = findViewById(R.id.resultImageView);
        editPolygonView = findViewById(R.id.polygonView);
        magnifierView = findViewById(R.id.magnifier);
        iv_back = findViewById(R.id.iv_back);
        iv_add = findViewById(R.id.iv_add);
        iv_confirm = findViewById(R.id.iv_confirm);
        iv_back.setOnClickListener(this);
        iv_add.setOnClickListener(this);
        iv_confirm.setOnClickListener(this);
        resultImageView.setImageBitmap(MyApplication.cameraPhoto);

        editPolygonView.setImageBitmap(MyApplication.cameraPhoto);
        // MagifierView should be set up every time when editPolygonView is set with new image
        magnifierView.setupMagnifier(editPolygonView);
        new InitImageViewTask().executeOnExecutor(Executors.newSingleThreadExecutor(), MyApplication.cameraPhoto);
    }

    /**
     * 使用EditPolygonView中的方法将图片按照剪裁框进行剪裁
     *
     * @return 剪裁后的图片Bitmap
     */
    private Bitmap crop() {
        // crop & warp image by selected polygon (editPolygonView.getPolygon())
        final Bitmap documentImage = new ContourDetector().processImageF(
                ((BitmapDrawable) editPolygonView.getDrawable()).getBitmap(), editPolygonView.getPolygon(), ContourDetector.IMAGE_FILTER_NONE);
        return documentImage;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                //返回到拍照界面
                finish();
                break;
            case R.id.iv_add:
                //保存图片
                postCameraPhoto(IV_ADD);
                //返回到拍照界面
                finish();
                break;
            case R.id.iv_confirm:
//                //保存图片
//                postCameraPhoto(IV_CONFIRM);
//                //跳转到主界面并刷新
//                finish();

                //edit by ls at 2019.04.16 剪裁后的展示界面
                MyApplication.cutCameraPhoto = crop();
                Intent intent = new Intent();
                intent.putExtra("sfz", 0);
                intent.setClass(this, ShowImageActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    /**
     * 照片拍摄完保存在本地
     *
     * @param event
     */
    @Subscribe(priority = 100)
    public void onEventPhoto(PhotoEvent event) {
        int saveType = event.saveType;
        finish();
    }

    private void postCameraPhoto(int saveType) {
        PhotoEvent event = new PhotoEvent();
        event.photo = crop();
        event.saveType = saveType;
        EventBus.getDefault().post(event);
    }

    /**
     * Detects horizontal and vertical lines and polygon of the given bitmap image.
     * Initializes EditPolygonImageView with detected lines and polygon.
     */
    class InitImageViewTask extends AsyncTask<Bitmap, Void, InitImageResult> {

        @Override
        protected InitImageResult doInBackground(Bitmap... params) {
            Bitmap image = params[0];
            ContourDetector detector = new ContourDetector();
            final DetectionResult detectionResult = detector.detect(image);
            Pair<List<Line2D>, List<Line2D>> linesPair = null;
            List<PointF> polygon = new ArrayList<>(EditPolygonImageView.DEFAULT_POLYGON);
            switch (detectionResult) {
                case OK:
                case OK_BUT_BAD_ANGLES:
                case OK_BUT_TOO_SMALL:
                case OK_BUT_BAD_ASPECT_RATIO:
                    linesPair = new Pair<>(detector.getHorizontalLines(), detector.getVerticalLines());
                    polygon = detector.getPolygonF();
                    break;
            }
            return new InitImageResult(linesPair, polygon);
        }

        @Override
        protected void onPostExecute(final InitImageResult initImageResult) {
            // set detected polygon and lines into EditPolygonImageView
            editPolygonView.setPolygon(initImageResult.polygon);
            if (initImageResult.linesPair != null) {
                editPolygonView.setLines(initImageResult.linesPair.first, initImageResult.linesPair.second);
            }
        }
    }

    class InitImageResult {
        final Pair<List<Line2D>, List<Line2D>> linesPair;
        final List<PointF> polygon;

        InitImageResult(final Pair<List<Line2D>, List<Line2D>> linesPair, final List<PointF> polygon) {
            this.linesPair = linesPair;
            this.polygon = polygon;
        }
    }

}