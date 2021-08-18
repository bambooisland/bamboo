package io.github.bambooisland.bamboo;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.controlsfx.control.RangeSlider;

import io.github.bambooisland.bamboo.PlayerManager.RepeatStatus;
import io.github.bambooisland.pine.base.Element;
import io.github.bambooisland.pine.base.Table;
import io.github.bambooisland.pine.base.VoidOperationException;
import io.github.bambooisland.pine.base.values.StringValue;
import io.github.bambooisland.pine.poi.PoiTableBuilder;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import javafx.util.Callback;
import javafx.util.Duration;

public class UIController {
    private Table table;
    private BambooSong[] songs;

    @FXML
    public void initialize() {
        new Thread(() -> {
            try {
                table = PoiTableBuilder.getTable(new File("data/list.xlsx"), 0);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            songs = new BambooSong[table.getNumberOfElements()];
            for (int i = 0; i < table.getNumberOfElements(); i++) {
                Element ele = table.getElement(i);
                songs[i] = new BambooSong(ele.getField(0).getStringValue(), ele.getField(1).getStringValue(),
                        ele.getField(2).getStringValue(), ele.getField(3).getStringValue(),
                        ele.getField(4).getStringValue());
            }

            ObservableList<String> list_tab1 = FXCollections.observableArrayList();
            for (String s : table.getAllKindsOfValues(0)) {
                list_tab1.add(s);
            }
            Collections.sort(list_tab1, (str1, str2) -> {
                String cache1 = str1;
                String cache2 = str2;
                if (str1.toLowerCase().startsWith("the ")) {
                    cache1 = str1.substring(4);
                }
                if (str2.toLowerCase().startsWith("the ")) {
                    cache2 = str2.substring(4);
                }
                return Collator.getInstance().compare(cache1, cache2);
            });
            ObservableList<String> list_tab2 = FXCollections.observableArrayList();
            for (String s : table.getAllKindsOfValues(1)) {
                list_tab2.add(s);
            }
            Collections.sort(list_tab2);
            ObservableList<BambooSong> list_tab4 = FXCollections.observableArrayList();
            for (BambooSong song : songs) {
                list_tab4.add(song);
            }
            Collections.sort(list_tab4, (song1, song2) -> {
                return Collator.getInstance().compare(song1.getTitle(), song2.getTitle());
            });

            Platform.runLater(() -> {
                this.artistList_tab1.setItems(list_tab1);
                this.artistList_tab3.setItems(list_tab1);
                this.albumList_tab2.setItems(list_tab2);
                this.songList_tab4.setItems(list_tab4);

                titleLabel.textProperty().bind(PlayerManager.currentSongTitleProperty());
                Tooltip tooltip = new Tooltip();
                tooltip.textProperty().bind(titleLabel.textProperty());
                titleLabel.setTooltip(tooltip);

                artistLabel.textProperty().bind(PlayerManager.currentSongArtistProperty());
                tooltip = new Tooltip();
                tooltip.textProperty().bind(artistLabel.textProperty());
                artistLabel.setTooltip(tooltip);

                albumLabel.textProperty().bind(PlayerManager.currentSongAlbumProperty());
                tooltip = new Tooltip();
                tooltip.textProperty().bind(albumLabel.textProperty());
                albumLabel.setTooltip(tooltip);

                rangeSlider.lowValueProperty().addListener((value, oldValue, newValue) -> {
                    PlayerManager.setStartTime(newValue.doubleValue());
                });
                rangeSlider.highValueProperty().addListener((value, oldValue, newValue) -> {
                    PlayerManager.setStopTime(newValue.doubleValue());
                });

                playListView.itemsProperty().bind(PlayerManager.playListProperty());

                rateSlider.valueProperty().addListener((value, oldValue, newValue) -> {
                    double d = newValue.doubleValue();
                    PlayerManager.rateProperty().setValue(d * d);
                });

                PlayerManager.balanceProperty().bind(balanceSlider.valueProperty());
                PlayerManager.volumeProperty().bind(volumeSlider.valueProperty());

                numberOfMusicLabel.setText(table.getAllKindsOfValues(0).length + "人のアーティスト "
                        + table.getAllKindsOfValues(1).length + "枚のアルバム 全" + table.getNumberOfElements() + "曲");

                Callback<ListView<BambooSong>, ListCell<BambooSong>> callback = list -> {
                    return new ListCell<>() {
                        @Override
                        protected void updateItem(BambooSong item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                setText(item.toString());
                                setTooltip(new Tooltip(item.getArtist() + "\n\t- " + item.getAlbum() + " ( track"
                                        + item.getTrack() + " )"));
                            }
                        }
                    };
                };
                songList_tab1.setCellFactory(callback);
                songList_tab2.setCellFactory(callback);
                songList_tab3.setCellFactory(callback);
                songList_tab4.setCellFactory(callback);
                playListView.setCellFactory(callback);
            });
        }).start();
    }

    @FXML
    private Tab tab1;

    @FXML
    private Tab tab2;

    @FXML
    private Tab tab3;

    @FXML
    private Tab tab4;

