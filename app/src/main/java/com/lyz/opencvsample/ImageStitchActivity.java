package com.lyz.opencvsample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.donkingliang.imageselector.utils.ImageSelector;
import com.github.chrisbanes.photoview.PhotoView;
import com.lyz.opencvsample.adapter.ImageAdapter;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageStitchActivity extends AppCompatActivity {

    private static final String TAG = ImageStitchActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 100;
    private static final int REQUEST_CODE_PERMISSION = 101;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    /* Now enable camera view to start receiving frames */
//                    mOpenCvCameraView.setOnTouchListener(Puzzle15Activity.this);
//                    mOpenCvCameraView.enableView();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private TextView tvStitch;
    private TextView tvLoad;
    private RecyclerView rv;
    private PhotoView ivResult;
    private ImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_stitch);
        initView();
        initEvent();
    }

    private void initView() {
        tvStitch = findViewById(R.id.tvStitch);
        tvLoad = findViewById(R.id.tvLoad);
        rv = findViewById(R.id.rv);
        ivResult = findViewById(R.id.ivResult);

        rv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        adapter = new ImageAdapter();
        rv.setAdapter(adapter);
    }

    private void initEvent() {
        tvLoad.setOnClickListener((v) -> {
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    || PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                LogUtils.d("请求权限");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
                return;
            }
            takePhoto();
        });
        tvStitch.setOnClickListener((v) -> {
            List<String> data = adapter.getData();
            if (data.size() > 0) {
                stitchImages(data);
            } else {
                ToastUtils.showShort("先加载图片!");
            }
        });
    }

    private void takePhoto() {
//        this.selectUri = selectUri;
//        //单选
//        ImageSelector.builder()
//                .useCamera(true) // 设置是否使用拍照
//                .setSingle(true)  //设置是否单选
//                .canPreview(true) //是否可以预览图片，默认为true
//                .start(this, REQUEST_CODE); // 打开相册

        //限数量的多选(比如最多9张)
        ImageSelector.builder()
                .useCamera(true) // 设置是否使用拍照
                .setSingle(false)  //设置是否单选
                .setMaxSelectCount(9) // 图片的最大选择数量，小于等于0时，不限数量。
//                .setSelected(selected) // 把已选的图片传入默认选中。
                .canPreview(true) //是否可以预览图片，默认为true
                .start(this, REQUEST_CODE); // 打开相册
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void stitchImages(List<String> list) {

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("开始拼接...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            String[] array = new String[list.size()];
            list.toArray(array);
            ImagesStitchUtil.StitchImages(array, new ImagesStitchUtil.onStitchResultListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        Glide.with(ImageStitchActivity.this)
                                .load(bitmap)
                                .centerInside()
                                .into(ivResult);
                    });
                }

                @Override
                public void onError(String errorMsg) {
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        String str = String.format("onError:%s", errorMsg);
                        LogUtils.d(str);
                        ToastUtils.showShort(str);
                    });
                }
            });
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission_group.STORAGE);
            if (PackageManager.PERMISSION_GRANTED == permission) {
                takePhoto();
            } else {
                ToastUtils.showShort("存储权限被拒绝");
            }
        }
        if (requestCode == REQUEST_CODE && data != null) {
            //获取选择器返回的数据
            ArrayList<String> images = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);

            /**
             * 是否是来自于相机拍照的图片，
             * 只有本次调用相机拍出来的照片，返回时才为true。
             * 当为true时，图片返回的结果有且只有一张图片。
             */
//            boolean isCameraImage = data.getBooleanExtra(ImageSelector.IS_CAMERA_IMAGE, false);
//            if (isCameraImage) {
//                String path = images.get(0);
//                paths[selectUri] = path;
//                LogUtils.d("path:" + path);
//                Glide.with(this)
//                        .load(path)
//                        .fitCenter()
//                        .into(selectUri == 0 ? iv1 : iv2);
//            }

            LogUtils.d(images);
            adapter.setList(images);
        }
    }
}