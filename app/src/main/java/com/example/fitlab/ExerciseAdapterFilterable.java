package com.example.fitlab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitlab.Exercise;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapterFilterable extends RecyclerView.Adapter<ExerciseAdapterFilterable.ExerciseViewHolder> {
    private List<Exercise> originalList;
    private List<Exercise> filteredList;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnExerciseClickListener onExerciseClickListener;

    public ExerciseAdapterFilterable(List<Exercise> exercises, OnExerciseClickListener listener) {
        this.originalList = exercises;
        this.filteredList = new ArrayList<>(exercises);
        this.onExerciseClickListener = listener;
    }

    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExerciseViewHolder holder, int position) {
        Exercise exercise = filteredList.get(position);
        holder.exerciseName.setText(exercise.getName());
        holder.exerciseDescription.setText(exercise.getDescription());

        holder.itemView.setSelected(selectedPosition == position);
        holder.itemView.setOnClickListener(v -> {
            notifyItemChanged(selectedPosition);
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(selectedPosition);
            onExerciseClickListener.onExerciseClick(exercise);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseName, exerciseDescription;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            exerciseDescription = itemView.findViewById(R.id.exerciseDescription);
        }
    }

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }



    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Exercise> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(originalList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Exercise exercise : originalList) {
                        if (exercise.getMuscle().toLowerCase().contains(filterPattern)) {
                            filteredList.add(exercise);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                if (results != null && results.values != null) {
                    filteredList.addAll((List<Exercise>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }


    }