    @FXML
    private ListView<String> artistList_tab1;

    @FXML
    private ListView<String> albumList_tab1;

    @FXML
    private ListView<BambooSong> songList_tab1;

    @FXML
    private ListView<String> albumList_tab2;

    @FXML
    private ListView<BambooSong> songList_tab2;

    @FXML
    private ListView<String> artistList_tab3;

    @FXML
    private ListView<BambooSong> songList_tab3;

    @FXML
    private ListView<BambooSong> songList_tab4;

    @FXML
    private ListView<BambooSong> playListView;

    @FXML
    private Slider seekSlider;

    @FXML
    private Label titleLabel;

    @FXML
    private Label artistLabel;

    @FXML
    private Label albumLabel;

    @FXML
    private Label nowTimeLabel;

    @FXML
    private Label totalTimeLabel;

    @FXML
    private RangeSlider rangeSlider;

    @FXML
    private Label startTimeLabel;

    @FXML
    private Label stopTimeLabel;

    @FXML
    private Label numberOfMusicLabel;

    @FXML
    private Button backSongButton;

    @FXML
    private Button repeatButton;

    @FXML
    private Slider rateSlider;

    @FXML
    private Slider balanceSlider;

    @FXML
    private Slider volumeSlider;

    public static ListView<BambooSong> getPlayListView() {
        return Main.con.playListView;
    }

    public static boolean seekSliderIsChanging() {
        return Main.con.seekSlider.isValueChanging() || Main.con.seekSlider.isPressed();
    }

    public static void setSeekSliderValue(double value) {
        Main.con.seekSlider.setValue(value);
    }

    public static DoubleProperty seekSliderValueProperty() {
        return Main.con.seekSlider.valueProperty();
    }

    public static void setNowTimeLabelText(String text) {
        Main.con.nowTimeLabel.setText(text);
    }

    public static void setTotalTimeLabelText(String text) {
        Main.con.totalTimeLabel.setText(text);
    }

    public static void setStartTimeLabelText(String text) {
        Main.con.startTimeLabel.setText(text);
    }

    public static void setStopTimeLabelText(String text) {
        Main.con.stopTimeLabel.setText(text);
    }

    public static double getRangeSliderLowValue() {
        return Main.con.rangeSlider.lowValueProperty().get();
    }

    public static double getRangeSliderHighValue() {
        return Main.con.rangeSlider.highValueProperty().get();
    }

