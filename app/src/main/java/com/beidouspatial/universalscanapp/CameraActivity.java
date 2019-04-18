package com.beidouspatial.universalscanapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.core.view.WindowCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.beidouspatial.universalscanapp.Event.PhotoEvent;
import com.beidouspatial.universalscanapp.Event.SFZIfSaveEvent;
import com.beidouspatial.universalscanapp.Event.SFZPhotoEvent;
import com.beidouspatial.universalscanapp.activity.ShowSFZImageActivity;
import com.beidouspatial.universalscanapp.db.entity.Pic;
import com.beidouspatial.universalscanapp.util.BitmapUtil;
import com.beidouspatial.universalscanapp.util.BubbleSort;

import net.doo.snap.camera.AutoSnappingController;
import net.doo.snap.camera.ContourDetectorFrameHandler;
import net.doo.snap.camera.PictureCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.ui.PolygonView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CameraActivity extends AppCompatActivity implements PictureCallback, ContourDetectorFrameHandler.ResultHandler {

    private ScanbotCameraView scanbotCameraView;
    private PolygonView polygonView;
    private ImageView resultImageView;
    private ContourDetectorFrameHandler contourDetectorFrameHandler;
    private AutoSnappingController autoSnappingController;
    private Toast userGuidanceToast;

    private boolean flashEnabled = false;
    private boolean autoSnappingEnabled = false;
    private TabLayout tl_type_select;
    private String[] titles = new String[]{"身份证", "户口本", "权属来源证明", "房屋照片", "无异议声明书", "标准文档"};
    private String picFolderPath;
    private String createTime;
    private List<String> picNameList;
    private ImageView ic_idcard;
    private ImageView ic_idcard_emblem;
    private Bitmap headBitmap;
    private Bitmap emblemBitmap;
    private int continue_capture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        //初始化数据
        picNameList = new ArrayList<>();

        //获取照片存储文件夹
        Intent intent = getIntent();
        picFolderPath = intent.getStringExtra("pic_folder_path");
        createTime = intent.getStringExtra("create_time");

        //是否为继续拍摄
        continue_capture = intent.getIntExtra("continue_capture", 0);
        if (continue_capture == 1) {
            Bundle bundle = intent.getExtras();
            pic = (Pic) bundle.get("pic");
            String[] strings = pic.getPicPath().split(",");
            for (String string : strings) {
                picNameList.add(string);
            }
        }

        //Scanbot相机
        scanbotCameraView = findViewById(R.id.scanbotCameraView);
        scanbotCameraView.setCameraOpenCallback(() -> scanbotCameraView.postDelayed(() -> {
            scanbotCameraView.continuousFocus();
            scanbotCameraView.useFlash(flashEnabled);
        }, 700));
        //相机所拍摄图片
        resultImageView = findViewById(R.id.resultImageView);

        //ContourDetectorFrameHandler 控制相机拍照识别参数及自动识别框
        contourDetectorFrameHandler = ContourDetectorFrameHandler.attach(scanbotCameraView);
        // Please note: https://github.com/doo/Scanbot-SDK-Examples/wiki/Detecting-and-drawing-contours#contour-detection-parameters
        contourDetectorFrameHandler.setAcceptedAngleScore(60);
        contourDetectorFrameHandler.setAcceptedSizeScore(70);
        polygonView = findViewById(R.id.polygonView);
        contourDetectorFrameHandler.addResultHandler(polygonView);
        contourDetectorFrameHandler.addResultHandler(this);

        //AutoSnappingController 控制自动识别拍照类
        autoSnappingController = AutoSnappingController.attach(scanbotCameraView, contourDetectorFrameHandler);

        scanbotCameraView.addPictureCallback(this);

        userGuidanceToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        userGuidanceToast.setGravity(Gravity.CENTER, 0, 0);

        //拍照类型选择
        tl_type_select = findViewById(R.id.tl_type_select);
        for (int i = 0; i < titles.length; i++) {
            tl_type_select.addTab(tl_type_select.newTab());
        }
        for (int i = 0; i < titles.length; i++) {
            tl_type_select.getTabAt(i).setText(titles[i]);
        }
        ic_idcard = findViewById(R.id.ic_idcard);
        ic_idcard_emblem = findViewById(R.id.ic_idcard_emblem);
        tl_type_select.setOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals("身份证")) {
                    ic_idcard.setVisibility(View.VISIBLE);
                    ic_idcard_emblem.setVisibility(View.GONE);
                } else {
                    ic_idcard.setVisibility(View.GONE);
                    ic_idcard_emblem.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        findViewById(R.id.snap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanbotCameraView.takePicture(false);
            }
        });

        setAutoSnapEnabled(autoSnappingEnabled);
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
        scanbotCameraView.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    /**
     * 身份证拍摄完保存在本地
     *
     * @param event
     */
    Pic pic;

    @Subscribe
    public void onEventSFZPhoto(SFZPhotoEvent event) {
        int saveType = event.saveType;
        if (headBitmap == null) {
            headBitmap = event.photo;
            ic_idcard.setVisibility(View.GONE);
            ic_idcard_emblem.setVisibility(View.VISIBLE);
        } else {
            //合成身份证
            int width = 2480;
            int height = 3506;
            Bitmap photo = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(photo);
            canvas.drawColor(Color.parseColor("#ffffff"));
            Bitmap zoomBitmap1 = BitmapUtil.zoomImage(BitmapUtil.toRoundCorner(headBitmap, 60), 1008, 642);
            Bitmap zoomBitmap2 = BitmapUtil.zoomImage(BitmapUtil.toRoundCorner(event.photo, 60), 1008, 642);
            canvas.drawBitmap(zoomBitmap1, (float) (width * 0.3), (float) (height * 0.215), null);
            canvas.drawBitmap(zoomBitmap2, (float) (width * 0.3), (float) (height * 0.215 + zoomBitmap1.getHeight() + height * 0.098), null);
            //将合成的身份证保存在本地
            String picName = titles[tl_type_select.getSelectedTabPosition()] + getPicName(picFolderPath, titles[tl_type_select.getSelectedTabPosition()]);
            picNameList.add(picName);
            //将照片保存在本地
            MyBitmapUtils.saveBitmapToLocal(photo, picFolderPath + "/" + picName + ".jpg");

            //
            MyApplication.cutCameraPhoto = photo;
            Intent intent = new Intent();
            intent.setClass(this, ShowImageActivity.class);
            intent.putExtra("sfz", 1);
            startActivity(intent);

            //保存在数据库中
//            saveToDB();
            ic_idcard.setVisibility(View.VISIBLE);
            ic_idcard_emblem.setVisibility(View.GONE);
            headBitmap = null;
        }

    }

    @Subscribe
    public void onEventSFZIfSave(SFZIfSaveEvent event) {
        saveToDB();
        if (event.ifClose) {
            finish();
        }
    }

    private void saveToDB() {
        if (pic == null) {
            pic = new Pic();
            pic.setCreateTime(createTime);
            pic.setFolderName(picFolderPath.substring(picFolderPath.lastIndexOf("/") + 1));
        }
        String picPath = "";
        for (int i = 0; i < picNameList.size(); i++) {
            picPath += picNameList.get(i) + ",";
        }
        pic.setPicPath(picPath);
        Observable
                .create(emitter -> {
                    //添加到数据库中
                    MyApplication.db.picDao().insertAll(pic);
                    emitter.onNext(1);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                });
    }

    @Override
    public void onBackPressed() {
        if (continue_capture == 1) {
            Intent intent = new Intent();
            intent.putExtra("uid", pic.getUid());
            setResult(0, intent);
        }
        super.onBackPressed();
    }

    /**
     * 照片拍摄完保存在本地
     *
     * @param event
     */
    @Subscribe(priority = 50)
    public void onEventPhoto(PhotoEvent event) {
        int saveType = event.saveType;
        Bitmap photo = event.photo;
        String picName = titles[tl_type_select.getSelectedTabPosition()] + getPicName(picFolderPath, titles[tl_type_select.getSelectedTabPosition()]);
        picNameList.add(picName);
        //将照片保存在本地
        MyBitmapUtils.saveBitmapToLocal(photo, picFolderPath + "/" + picName + ".jpg");
        if (saveType == CutImageActivity.IV_CONFIRM) {
            if (pic == null) {
                pic = new Pic();
                pic.setCreateTime(createTime);
                pic.setFolderName(picFolderPath.substring(picFolderPath.lastIndexOf("/") + 1));
            }
            String picPath = "";
            for (int i = 0; i < picNameList.size(); i++) {
                picPath += picNameList.get(i) + ",";
            }
            pic.setPicPath(picPath);
            Observable
                    .create(emitter -> {
                        //添加到数据库中
                        MyApplication.db.picDao().insertAll(pic);
                        emitter.onNext(1);
                        emitter.onComplete();
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                        //回传数据必须放在finish方法之前
                        if (continue_capture == 1) {
                            Intent intent = new Intent();
                            intent.putExtra("uid", pic.getUid());
                            setResult(0, intent);
                        }
                        //销毁界面
                        finish();
                    });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanbotCameraView.onPause();
    }

    @Override
    public boolean handleResult(final ContourDetectorFrameHandler.DetectedFrame detectedFrame) {
        // Here you are continuously notified about contour detection results.
        // For example, you can show a user guidance text depending on the current detection status.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //根据拍照环境给用户相应提示
                showUserGuidance(detectedFrame.detectionResult);
            }
        });
        return false; // typically you need to return false
    }

    private void showUserGuidance(final DetectionResult result) {
        if (!autoSnappingEnabled) {
            return;
        }
        switch (result) {
            case OK:
                userGuidanceToast.setText("不要移动");
                userGuidanceToast.show();
                break;
            case OK_BUT_TOO_SMALL:
                userGuidanceToast.setText("移近一些");
                userGuidanceToast.show();
                break;
            case OK_BUT_BAD_ANGLES:
                userGuidanceToast.setText("移远一些");
                userGuidanceToast.show();
                break;
            case ERROR_NOTHING_DETECTED:
                userGuidanceToast.setText("没有文档");
                userGuidanceToast.show();
                break;
            case ERROR_TOO_NOISY:
                userGuidanceToast.setText("背景不清");
                userGuidanceToast.show();
                break;
            case ERROR_TOO_DARK:
                userGuidanceToast.setText("光线太暗");
                userGuidanceToast.show();
                break;
            default:
                userGuidanceToast.cancel();
                break;
        }
    }

    /**
     * 处理所拍的照片
     *
     * @param image
     * @param imageOrientation
     */
    @Override
    public void onPictureTaken(byte[] image, int imageOrientation) {
        /********************************改变图片尺寸开始*********************/
        // Here we get the full image from the camera.
        // Implement a suitable async(!) detection and image handling here.
        // This is just a demo showing detected image as downscaled preview image.

        // Decode Bitmap from bytes of original image:
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1; // use 1 for original size (if you want no downscale)!
        // in this demo we downscale the image to 1/8 for the preview.
        Bitmap originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);

        // rotate original image if required:
        // TODO: 2017/8/15 tip by ls 此处获取到的图片方向恒为"0",暂时未查到原因
        if (imageOrientation > 0) {
            final Matrix matrix = new Matrix();
            matrix.setRotate(imageOrientation, originalBitmap.getWidth() / 2f, originalBitmap.getHeight() / 2f);
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);
        }

        //如果图片的宽度大于高度则顺时针旋转图片90度
        if (originalBitmap.getHeight() < originalBitmap.getWidth()) {
            originalBitmap = rotateBitmap(originalBitmap, 90);
        }

        // Run document detection on original image:
        final ContourDetector detector = new ContourDetector();
        detector.detect(originalBitmap);
        //静态保存原始bitmap，方便传递
        MyApplication.cameraPhoto = originalBitmap;
