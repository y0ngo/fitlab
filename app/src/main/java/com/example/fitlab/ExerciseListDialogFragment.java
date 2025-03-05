package com.example.fitlab;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import java.util.List;

public class ExerciseListDialogFragment extends DialogFragment {

    private List<String> exerciseNames;
    private OnExerciseSelectedListener listener;

    public ExerciseListDialogFragment(List<String> exerciseNames, OnExerciseSelectedListener listener) {
        this.exerciseNames = exerciseNames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Select an Exercise")
                .setItems(exerciseNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onExerciseSelected(exerciseNames.get(which));
                    }
                });
        return builder.create();
    }

    public interface OnExerciseSelectedListener {
        void onExerciseSelected(String exerciseName);
    }
}