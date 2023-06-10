package com.juanza.musicapp;

import android.graphics.Bitmap;

public class Song {
    private String title;
    private String artist;
    private Bitmap coverImageUrl;
    private boolean playing;
    private boolean downloaded;

    private Result result;

    public Song(String title, String artist, Bitmap coverImageUrl, Result result) {
        this.title = title;
        this.artist = artist;
        this.result = result;
        this.coverImageUrl = coverImageUrl;
        this.playing = false;
        this.downloaded = false;
    }

    public void setResult(Result result){
        this.result = result;
    }

    public Result getResult() {
        return result;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public Bitmap getCoverImageUrl() {
        return coverImageUrl;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
}

