package com.sail.sailright2new;

import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;

public class Sounds extends AppCompatActivity {

    public MediaPlayer mediaPlayer;

    public void playSounds(String sound) {
            if (sound == "air_horn") {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.air_horn);
                mediaPlayer.start();
            }

            if (sound == "shotgun"){
                final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.shotgun);
                mediaPlayer.start();
            }

            if (sound == "klaxon"){
                final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.klaxon);
                mediaPlayer.start();
            }
    }


}
