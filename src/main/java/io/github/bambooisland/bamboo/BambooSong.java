package io.github.bambooisland.bamboo;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import javafx.scene.media.Media;

public class BambooSong implements Comparable<BambooSong> {
    private String artist;
    private String album;
    private String title;
    private int track;
    private String path;

    public BambooSong(String artist, String album, String title, String track, String path) {
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.track = (int) (Double.parseDouble(track));
        this.path = path;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int compareTo(BambooSong target) {
        return Integer.compare(this.getTrack(), target.getTrack());
    }

    public Media getMedia() {
        if (path.startsWith("http")) {
            try {
                int index = path.indexOf("://");
                String ok = path.substring(0, index + 3);
                String cache = path.substring(index + 3);
                index = cache.indexOf('/');
                ok += cache.substring(0, index);
                cache = cache.substring(index);
                return new Media(ok + URLEncoder.encode(cache, "UTF-8").replace("%2F", "/").replace("+", "%20"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            try {
                return new Media(new File(path).toURI().toURL().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getTitle() {
        return title;
    }

    public int getTrack() {
        return track;
    }

    public String getPath() {
        return path;
    }
}