    public static void scrollPlayListView(BambooSong song) {
        ListView<BambooSong> view = Main.con.playListView;
        view.getSelectionModel().select(song);
        List<BambooSong> list = view.getItems();
        for (ScrollBar bar : getScrollBars(view)) {
            if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                new Thread(() -> {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Platform.runLater(() -> {
                        double d = list.size() > 1 ? (double) list.indexOf(song) / (list.size() - 1) : 0;
                        bar.setValue(d);
                    });
                }).start();
            }
        }
    }

    private static List<ScrollBar> getScrollBars(ListView<?> view) {
        List<ScrollBar> list = new ArrayList<>();
        for (Node node : view.lookupAll(".scroll-bar")) {
            try {
                ScrollBar bar = (ScrollBar) node;
                list.add(bar);
            } catch (ClassCastException e) {
                continue;
            }
        }
        return list;
    }

    private static void resetScrollBars(ListView<?> view) {
        for (ScrollBar bar : getScrollBars(view)) {
            bar.setValue(0);
        }
    }

    @FXML
    private void artistPressed(KeyEvent event) {
        artistClicked(null);
    }

    @FXML
    private void artistClicked(MouseEvent event) {
        try {
            if (tab1.isSelected()) {
                String artist = artistList_tab1.getSelectionModel().getSelectedItem();
                if (artist == null) {
                    return;
                }
                ObservableList<String> list = FXCollections.observableArrayList();
                for (String album : table.cloneExtractor().where(0, StringValue.EQUAL_OPERATOR, artist)
                        .getAllKindsOfValues(1)) {
                    list.add(album);
                }
                albumList_tab1.setItems(list);
                songList_tab1.setItems(FXCollections.emptyObservableList());
                resetScrollBars(albumList_tab1);
            } else if (tab3.isSelected()) {
                String artist = artistList_tab3.getSelectionModel().getSelectedItem();
                if (artist == null) {
                    return;
                }
                ObservableList<BambooSong> list = FXCollections.observableArrayList();
                for (BambooSong song : songs) {
                    if (song.getArtist().equals(artist)) {
                        list.add(song);
                    }
                }
                songList_tab3.setItems(list);
                resetScrollBars(songList_tab3);
            }
        } catch (VoidOperationException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void albumPressed(KeyEvent event) {
        albumClicked(null);
    }

    @FXML
    private void albumClicked(MouseEvent event) {
        ListView<String> view = null;
        ListView<BambooSong> target = null;
        if (tab1.isSelected()) {
            view = albumList_tab1;
            target = songList_tab1;
        } else if (tab2.isSelected()) {
            view = albumList_tab2;
            target = songList_tab2;
        }
        String album = view.getSelectionModel().getSelectedItem();
        if (album == null) {
            return;
        }
        ObservableList<BambooSong> list = FXCollections.observableArrayList();
        for (BambooSong song : songs) {
            if (tab1.isSelected()) {
                if (!artistList_tab1.getSelectionModel().getSelectedItem().equals(song.getArtist())) {
                    continue;
                }
            }

            if (song.getAlbum().equals(album)) {
                list.add(song);
            }
        }
        Collections.sort(list);
        target.setItems(list);
        resetScrollBars(target);
        return;
    }

    private void newSong() {
        ListView<BambooSong> view = null;
        if (tab1.isSelected()) {
            view = songList_tab1;
        } else if (tab2.isSelected()) {
            view = songList_tab2;
        } else if (tab3.isSelected()) {
            view = songList_tab3;
        } else if (tab4.isSelected()) {
            view = songList_tab4;
        }
        PlayerManager.setPlaylist(view);
    }

    @FXML
    private void songPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            newSong();
        }
    }

    @FXML
    private void songClicked(MouseEvent event) {
        if (event.getClickCount() < 2) {
            return;
        }
        newSong();
    }

    @FXML
    private void playListPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            PlayerManager.setPlaylist(playListView);
        }
    }

    @FXML
    private void playListClicked(MouseEvent event) {
        if (event.getClickCount() < 2) {
            return;
        }
        PlayerManager.setPlaylist(playListView);
    }

    private void pause() {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        if (player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.pause();
        } else {
            player.play();
        }
    }

    @FXML
    private void pauseKey(ActionEvent event) {
        pause();
    }

    @FXML
    private void pauseClicked(MouseEvent event) {
        pause();
    }

    private static boolean playing;

    private static long time;

    private static boolean isContinuous() {
        long cache = time;
        return (time = System.currentTimeMillis()) - cache < 1000;
    }

    @FXML
    private void nextSongKey() {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        if (!isContinuous()) {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                playing = true;
            } else {
                playing = false;
            }
        }
        PlayerManager.nextSong(playing);
    }

    @FXML
    private void nextSongClicked(MouseEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        if (!isContinuous()) {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                playing = true;
            } else {
                playing = false;
            }
        }
        PlayerManager.nextSong(playing);
    }

    @FXML
    private void backSongKey(ActionEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        if (!isContinuous()) {
            PlayerManager.getCurrentPlayer().seek(new Duration(0));
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                playing = true;
            } else {
                playing = false;
            }
        } else {
            PlayerManager.backSong(playing);
        }
    }

    @FXML
    private void backSongClicked(MouseEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        if (event.getClickCount() >= 2) {
            if (event.getClickCount() == 2) {
                if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                    playing = true;
                } else {
                    playing = false;
                }
            }
            PlayerManager.backSong(playing);
        } else {
            PlayerManager.getCurrentPlayer().seek(new Duration(0));
        }
    }

    private void changeRepeatStatus() {
        switch (PlayerManager.getRepeatStatus()) {
        case LIST:
            PlayerManager.setRepeatStatus(RepeatStatus.SHUFFLE);
            repeatButton.setText("🔀");
            backSongButton.setVisible(false);
            break;
        case SHUFFLE:
            PlayerManager.setRepeatStatus(RepeatStatus.ONE);
            repeatButton.setText("🔂");
            break;
        case ONE:
            PlayerManager.setRepeatStatus(RepeatStatus.LIST);
            repeatButton.setText("🔁");
            backSongButton.setVisible(true);
            break;
        }
    }

    @FXML
    private void changeRepeatStatusKey(ActionEvent event) {
        changeRepeatStatus();
    }

    @FXML
    private void changeRepeatStatusClicked(MouseEvent event) {
        changeRepeatStatus();
    }

    @FXML
    private void seek0(ActionEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        player.seek(new Duration(0));
    }

    @FXML
    private void seek1(ActionEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        player.seek(player.getTotalDuration().multiply(0.1));
    }

    @FXML
    private void seek2(ActionEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        player.seek(player.getTotalDuration().multiply(0.2));
    }

    @FXML
    private void seek3(ActionEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        player.seek(player.getTotalDuration().multiply(0.3));
    }

    @FXML
    private void seek4(ActionEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        player.seek(player.getTotalDuration().multiply(0.4));
    }

    @FXML
    private void seek5(ActionEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        player.seek(player.getTotalDuration().multiply(0.5));
    }

    @FXML
    private void seek6(ActionEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        player.seek(player.getTotalDuration().multiply(0.6));
    }

    @FXML
    private void seek7(ActionEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        player.seek(player.getTotalDuration().multiply(0.7));
    }

    @FXML
    private void seek8(ActionEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        player.seek(player.getTotalDuration().multiply(0.8));
    }

    @FXML
    private void seek9(ActionEvent event) {
        MediaPlayer player;
        if ((player = PlayerManager.getCurrentPlayer()) == null) {
            return;
        }
        player.seek(player.getTotalDuration().multiply(0.9));
    }

    @FXML
    private void exit(ActionEvent event) {
        System.exit(0);
    }
}
