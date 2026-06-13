package org.example.demo;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class AudioSystem {
    private MediaPlayer backgroundMusicPlayer;
    private AudioClip explosionSoundEffect;
    private AudioClip clickSoundEffect;

    private double currentMusicVolume = 0.5;
    private double currentSoundVolume = 0.5;

    public AudioSystem() {
        try {
            URL musicFileUrl = getClass().getResource("/audio/music.mp3");
            if (musicFileUrl != null) {
                backgroundMusicPlayer = new MediaPlayer(new Media(musicFileUrl.toString()));
                backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusicPlayer.setVolume(currentMusicVolume);
                backgroundMusicPlayer.play();
            }
            URL explosionFileUrl = getClass().getResource("/audio/explosion.wav");
            if (explosionFileUrl != null) {
                explosionSoundEffect = new AudioClip(explosionFileUrl.toString());
            }
            URL clickFileUrl = getClass().getResource("/audio/click.wav");
            if (clickFileUrl != null) {
                clickSoundEffect = new AudioClip(clickFileUrl.toString());
            }
        } catch (Exception exception) {
        }
    }

    public void setMusicVolume(double targetVolume) {
        this.currentMusicVolume = targetVolume;
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(targetVolume);
        }
    }

    public void setSoundVolume(double targetVolume) {
        this.currentSoundVolume = targetVolume;
    }

    public void playExplosionSound() {
        if (explosionSoundEffect != null) {
            explosionSoundEffect.setVolume(currentSoundVolume);
            explosionSoundEffect.play();
        }
    }

    public void playClickSound() {
        if (clickSoundEffect != null) {
            clickSoundEffect.setVolume(currentSoundVolume);
            clickSoundEffect.play();
        }
    }
}