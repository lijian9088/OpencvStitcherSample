package com.lyz.opencvsample.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lyz.opencvsample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mac
 * @create 2021/02/08
 * @Describe
 */
public class VideoFrameAdapter extends RecyclerView.Adapter<VideoFrameAdapter.ImageViewHolder> {

    ArrayList<Bitmap> list = new ArrayList<>();

    public void setList(ArrayList<Bitmap> urlList) {
        if (urlList == null) {
            return;
        }
        this.list.clear();
        this.list.addAll(urlList);
        notifyDataSetChanged();
    }

    public ArrayList<Bitmap> getData(){
        return list;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_frame, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(list.get(position))
                .fitCenter()
                .into(holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(holder.getAdapterPosition());
                }
            }
        });
        holder.textView.setText(String.valueOf(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv);
            textView = itemView.findViewById(R.id.tv);
        }
    }


    public interface OnItemClickListener{
        void onClick(int position);
    }

    public OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
