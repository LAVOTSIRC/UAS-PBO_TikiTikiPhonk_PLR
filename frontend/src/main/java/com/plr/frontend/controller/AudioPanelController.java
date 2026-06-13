package com.plr.frontend.controller;

import com.plr.frontend.model.AudioTrack;
import com.plr.frontend.model.NoiseType;
import com.plr.frontend.model.Playlist;
import com.plr.frontend.service.AudioPlayerService;
import com.plr.frontend.util.ApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioPanelController {

    @FXML private Button noiseTabBtn;
    @FXML private Button playlistTabBtn;

    @FXML private VBox noiseContent;
    @FXML private VBox playlistContent;

    @FXML private Button whiteNoiseBtn;
    @FXML private Button brownNoiseBtn;
    @FXML private Button rainNoiseBtn;
    @FXML private Button forestNoiseBtn;
    @FXML private Button tikiNoiseBtn;
    @FXML private Button cricketNoiseBtn;

    @FXML private ListView<Playlist>   playlistView;
    @FXML private ListView<AudioTrack> trackView;
    @FXML private VBox                 emptyPlaylistLabel;
    @FXML private HBox                 backNavBar;
    @FXML private Label                trackListTitle;
    @FXML private Label                playlistHeader;
    @FXML private Button               createPlaylistBtn;
    @FXML private HBox                 playButtons;
    @FXML private Button               addTracksBtn;

    @FXML private VBox       createForm;
    @FXML private TextField  formNameField;
    @FXML private TextArea   formDescField;
    @FXML private ListView<String> formFileList;

    @FXML private Slider volumeSlider;
    @FXML private Label  volumeLabel;

    @FXML private Label       playStateIcon;
    @FXML private Label       nowPlayingLabel;
    @FXML private Label       currentTimeLabel;
    @FXML private Label       totalDurationLabel;
    @FXML private ProgressBar audioProgressBar;
    @FXML private Button      prevBtn;
    @FXML private Button      playPauseBtn;
    @FXML private Button      nextBtn;
    @FXML private Button      stopBtn;
    @FXML private Button      loopBtn;
    @FXML private Button      shuffleBtn;

    private final AudioPlayerService audioService = new AudioPlayerService();

    private Playlist viewingPlaylist = null;
    private final List<File> pendingFiles = new ArrayList<>();

    @FXML
    public void initialize() {
        setupServiceCallbacks();
        setupVolumeSlider();
        setupPlaylistView();
        setupTrackView();
        setupModeButtons();
        resetPlayerBar();
        showPlaylistList();
        loadPlaylists();
    }

    private void setupServiceCallbacks() {
        audioService.setOnProgressUpdate(data -> {
            audioProgressBar.setProgress(data[0]);
            currentTimeLabel.setText(formatTime(data[1]));
            totalDurationLabel.setText(formatTime(data[2]));
        });

        audioService.setOnTrackChanged(name -> {
            nowPlayingLabel.setText(name);
        });

        audioService.setOnPlayStateChanged(playing -> {
            playPauseBtn.setText(playing ? "\u23F8" : "\u25B6");
            playPauseBtn.setDisable(false);
        });

        audioService.setOnTrackEnded(() -> {
            audioService.nextTrack();
        });
    }

    private void setupVolumeSlider() {
        volumeSlider.setValue(audioService.getCurrentVolume());
        updateVolumeLabel(audioService.getCurrentVolume());

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue();
            audioService.setVolume(vol);
            updateVolumeLabel(vol);
        });
    }

    private void setupPlaylistView() {
        playlistView.setItems(audioService.getPlaylists());

        playlistView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                Playlist pl = playlistView.getSelectionModel().getSelectedItem();
                if (pl != null) {
                    showPlaylistTracks(pl);
                }
            }
        });

        playlistView.setCellFactory(lv -> {
            ListCell<Playlist> cell = new ListCell<>() {
                @Override
                protected void updateItem(Playlist pl, boolean empty) {
                    super.updateItem(pl, empty);
                    if (empty || pl == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                    } else {
                        Label iconLabel = new Label("\uD83C\uDFB6");
                        iconLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #C084FC; -fx-min-width: 28; -fx-alignment: center-left;");
                        Label nameLabel = new Label(pl.getName());
                        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #F0EAFF; -fx-font-weight: bold;");
                        Label countLabel = new Label(pl.getTrackCount() + " lagu");
                        countLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #C084FC; -fx-font-weight: bold;");
                        Label descLabel = new Label(pl.getDescription());
                        descLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #7C6E8A;");
                        descLabel.setVisible(!pl.getDescription().isEmpty());
                        descLabel.setManaged(!pl.getDescription().isEmpty());
                        VBox textBox = new VBox(1, nameLabel, countLabel, descLabel);
                        javafx.scene.layout.HBox root = new javafx.scene.layout.HBox(8, iconLabel, textBox);
                        root.setStyle("-fx-padding: 8 10; -fx-background-color: #140F1C; -fx-background-radius: 8; -fx-border-color: #2D2936; -fx-border-radius: 8; -fx-border-width: 1;");
                        VBox wrapper = new VBox(root);
                        wrapper.setStyle("-fx-padding: 3 0;");
                        setGraphic(wrapper);
                        setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                    }
                }
            };
            return cell;
        });

        ContextMenu playlistContextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Hapus Playlist");
        deleteItem.setOnAction(e -> {
            Playlist pl = playlistView.getSelectionModel().getSelectedItem();
            if (pl != null && pl.getBackendId() != null) {
                Task<Void> deleteTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        ApiClient.getInstance().deletePlaylist(pl.getBackendId());
                        return null;
                    }
                };
                deleteTask.setOnSucceeded(ev -> Platform.runLater(this::loadPlaylists));
                new Thread(deleteTask).start();
            }
        });
        playlistContextMenu.getItems().add(deleteItem);
        playlistView.setContextMenu(playlistContextMenu);

        updateEmptyPlaylistVisibility();
    }

    private void setupTrackView() {
        trackView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                Object target = event.getTarget();
                boolean cellClicked = false;
                if (target instanceof javafx.scene.Node) {
                    javafx.scene.Node n = (javafx.scene.Node) target;
                    while (n != null) {
                        if (n instanceof ListCell) { cellClicked = true; break; }
                        n = n.getParent();
                    }
                }
                if (!cellClicked) return;
                AudioTrack track = trackView.getSelectionModel().getSelectedItem();
                if (track != null && viewingPlaylist != null) {
                    int idx = viewingPlaylist.getTracks().indexOf(track);
                    if (idx >= 0) {
                        clearNoiseActiveState();
                        audioService.playPlaylist(viewingPlaylist);
                        audioService.playTrackAt(idx);
                        playPauseBtn.setDisable(false);
                    }
                }
            }
        });

        trackView.setCellFactory(lv -> {
            ListCell<AudioTrack> cell = new ListCell<>() {
                @Override
                protected void updateItem(AudioTrack track, boolean empty) {
                    super.updateItem(track, empty);
                    if (empty || track == null) {
                        setText(null);
                    } else {
                        setText(track.getDisplayName());
                    }
                }
            };
            return cell;
        });
    }

    // ── View Switching ────────────────────────────────────

    private void showPlaylistList() {
        viewingPlaylist = null;
        hideAllStackContent();
        playlistView.setVisible(true);
        playlistView.setManaged(true);
        createPlaylistBtn.setVisible(true);
        createPlaylistBtn.setManaged(true);
        playButtons.setVisible(false);
        playButtons.setManaged(false);
        addTracksBtn.setVisible(false);
        addTracksBtn.setManaged(false);
        backNavBar.setVisible(false);
        backNavBar.setManaged(false);
        playlistHeader.setText("DAFTAR PLAYLIST");
        updateEmptyPlaylistVisibility();
        if (audioService.getPlaylists().isEmpty()) {
            emptyPlaylistLabel.toFront();
        }
    }

    private void showPlaylistTracks(Playlist pl) {
        viewingPlaylist = pl;
        hideAllStackContent();
        trackView.setItems(pl.getTracks());
        trackView.setVisible(true);
        trackView.setManaged(true);
        trackView.getSelectionModel().clearSelection();
        createPlaylistBtn.setVisible(false);
        createPlaylistBtn.setManaged(false);
        playButtons.setVisible(true);
        playButtons.setManaged(true);
        addTracksBtn.setVisible(true);
        addTracksBtn.setManaged(true);
        backNavBar.setVisible(true);
        backNavBar.setManaged(true);
        trackListTitle.setText(pl.getName());
        playlistHeader.setText("DAFTAR LAGU");
        emptyPlaylistLabel.setVisible(false);
        emptyPlaylistLabel.setManaged(false);
    }

    private void showCreateForm() {
        hideAllStackContent();
        createForm.setVisible(true);
        createForm.setManaged(true);
        createPlaylistBtn.setVisible(false);
        createPlaylistBtn.setManaged(false);
        playButtons.setVisible(false);
        playButtons.setManaged(false);
        addTracksBtn.setVisible(false);
        addTracksBtn.setManaged(false);
        backNavBar.setVisible(true);
        backNavBar.setManaged(true);
        trackListTitle.setText("Buat Playlist Baru");
        playlistHeader.setText("");
        emptyPlaylistLabel.setVisible(false);
        emptyPlaylistLabel.setManaged(false);
        formNameField.clear();
        formDescField.clear();
        pendingFiles.clear();
        formFileList.setItems(FXCollections.observableArrayList());
    }

    private void hideAllStackContent() {
        playlistView.setVisible(false); playlistView.setManaged(false);
        trackView.setVisible(false);    trackView.setManaged(false);
        createForm.setVisible(false);   createForm.setManaged(false);
        emptyPlaylistLabel.setVisible(false); emptyPlaylistLabel.setManaged(false);
    }

    @FXML
    public void handleBackToPlaylists() {
        if (createForm.isVisible()) {
            showPlaylistList();
        } else {
            showPlaylistList();
        }
    }

    private void loadPlaylists() {
        Task<List<Map<String, Object>>> fetchTask = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return ApiClient.getInstance().getPlaylists();
            }
        };

        fetchTask.setOnSucceeded(e -> {
            List<Map<String, Object>> data = fetchTask.getValue();
            ObservableList<Playlist> playlists = audioService.getPlaylists();
            playlists.clear();

            for (Map<String, Object> plMap : data) {
                Long id = ((Number) plMap.get("id")).longValue();
                String name = (String) plMap.get("name");
                String desc = (String) plMap.get("description");
                if (desc == null) desc = "";

                Playlist pl = new Playlist(name, desc);
                pl.setBackendId(id);

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> songsData = (List<Map<String, Object>>) plMap.get("songs");
                if (songsData != null) {
                    for (Map<String, Object> songMap : songsData) {
                        String filePath = (String) songMap.get("filePath");
                        if (filePath != null && !filePath.isEmpty()) {
                            File file = new File(filePath);
                            if (file.exists()) {
                                pl.addTrack(new AudioTrack(file));
                            }
                        }
                    }
                }
                playlists.add(pl);
            }

            Platform.runLater(() -> {
                showPlaylistList();
                updateEmptyPlaylistVisibility();
            });
        });

        fetchTask.setOnFailed(e -> {
            // Backend tidak tersedia — tetap pakai data lokal (jika ada)
            Platform.runLater(this::updateEmptyPlaylistVisibility);
        });

        new Thread(fetchTask).start();
    }

    // ── Form Handlers ──────────────────────────────────────

    @FXML
    public void handleCreatePlaylist() {
        showCreateForm();
    }

    @FXML
    public void handlePickFormFiles() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih File Audio");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Audio Files (MP3, WAV)", "*.mp3", "*.wav"),
            new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"),
            new FileChooser.ExtensionFilter("WAV Files", "*.wav")
        );
        Stage stage = (Stage) playlistView.getScene().getWindow();
        List<File> files = fc.showOpenMultipleDialog(stage);
        if (files != null) {
            pendingFiles.addAll(files);
            ObservableList<String> names = FXCollections.observableArrayList();
            for (File f : pendingFiles) {
                names.add(f.getName());
            }
            formFileList.setItems(names);
        }
    }

    @FXML
    public void handleSavePlaylist() {
        String name = formNameField.getText().trim();
        if (name.isEmpty()) {
            formNameField.getStyleClass().add("audio-form-field-error");
            return;
        }
        formNameField.getStyleClass().remove("audio-form-field-error");
        String desc = formDescField.getText().trim();

        Task<Map<String, Object>> saveTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                Map<String, Object> plData = new HashMap<>();
                plData.put("name", name);
                plData.put("description", desc);
                Map<String, Object> result = ApiClient.getInstance().createPlaylist(plData);
                Long playlistId = ((Number) result.get("id")).longValue();

                for (File f : pendingFiles) {
                    String title = f.getName();
                    int dot = title.lastIndexOf('.');
                    if (dot > 0) title = title.substring(0, dot);

                    Map<String, Object> songData = new HashMap<>();
                    songData.put("title", title);
                    songData.put("filePath", f.getAbsolutePath());
                    songData.put("fileSize", f.length());
                    ApiClient.getInstance().addSong(playlistId, songData);
                }
                return result;
            }
        };

        String finalName = name;
        saveTask.setOnSucceeded(e -> Platform.runLater(() -> {
            loadPlaylists();
            for (Playlist p : audioService.getPlaylists()) {
                if (p.getName().equals(finalName) && p.getBackendId() != null) {
                    showPlaylistTracks(p);
                    return;
                }
            }
            showPlaylistList();
        }));

        saveTask.setOnFailed(e -> Platform.runLater(() -> {
            formNameField.getStyleClass().add("audio-form-field-error");
        }));

        new Thread(saveTask).start();
    }

    @FXML
    public void handleCancelCreate() {
        showPlaylistList();
    }

    // ── Player Controls ────────────────────────────────────

    private void resetPlayerBar() {
        nowPlayingLabel.setText("Tidak ada audio");
        currentTimeLabel.setText("0:00");
        totalDurationLabel.setText("0:00");
        audioProgressBar.setProgress(0);
        playPauseBtn.setDisable(true);
        playPauseBtn.setText("\u25B6");
        if (playStateIcon != null) playStateIcon.setText("\u25B6");
    }

    private void setupModeButtons() {
        audioService.setOnModeChanged(() -> updateModeButtons());
        updateModeButtons();
    }

    private void updateModeButtons() {
        switch (audioService.getLoopMode()) {
            case NONE -> {
                loopBtn.setText("LP");
                loopBtn.getStyleClass().remove("active");
            }
            case ALL -> {
                loopBtn.setText("LP");
                if (!loopBtn.getStyleClass().contains("active"))
                    loopBtn.getStyleClass().add("active");
            }
            case ONE -> {
                loopBtn.setText("L1");
                if (!loopBtn.getStyleClass().contains("active"))
                    loopBtn.getStyleClass().add("active");
            }
        }
        if (audioService.isShuffle()) {
            if (!shuffleBtn.getStyleClass().contains("active"))
                shuffleBtn.getStyleClass().add("active");
        } else {
            shuffleBtn.getStyleClass().remove("active");
        }
    }

    @FXML
    public void showNoiseTab() {
        setActiveTab(noiseTabBtn, playlistTabBtn, noiseContent, playlistContent);
    }

    @FXML
    public void showPlaylistTab() {
        setActiveTab(playlistTabBtn, noiseTabBtn, playlistContent, noiseContent);
    }

    private void setActiveTab(Button activeBtn, Button inactiveBtn,
                              VBox activeContent, VBox inactiveContent) {
        activeBtn.getStyleClass().add("active");
        inactiveBtn.getStyleClass().remove("active");
        activeContent.setVisible(true);
        activeContent.setManaged(true);
        inactiveContent.setVisible(false);
        inactiveContent.setManaged(false);
    }

    @FXML
    public void playWhiteNoise() {
        handleNoiseButton(whiteNoiseBtn, NoiseType.WHITE_NOISE);
    }

    @FXML
    public void playBrownNoise() {
        handleNoiseButton(brownNoiseBtn, NoiseType.BROWN_NOISE);
    }

    @FXML
    public void playRainNoise() {
        handleNoiseButton(rainNoiseBtn, NoiseType.RAIN);
    }

    @FXML
    public void playForestNoise() {
        handleNoiseButton(forestNoiseBtn, NoiseType.FOREST);
    }

    @FXML
    public void playTikiNoise() {
        handleNoiseButton(tikiNoiseBtn, NoiseType.TIKI);
    }

    @FXML
    public void playCricketNoise() {
        handleNoiseButton(cricketNoiseBtn, NoiseType.CRICKET);
    }

    private void handleNoiseButton(Button clickedBtn, NoiseType type) {
        setNoiseActiveState(clickedBtn);
        clearPlaylistSelection();
        audioService.playNoise(type);
        playPauseBtn.setDisable(false);
    }

    @FXML
    public void handleSequentialPlay() {
        if (viewingPlaylist == null || viewingPlaylist.getTracks().isEmpty()) return;
        clearNoiseActiveState();
        audioService.playPlaylist(viewingPlaylist);
        playPauseBtn.setDisable(false);
    }

    @FXML
    public void handleShufflePlay() {
        if (viewingPlaylist == null || viewingPlaylist.getTracks().isEmpty()) return;
        clearNoiseActiveState();
        audioService.playPlaylistShuffled(viewingPlaylist);
        playPauseBtn.setDisable(false);
    }

    @FXML
    public void handleAddTracks() {
        if (viewingPlaylist == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Tambah File Audio ke \"" + viewingPlaylist.getName() + "\"");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Audio Files (MP3, WAV)", "*.mp3", "*.wav"),
            new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"),
            new FileChooser.ExtensionFilter("WAV Files", "*.wav")
        );

        Stage stage = (Stage) playlistView.getScene().getWindow();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            Long playlistId = viewingPlaylist.getBackendId();

            Task<Void> addTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    for (File file : selectedFiles) {
                        String title = file.getName();
                        int dot = title.lastIndexOf('.');
                        if (dot > 0) title = title.substring(0, dot);

                        Map<String, Object> songData = new HashMap<>();
                        songData.put("title", title);
                        songData.put("filePath", file.getAbsolutePath());
                        songData.put("fileSize", file.length());
                        ApiClient.getInstance().addSong(playlistId, songData);
                    }
                    return null;
                }
            };

            addTask.setOnSucceeded(e -> Platform.runLater(() -> {
                loadPlaylists();
                showPlaylistList();
            }));

            new Thread(addTask).start();
        }
    }

    @FXML
    public void handlePlayPause() {
        audioService.togglePlayPause();
    }

    @FXML
    public void handleStop() {
        audioService.stop();
        clearNoiseActiveState();
        clearPlaylistSelection();
        playPauseBtn.setDisable(true);
        playPauseBtn.setText("\u25B6");
    }

    @FXML
    public void handleNext() {
        if (audioService.isNoiseMode() || audioService.getPlaylist().isEmpty()) return;
        audioService.nextTrack();
    }

    @FXML
    public void handlePrevious() {
        if (audioService.isNoiseMode() || audioService.getPlaylist().isEmpty()) return;
        audioService.previousTrack();
    }

    @FXML
    public void handleToggleLoop() {
        audioService.toggleLoopMode();
    }

    @FXML
    public void handleToggleShuffle() {
        audioService.toggleShuffle();
    }

    public void shutdown() {
        audioService.dispose();
    }

    // ── Helpers ────────────────────────────────────────────

    private void setNoiseActiveState(Button activeBtn) {
        Button[] noiseBtns = { whiteNoiseBtn, brownNoiseBtn, rainNoiseBtn, forestNoiseBtn, tikiNoiseBtn, cricketNoiseBtn };
        for (Button btn : noiseBtns) {
            if (btn != null) btn.getStyleClass().remove("noise-btn-active");
        }
        if (activeBtn != null && !activeBtn.getStyleClass().contains("noise-btn-active")) {
            activeBtn.getStyleClass().add("noise-btn-active");
        }
    }

    private void clearNoiseActiveState() {
        setNoiseActiveState(null);
    }

    private void clearPlaylistSelection() {
        playlistView.getSelectionModel().clearSelection();
        trackView.getSelectionModel().clearSelection();
    }

    private void updateEmptyPlaylistVisibility() {
        boolean empty = audioService.getPlaylists().isEmpty();
        emptyPlaylistLabel.setVisible(empty);
        emptyPlaylistLabel.setManaged(empty);
    }

    private String formatTime(double totalSeconds) {
        if (totalSeconds < 0 || Double.isNaN(totalSeconds)) return "0:00";
        int minutes = (int) totalSeconds / 60;
        int seconds = (int) totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void updateVolumeLabel(double volume) {
        volumeLabel.setText((int) (volume * 100) + "%");
    }
}
