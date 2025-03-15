package com.example.fitlab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import java.util.List;

public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.TutorialViewHolder> {
    private List<Tutorial> tutorials;
    private Lifecycle lifecycle;

    public TutorialAdapter(List<Tutorial> tutorials, Lifecycle lifecycle) {
        this.tutorials = tutorials;
        this.lifecycle = lifecycle;
    }

    @NonNull
    @Override
    public TutorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tutorial, parent, false);
        return new TutorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorialViewHolder holder, int position) {
        Tutorial tutorial = tutorials.get(position);
        holder.bind(tutorial);
    }

    @Override
    public int getItemCount() {
        return tutorials.size();
    }

    class TutorialViewHolder extends RecyclerView.ViewHolder {
        YouTubePlayerView youTubePlayerView;
        TextView tutorialTitle;

        TutorialViewHolder(@NonNull View itemView) {
            super(itemView);
            youTubePlayerView = itemView.findViewById(R.id.youtube_player_view);
            tutorialTitle = itemView.findViewById(R.id.tutorial_title);
            lifecycle.addObserver(youTubePlayerView);
        }

        void bind(Tutorial tutorial) {
            tutorialTitle.setText(tutorial.getTitle());
            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.cueVideo(tutorial.getVideoId(), 0);
                }
            });
        }
    }
}