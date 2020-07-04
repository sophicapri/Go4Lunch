package com.sophieopenclass.go4lunch.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sophieopenclass.go4lunch.R;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {
    private List<String> photoUrls;

    public SliderAdapter(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_item_container, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        holder.bind(photoUrls.get(position));
    }

    @Override
    public int getItemCount() {
        return photoUrls.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.imageSlide);
        }

        void bind(String urlPhoto) {
            Glide.with(imageView.getContext())
                    .load(urlPhoto)
                    .apply(RequestOptions.fitCenterTransform())
                    .into(imageView);
        }
    }
}
