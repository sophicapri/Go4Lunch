package com.sophieopenclass.go4lunch.controllers.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sophieopenclass.go4lunch.R;

public class WorkmatesDetailsAdapter extends RecyclerView.Adapter<WorkmatesDetailsAdapter.WorkmatesDetailHolder>  {

    @NonNull
    @Override
    public WorkmatesDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workmates_restaurant_preview,
                parent, false);
        return new WorkmatesDetailHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmatesDetailHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class WorkmatesDetailHolder extends RecyclerView.ViewHolder {

        public WorkmatesDetailHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
