package com.example.fitlab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GymAdapter extends RecyclerView.Adapter<GymAdapter.GymViewHolder> {
    private List<String> gymList;

    public GymAdapter(List<String> gymList) {
        this.gymList = gymList;
    }

    @NonNull
    @Override
    public GymViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gym_list_item, parent, false);
        return new GymViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GymViewHolder holder, int position) {
        String gym = gymList.get(position);
        holder.gymName.setText(gym);
    }

    @Override
    public int getItemCount() {
        return gymList.size();
    }

    public static class GymViewHolder extends RecyclerView.ViewHolder {
        TextView gymName;

        public GymViewHolder(@NonNull View itemView) {
            super(itemView);
            gymName = itemView.findViewById(R.id.gym_name);
        }
    }
}