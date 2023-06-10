package com.juanza.musicapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cafsoft.foundation.Data;
import cafsoft.foundation.HTTPURLResponse;
import cafsoft.foundation.URLSession;

public class MainActivity extends AppCompatActivity implements SongAdapter.OnSongActionListener {
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private List<Song> songList;
    private MusicServiceAPI musicService = null;
    private Root root = null;
    private EditText searchBox = null;
    private Button searchButton = null;
    private String currentFullFilename = "";
    private MediaPlayer mediaPlayer = null;
    private String fullFilename = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = new MediaPlayer();
        setContentView(R.layout.activity_main);
        fullFilename = getApplicationContext().getCacheDir() + "/";
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(view ->
                generateList());
    }

    public void generateList(){
        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Crear una lista de canciones (sustituye esto con tus datos reales)
        songList = createSongList();

        // Crear el adaptador de canciones
        songAdapter = new SongAdapter(songList, this);

        // Establecer el adaptador en el RecyclerView
        recyclerView.setAdapter(songAdapter);
        //fullFilename = getApplicationContext().getCacheDir() + "/";
    }

    // Método para crear una lista de canciones (sustituye esto con tus datos reales)
    private List<Song> createSongList() {
        List<Song> songList = new ArrayList<>();
        musicService = new MusicServiceAPI();
        searchBox = findViewById(R.id.requestBox);
        String searchTerm = searchBox.getText().toString();
        musicService.searchSongsByTerm(searchTerm, 20, new MusicServiceAPI.OnDataResponse() {
            @Override
            public void onChange(boolean isNetworkError, int statusCode, String response) {
                if(!isNetworkError){
                    if(statusCode == 200){
                        Gson gson = new Gson();
                        root = gson.fromJson(response, Root.class);
                        ArrayList<Result> results = root.getResults();
                        for (Result result: results) {
                            String atworkUrl = result.getArtworkUrl100();
                            String atworkFileName = result.getLocalArtworkFilename();
                            String fullFilenameAtwork = getApplicationContext().getCacheDir() + "/" + atworkFileName;

                            currentFullFilename = fullFilenameAtwork;

                            if(new File(fullFilenameAtwork).exists()) {

                                Bitmap bitmap = BitmapFactory.decodeFile(new File(fullFilenameAtwork).getAbsolutePath());
                                Song song = new Song(result.getTrackName(), result.getArtistName(), bitmap, result);

                                song.setDownloaded(songDownloaded(song));

                                songList.add(song);

                            } else {
                                URL url = null;

                                try {
                                    url = new URL(atworkUrl);
                                }catch(MalformedURLException e) {
                                    e.printStackTrace();
                                }

                                URLSession session = new URLSession();
                                session.downloadTask(url, (localUrl, response1, error) -> {
                                    HTTPURLResponse resp = (HTTPURLResponse) response1;

                                    if (error == null) {
                                        if(resp.getStatusCode() == 200) {
                                            File file = new File(localUrl.getFile());
                                            if(file.renameTo(new File(fullFilenameAtwork))) {
                                                if(currentFullFilename.equals(fullFilenameAtwork)) {
                                                    Bitmap bitmap = BitmapFactory.decodeFile(new File(fullFilenameAtwork).getAbsolutePath());
                                                    Song song = new Song(result.getTrackName(), result.getArtistName(), bitmap, result);

                                                    song.setDownloaded(songDownloaded(song));

                                                    songList.add(song);
                                                }
                                            }
                                        }
                                    }
                                }).resume();
                            }
                        }
                    }
                }
            }
        });

        return songList;
    }

    public boolean songDownloaded(Song song) {
        String previewUrl = song.getResult().getPreviewUrl();
        String filename = song.getResult().getLocalPreviewFilename();
        String fullFilename = getApplicationContext().getCacheDir() + "/" + filename;


        currentFullFilename = fullFilename;

        if (new File(fullFilename).exists()) {
            //Ya fue descargado y se encuentra en cache
            return true;
        }

        return false;

    }

    public Bitmap dataToImage(Data data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data.toBytes(), 0, data.length());
        return bitmap;
    }

    public void stopTrack() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void playTrack(String fullFilename) {
        stopTrack();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(fullFilename));
        mediaPlayer.start();
    }

    @Override
    public void onDownloadButtonClicked(int position) {
        // Actualizar el estado de reproducción de la canción en la posición indicada
        String previewUrl = songList.get(position).getResult().getPreviewUrl();
        String filename = songList.get(position).getResult().getLocalPreviewFilename();
        String fullFilename = getApplicationContext().getCacheDir() + "/" + filename;

        currentFullFilename = fullFilename;
        stopTrack();

            URL url = null;

            try {
                url = new URL(previewUrl);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            URLSession session = new URLSession();
            session.downloadTask(url, (localUrl, response1, error) -> {
                HTTPURLResponse resp = (HTTPURLResponse) response1;

                if (error == null) {
                    if (resp.getStatusCode() == 200) {
                        File file = new File(localUrl.getFile());
                        if (file.renameTo(new File(fullFilename))) {
                            if (currentFullFilename.equals(fullFilename)) {
                                playTrack(fullFilename);
                            }
                        }
                    }
                }
            }).resume();

        songList.get(position).setPlaying(true);
        // Notificar los cambios al adaptador
        songAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayButtonClicked(int position) {
        // Actualizar el estado de reproducción de la canción en la posición indicada
        String previewUrl = songList.get(position).getResult().getPreviewUrl();
        String filename = songList.get(position).getResult().getLocalPreviewFilename();
        String fullFilename = getApplicationContext().getCacheDir() + "/" + filename;


        currentFullFilename = fullFilename;
        stopTrack();
        songList.get(position).setPlaying(true);
        playTrack(fullFilename);
        // Notificar los cambios al adaptador
        songAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPauseButtonClicked(int position) {
        // Actualizar el estado de reproducción de la canción en la posición indicada
        songList.get(position).setPlaying(true);
        stopTrack();
        // Notificar los cambios al adaptador
        songAdapter.notifyDataSetChanged();
    }


}
