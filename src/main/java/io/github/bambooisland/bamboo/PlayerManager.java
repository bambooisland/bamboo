package io.github.bambooisland.bamboo;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class PlayerManager {
    private static MediaPlayer currentPlayer;
    private static ObjectProperty<ObservableList<BambooSong>> playList = new SimpleObjectProperty<>();
    private static ObjectProperty<BambooSong> currentSong = new SimpleObjectProperty<>();
    private static StringProperty currentSongArtist = new SimpleStringProperty();
    private static StringProperty currentSongAlbum = new SimpleStringProperty();
    private static StringProperty currentSongTitle = new SimpleStringProperty();
    private static IntegerProperty currentSongTrack = new SimpleIntegerProperty();
    private static RepeatStatus repeatStatus = RepeatStatus.LIST;

    private static DoubleProperty startTime = new SimpleDoubleProperty();
    private static DoubleProperty stopTime = new SimpleDoubleProperty();
    private static ObjectProperty<Duration> totalTime = new SimpleObjectProperty<>();
    private static ObjectProperty<Duration> currentTime = new SimpleObjectProperty<>();

    private static DoubleProperty rate = new SimpleDoubleProperty(1.0);
    private static DoubleProperty balance = new SimpleDoubleProperty(0.0);
    private static DoubleProperty volume = new SimpleDoubleProperty(0.5);

    public static void init() {
        startTime.set(0);
        stopTime.set(1);
        playList.set(FXCollections.observableArrayList());
        currentSong.addListener((value, oldValue, newValue) -> {
            currentSongArtist.set(newValue.getArtist());
            currentSongAlbum.set(newValue.getAlbum());
            currentSongTitle.set(newValue.getTitle());
            currentSongTrack.set(newValue.getTrack());
            UIController.scrollPlayListView(newValue);
        });
        UIController.seekSliderValueProperty().addListener((value, oldValue, newValue) -> {
            if (UIController.seekSliderIsChanging()) {
                Duration seekTime = totalTime.get().multiply((Double) newValue);
                currentPlayer.seek(seekTime);
                UIController.setNowTimeLabelText(format(seekTime));
            }
        });
        currentTime.addListener((value, oldValue, newValue) -> {
            updateCurrentTime(newValue);
        });
        startTime.addListener((value, oldValue, newValue) -> {
            UIController.setStartTimeLabelText(format(totalTime.get().multiply(newValue.doubleValue())));
        });
        stopTime.addListener((value, oldValue, newValue) -> {
            UIController.setStopTimeLabelText(format(totalTime.get().multiply(newValue.doubleValue())));
        });
        totalTime.addListener((value, oldValue, newValue) -> {
            UIController.setTotalTimeLabelText(format(newValue));
            UIController.setStartTimeLabelText(format(newValue.multiply(UIController.getRangeSliderLowValue())));
            UIController.setStopTimeLabelText(format(newValue.multiply(UIController.getRangeSliderHighValue())));
        });
    }

    public static void updateCurrentTime(Duration duration) {
        UIController.setSeekSliderValue(duration.toMillis() / totalTime.get().toMillis());
        UIController.setNowTimeLabelText(format(duration));
    }

    public enum RepeatStatus {
        LIST, SHUFFLE, ONE,
    }

    public static ObjectProperty<BambooSong> currentSongProperty() {
        return currentSong;
    }

    public static StringProperty currentSongArtistProperty() {
        return currentSongArtist;
    }

    public static StringProperty currentSongAlbumProperty() {
        return currentSongAlbum;
    }

    public static StringProperty currentSongTitleProperty() {
        return currentSongTitle;
    }

    public static IntegerProperty currentSongTrackProperty() {
        return currentSongTrack;
    }

    public static ObjectProperty<ObservableList<BambooSong>> playListProperty() {
        return playList;
    }

    public static DoubleProperty rateProperty() {
        return rate;
    }

    public static DoubleProperty balanceProperty() {
        return balance;
    }

    public static DoubleProperty volumeProperty() {
        return volume;
    }

    public static void setStartTime(double d) {
        if (totalTime.get() == null) {
            return;
        }
        startTime.set(d);
    }

    public static void setStopTime(double d) {
        if (totalTime.get() == null) {
            return;
        }
        stopTime.set(d);
    }

    public static MediaPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    public static String format(Duration duration) {
        int minutes = (int) (duration.toMinutes());
        int seconds = (int) (duration.toSeconds()) - minutes * 60;
        if (seconds >= 10) {
            return minutes + ":" + seconds;
        } else {
            return minutes + ":0" + seconds;
        }
    }

    private static void getNewPlayer(BambooSong song, boolean playing) {
        if (currentPlayer != null) {
            currentPlayer.dispose();
        }
        currentPlayer = new MediaPlayer(song.getMedia());
        currentSong.set(song);
        currentPlayer.setOnEndOfMedia(() -> nextSong(true));

        currentPlayer.currentTimeProperty().addListener((value, oldValue, newValue) -> {
            if (!UIController.seekSliderIsChanging()) {
                currentTime.set(newValue);
            }
            if (newValue.greaterThan(totalTime.get().multiply(stopTime.get()))
                    && currentPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                currentPlayer.setVolume(currentPlayer.getVolume() - 0.02);
                if (currentPlayer.getVolume() <= 0) {
                    nextSong(true);
                }
            }
        });
        currentPlayer.setOnReady(() -> {
            totalTime.set(currentPlayer.getTotalDuration());
            currentPlayer.seek(totalTime.get().multiply(startTime.get()));
            updateCurrentTime(totalTime.get().multiply(startTime.get()));
            currentPlayer.rateProperty().bind(rate);
            currentPlayer.balanceProperty().bind(balance);
            currentPlayer.volumeProperty().bind(volume);
            if (playing) {
                currentPlayer.play();
            }
            System.gc();
        });
    }

    public static void nextSong(boolean playing) {
        if (getCurrentPlayer() == null) {
            return;
        }
        BambooSong song = null;
        switch (repeatStatus) {
        case ONE:
            song = currentSong.get();
            break;
        case LIST:
            int now = playList.get().indexOf(currentSong.get());
            if (now + 1 == playList.get().size()) {
                song = playList.get().get(0);
            } else {
                song = playList.get().get(now + 1);
            }
            break;
        case SHUFFLE:
            song = playList.get().get((int) (Math.random() * playList.get().size()));
            break;
        }
        getNewPlayer(song, playing);
    }

    public static void backSong(boolean playing) {
        if (getCurrentPlayer() == null) {
            return;
        }
        BambooSong song = null;
        switch (repeatStatus) {
        case ONE:
            song = currentSong.get();
            break;
        case LIST:
            int now = playList.get().indexOf(currentSong.get());
            if (now != 0) {
                song = playList.get().get(now - 1);
            } else {
                song = playList.get().get(playList.get().size() - 1);
            }
            break;
        case SHUFFLE:
            song = playList.get().get((int) (Math.random() * playList.get().size()));
            break;
        }
        getNewPlayer(song, playing);
    }

    public static void setPlaylist(ListView<BambooSong> list) {
        BambooSong song = list.getSelectionModel().getSelectedItem();
        if (song == null) {
            return;
        }
        if (!list.equals(UIController.getPlayListView())) {
            playList.get().clear();
            playList.get().addAll(FXCollections.observableArrayList(list.getItems()));
        }
        getNewPlayer(song, true);
    }

    public static RepeatStatus getRepeatStatus() {
        return repeatStatus;
    }

    public static void setRepeatStatus(RepeatStatus status) {
        repeatStatus = status;
    }
}
