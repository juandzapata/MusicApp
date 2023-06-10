package com.juanza.musicapp;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private List<Song> songList;
    private List<Boolean> isPlayingList;
    private OnSongActionListener songActionListener;

    public SongAdapter(List<Song> songList, OnSongActionListener songActionListener) {
        this.songList = songList;
        this.isPlayingList = new ArrayList<>(Collections.nCopies(songList.size(), false));
        this.songActionListener = songActionListener;
    }

    public interface OnSongActionListener {
        void onPlayButtonClicked(int position);

        void onPauseButtonClicked(int position);

        void onDownloadButtonClicked(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_song, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songList.get(holder.getAdapterPosition());
        holder.titleTextView.setText(song.getTitle());
        holder.artistTextView.setText(song.getArtist());

        // Cargar la imagen desde la ruta de archivo local
        Bitmap imageFile = song.getCoverImageUrl();
        holder.coverImageView.setImageBitmap(imageFile);


        if (song.isDownloaded()) {
            holder.downloadButton.setVisibility(View.GONE);
            holder.playButton.setVisibility(song.isPlaying() ? View.GONE : View.VISIBLE);
            holder.pauseButton.setVisibility(song.isPlaying() ? View.VISIBLE : View.GONE);
        } else {
            holder.downloadButton.setVisibility(View.VISIBLE);
            holder.playButton.setVisibility(View.GONE);
            holder.pauseButton.setVisibility(View.GONE);
        }

        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songActionListener.onDownloadButtonClicked(holder.getAdapterPosition());
                song.setDownloaded(true);
                notifyDataSetChanged();
            }
        });

        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songActionListener.onPlayButtonClicked(holder.getAdapterPosition());
                song.setPlaying(true);
                notifyDataSetChanged();
            }
        });

        holder.pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songActionListener.onPauseButtonClicked(holder.getAdapterPosition());
                song.setPlaying(false);
                notifyDataSetChanged();
            }
        });
    }


    @Override
    public int getItemCount() {
        return songList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView coverImageView;
        public TextView titleTextView;
        public TextView artistTextView;
        public Button downloadButton;
        public Button playButton;
        public Button pauseButton;
        public boolean isPlaying;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.coverImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            artistTextView = itemView.findViewById(R.id.artistTextView);
            downloadButton = itemView.findViewById(R.id.downloadButton);
            playButton = itemView.findViewById(R.id.playButton);
            pauseButton = itemView.findViewById(R.id.pauseButton);
            isPlaying = false;
        }

        public void bindData(Song song) {
            titleTextView.setText(song.getTitle());
            artistTextView.setText(song.getArtist());

            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Oculta el botón de Descargar
                    downloadButton.setVisibility(View.GONE);
                    // Muestra el botón de Play
                    playButton.setVisibility(View.VISIBLE);
                }
            });

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Oculta el botón de Play
                    playButton.setVisibility(View.GONE);
                    // Muestra el botón de Pausa
                    pauseButton.setVisibility(View.VISIBLE);
                }
            });

            pauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Oculta el botón de Pausa
                    pauseButton.setVisibility(View.GONE);
                    // Muestra el botón de Play
                    playButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}