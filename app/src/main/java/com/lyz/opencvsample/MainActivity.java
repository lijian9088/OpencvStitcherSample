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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView tvImageStitch;
    private TextView tvVideoFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }

    private void initView() {
        tvImageStitch = findViewById(R.id.tvImageStitch);
        tvVideoFrame = findViewById(R.id.tvVideoFrame);
    }

    private void initEvent() {
        tvImageStitch.setOnClickListener(v -> {
            startActivity(new Intent(this, ImageStitchActivity.class));
        });
        tvVideoFrame.setOnClickListener(v -> {
            startActivity(new Intent(this, VideoActivity.class));
        });
    }

}