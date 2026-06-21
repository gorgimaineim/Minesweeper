package org.example.demo;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AudioSystem {
    private MediaPlayer backgroundMusicPlayer;
    private AudioClip explosionSoundEffect;
    private AudioClip clickSoundEffect;

    private double currentMusicVolume = 0.5;
    private double currentSoundVolume = 0.5;

    public AudioSystem() {
        List<String> tracks = scanAudioDirectory("/audio/music/");
        if (!tracks.isEmpty()) setMusicTrack(tracks.get(0));

        List<String> clickSounds = scanAudioDirectory("/audio/clicks/");
        if (!clickSounds.isEmpty()) setClickSound(clickSounds.get(0));

        try {
            URL url = getClass().getResource("/audio/explosion.wav");
            if (url != null) explosionSoundEffect = new AudioClip(url.toString());
        } catch (Exception ignored) {}
    }

    public static List<String> scanAudioDirectory(String resourcePath) {
        List<String> names = new ArrayList<>();
        try {
            URL dirUrl = AudioSystem.class.getResource(resourcePath);
            if (dirUrl != null && "file".equals(dirUrl.getProtocol())) {
                File dir = new File(dirUrl.toURI());
                if (dir.isDirectory()) {
                    File[] hits = dir.listFiles((f, n) -> {
                        String lo = n.toLowerCase();
                        return lo.endsWith(".mp3") || lo.endsWith(".wav")
                                || lo.endsWith(".ogg") || lo.endsWith(".flac");
                    });
                    if (hits != null) {
                        Arrays.sort(hits);
                        for (File f : hits) names.add(f.getName());
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return names;
    }

    public void setMusicTrack(String filename) {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose();
        }
        try {
            URL url = getClass().getResource("/audio/music/" + filename);
            if (url != null) {
                backgroundMusicPlayer = new MediaPlayer(new Media(url.toString()));
                backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusicPlayer.setVolume(currentMusicVolume);
                backgroundMusicPlayer.play();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void setClickSound(String filename) {
        try {
            URL url = getClass().getResource("/audio/clicks/" + filename);
            if (url != null) clickSoundEffect = new AudioClip(url.toString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void setMusicVolume(double v) {
        currentMusicVolume = v;
        if (backgroundMusicPlayer != null) backgroundMusicPlayer.setVolume(v);
    }

    public void setSoundVolume(double v) { currentSoundVolume = v; }

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