//        final Bitmap resultBitmap = detector.processImageAndRelease(originalBitmap, detector.getPolygonF(), ContourDetector.IMAGE_FILTER_NONE);
        /********************************改变图片尺寸结束*********************/

        resultImageView.post(new Runnable() {
            @Override
            public void run() {
                //当前界面展示所拍摄到的照片
//                resultImageView.setImageBitmap(resultBitmap);
                //重置拍照状态
                scanbotCameraView.continuousFocus();
                scanbotCameraView.startPreview();

                //开启新界面，对所拍照片进行纠正剪切
                if (titles[tl_type_select.getSelectedTabPosition()].equals("身份证")) {
                    Intent intent = new Intent();
                    if (headBitmap == null) {
                        intent.putExtra("if_head", true);
                    } else {
                        intent.putExtra("if_head", false);
                    }
                    intent.setClass(CameraActivity.this, ShowSFZImageActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(CameraActivity.this, CutImageActivity.class);
                    startActivity(intent);
                }
//                CameraActivity.this.finish();
            }
        });
    }

    /**
     * 是否开启自动拍照
     *
     * @param enabled 是否开启
     */
    private void setAutoSnapEnabled(boolean enabled) {
        autoSnappingController.setEnabled(enabled);
        contourDetectorFrameHandler.setEnabled(enabled);
        polygonView.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param alpha  旋转角度，可正可负
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 根据指定的文件前缀以及文件所在路径，计算文件名称后索引
     * 0/.../../.../身份证1.jpg,0/.../../.../身份证2.jpg
     *
     * @param picPath 照片所在路径
     * @param reg     照片前缀（如：身份证）
     * @return
     */
    public static int getPicName(String picPath, String reg) {
        int i = 0;
        File fileDic = new File(picPath);
        String[] fileNames = fileDic.list((dir, filename) -> filename.contains(reg));
        if (fileNames != null && fileNames.length > 0) {
            int[] subFileNamesInt = new int[fileNames.length];
            for (int j = 0; j < fileNames.length; j++) {
                String substring = fileNames[j].substring(reg.length(), fileNames[j].lastIndexOf("."));
                if (!substring.equals("")) {
                    subFileNamesInt[j] = Integer.valueOf(substring);
                }
            }
            try {
                int[] subFileNameInts = BubbleSort.bubbleSort(subFileNamesInt);
                i = subFileNameInts[subFileNameInts.length - 1];
            } catch (NullPointerException e) {
                return i + 1;
            }
        }
        return i + 1;
    }

}
