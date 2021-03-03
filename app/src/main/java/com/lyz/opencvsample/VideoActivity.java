package com.lyz.opencvsample;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;
import com.bumptech.glide.Glide;
import com.lyz.opencvsample.adapter.BigBitmapAdapter;
import com.lyz.opencvsample.adapter.VideoFrameAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoActivity extends AppCompatActivity {

    private static final int VIDEO_REQUEST_CODE = 100;
    private Button btnLoadVideo;
    private RecyclerView rv;
    private ImageView ivPreView;
    private RecyclerView rvBigBitmap;
    private VideoFrameAdapter videoFrameAdapter;
    private BigBitmapAdapter bigBitmapAdapter;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        btnLoadVideo = findViewById(R.id.btnLoadVideo);
        rv = findViewById(R.id.rv);
        ivPreView = findViewById(R.id.ivPreView);
        rvBigBitmap = findViewById(R.id.rvBigBitmap);

        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        videoFrameAdapter = new VideoFrameAdapter();
        rv.setAdapter(videoFrameAdapter);
        videoFrameAdapter.setOnItemClickListener(position -> {
            Glide.with(this)
                    .load(videoFrameAdapter.getData().get(position))
                    .fitCenter()
                    .into(ivPreView);
        });
        rv.setScrollBarSize(SizeUtils.dp2px(10));
        rv.setHorizontalScrollBarEnabled(true);

        btnLoadVideo.setOnClickListener(v -> clickLoadVideo());

        rvBigBitmap.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bigBitmapAdapter = new BigBitmapAdapter();
        rvBigBitmap.setAdapter(bigBitmapAdapter);
    }

    private void clickLoadVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, VIDEO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("data:" + data.getData());
        if (requestCode == VIDEO_REQUEST_CODE) {
            if (data.getData() != null) {
                Uri uri = data.getData();

//                Cursor cursor = getContentResolver().query(uri, null, null,
//                        null, null);
//                cursor.moveToFirst();
//                // String imgNo = cursor.getString(0); // 图片编号
//                String v_path = cursor.getString(1); // 图片文件路径
//                String v_size = cursor.getString(2); // 图片大小
//                String v_name = cursor.getString(3); // 图片文件名
//                System.out.println("v_path="+v_path);
//                System.out.println("v_size="+v_size);
//                System.out.println("v_name="+v_name);

//                String path = getFilePathFromContentUri(uri, getContentResolver());
                File file = UriUtils.uri2File(uri);
                System.out.println("File=" + file.getAbsolutePath());

                showDialog();

                new Thread(()->{
                    parseVideoFrame(file);
                }).start();

//                parseVideoFrame(new File(path));

            }
        }
    }

    public void showDialog(){
        if(dialog == null){
            dialog = new ProgressDialog(this);
            dialog.setMessage("加载中...");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }
        dialog.show();
    }

    public void dismissDialog(){
        dialog.dismiss();
    }

    public void parseVideoFrame(File file) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(file.getPath());

        //时长
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        System.out.println("duration:" + duration);
        long videoDuration = Long.parseLong(duration);

        //帧
        List<Bitmap> list = videoFrameAdapter.getData();
        list.clear();

        for (long l = 0; l < videoDuration; l = l + 1000) {
            long time = l * 1000;

            Bitmap bitmap = retriever.getFrameAtTime(time);
//            float scale = (bitmap.getHeight() + 0.5f) / bitmap.getWidth();
//            int width = SizeUtils.dp2px(100);
//            int height = (int) (width * scale);
//            Bitmap newBitmap = ImageUtils.compressByScale(bitmap, width, height);
//            Bitmap newBitmap = ImageUtils.compressByScale(bitmap,0.2f,0.2f);

//            Bitmap bitmap = retriever.getScaledFrameAtTime(time,
//                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
//                    width, height);

            list.add(bitmap);
            runOnUiThread(() -> videoFrameAdapter.notifyDataSetChanged());
        }
//        saveBitmaps(list);
        createNewBitmap();
    }

    private void createNewBitmap() {
        ArrayList<Bitmap> data = videoFrameAdapter.getData();
        Bitmap bitmap = data.get(0);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int n = 8;
        int newWidth = (int) (width * 1f / n);

        ArrayList<Bitmap> list = new ArrayList<>();
//        list.add(bitmap);

        for (int i = 0; i < n; i++) {
            int x = i * newWidth;
            Bitmap clip = ImageUtils.clip(bitmap, x, 0, newWidth, height);
            list.add(clip);
        }

        for (int i = 1; i < data.size(); i++) {
            Bitmap b = data.get(i);
//            list.add(b);
            Bitmap clip = ImageUtils.clip(b, 0, 0, newWidth, height);
            list.add(clip);
        }

        runOnUiThread(() -> {
            bigBitmapAdapter.setList(list);
            dismissDialog();
        });
//        bigBitmapAdapter.setList(data);
    }

    private void saveBitmaps(List<Bitmap> list) {
        for (int i = 0; i < list.size(); i++) {
            ImageUtils.save2Album(list.get(i), Bitmap.CompressFormat.PNG);
        }
        runOnUiThread(()-> ToastUtils.showShort("保存成功"));
    }

}