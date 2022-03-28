package com.example.passwordmanager.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.passwordmanager.R;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    List<String> arrayList;
    Context context;
    ArrayList<String> selected = new ArrayList<>();
    Boolean isSelected = false;

    public ArrayList<String> getSelected() {
        return selected;
    }

    public ImageAdapter(List<String> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_tile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Glide.with(context).load(arrayList.get(position)).into(holder.imageView);

        if (!selected.contains(arrayList.get(position))) {
            holder.imageView.setColorFilter(Color.TRANSPARENT);
        }else {
            holder.imageView.setColorFilter(Color.argb(150,21, 90, 237));
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageTile);

            imageView.setOnClickListener((View v) -> {

                if (selected.contains(arrayList.get(getAdapterPosition()))) {
                    selected.remove(arrayList.get(getAdapterPosition()));
                } else {
                    selected.add(arrayList.get(getAdapterPosition()));
                }
                notifyDataSetChanged();
                Log.e("Tag", selected.toString());

            });

        }
    }
}
