package sickbay.pokenamon.system.home;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class BackgroundMusicManager implements DefaultLifecycleObserver {
    private static BackgroundMusicManager instance;
    private MediaPlayer player;
    private static Context context;
    private int queue;

    BackgroundMusicManager(Context context) {
        BackgroundMusicManager.context = context;
    }

    public static BackgroundMusicManager getInstance(Context context) {
        if (instance == null) {
            instance = new BackgroundMusicManager(context);
        }

        return instance;
    }

    public void play(int resId) {
        if (queue == resId && player.isPlaying()) { return; }

        queue = resId;

        if (player != null && player.isPlaying()) {
            player.stop();
            player = null;
        }

        player = MediaPlayer.create(context, resId);

        try {
            player.reset();

            android.content.res.AssetFileDescriptor afd = context.getResources().openRawResourceFd(resId);
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            player.setLooping(true);
            player.setVolume(0.5f,0.5f);

            player.prepareAsync();
            player.setOnPreparedListener(MediaPlayer::start);
        } catch (Exception e) {
            Log.d("MusicManager", e.getMessage(), e);
        }
    }

    public void resume() {
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

    public void pause() {
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    public void stop() {
        if (player != null && player.isPlaying()) {
            player.stop();
            player = null;
        }
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        resume();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        pause();
    }
}
