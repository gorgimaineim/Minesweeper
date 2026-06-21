package org.example.demo;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class App extends Application {

    private Stage mainApplicationStage;
    private Scene mainMenuScene;
    private Scene optionsMenuScene;
    private Scene activeGameScene;

    private AudioSystem currentAudioSystem;

    private int currentGridColumns = 15;
    private int currentGridRows = 15;
    private int currentTotalMines = 30;
    private int availableFlagsCount = 30;
    private String currentDifficulty = "Hard";

    private Cell[][] activeGameGrid;
    private boolean isGameCurrentlyOver = false;
    private boolean isGamePaused = false;

    private int timeSecondsElapsed = 0;
    private boolean isFirstClick = true;
    private Timeline gameTimer;
    private Text timerText;

    private Text activeFlagCounterText;
    private StackPane primaryGameRootPane;
    private HBox topStatisticsBar;
    private Timeline flagFlashTimeline;

    private VBox pauseMenuOverlay;
    private Button optionsReturnButton;

    private GridPane leaderboardTable;
    private String currentLeaderboardDifficulty = "Easy";

    private boolean isDarkMode = false;
    private BorderPane mainMenuRoot;
    private VBox optionsVerticalLayout;
    private VBox leaderboardLayout;
    private Text titleDisplayText;
    private Text optionsTitleText;
    private Text leaderboardTitle;
    private Label musicControlLabel;
    private Label soundControlLabel;
    private Label themeLabel;
    private Label musicTrackLabel;
    private Label clickSoundLabel;

    private String savedMusicTrack = null;
    private String savedClickSound = null;

    @Override
    public void start(Stage applicationStage) {
        DatabaseHelper.initializeDatabase();

        this.mainApplicationStage = applicationStage;
        this.currentAudioSystem = new AudioSystem();

        isDarkMode = Boolean.parseBoolean(DatabaseHelper.loadSetting("dark_mode", "false"));
        savedMusicTrack = DatabaseHelper.loadSetting("music_track", null);
        savedClickSound = DatabaseHelper.loadSetting("click_sound", null);

        if (savedMusicTrack != null) currentAudioSystem.setMusicTrack(savedMusicTrack);
        if (savedClickSound != null) currentAudioSystem.setClickSound(savedClickSound);

        buildOptionsMenuInterface();
        buildMainMenuInterface();
        updateTheme();

        mainApplicationStage.setTitle("JavaFX Minesweeper");
        mainApplicationStage.setScene(mainMenuScene);
        mainApplicationStage.setResizable(false);
        mainApplicationStage.show();
    }

    private void buildMainMenuInterface() {
        mainMenuRoot = new BorderPane();
        mainMenuRoot.setPrefSize(800, 600);

        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(40, 0, 20, 0));
        titleDisplayText = new Text("MINESWEEPER");
        titleDisplayText.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        titleBox.getChildren().add(titleDisplayText);
        mainMenuRoot.setTop(titleBox);

        leaderboardLayout = new VBox(15);
        leaderboardLayout.setAlignment(Pos.TOP_CENTER);
        leaderboardLayout.setPrefWidth(280);
        leaderboardLayout.setPadding(new Insets(20, 20, 20, 20));

        leaderboardTitle = new Text("LEADERBOARDS");
        leaderboardTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        HBox difficultySwitcher = new HBox(10);
        difficultySwitcher.setAlignment(Pos.CENTER);
        Button easyBtn = new Button("Easy");
        Button hardBtn = new Button("Hard");
        Button extremeBtn = new Button("Extreme");
        difficultySwitcher.getChildren().addAll(easyBtn, hardBtn, extremeBtn);

        easyBtn.setOnAction(e -> { currentLeaderboardDifficulty = "Easy"; updateLeaderboardTable(); });
        hardBtn.setOnAction(e -> { currentLeaderboardDifficulty = "Hard"; updateLeaderboardTable(); });
        extremeBtn.setOnAction(e -> { currentLeaderboardDifficulty = "Extreme"; updateLeaderboardTable(); });

        leaderboardTable = new GridPane();
        leaderboardTable.setAlignment(Pos.CENTER);
        leaderboardTable.setStyle("-fx-background-color: #adb5bd; -fx-border-color: #adb5bd; -fx-border-width: 1;");
        leaderboardTable.setHgap(1);
        leaderboardTable.setVgap(1);
        updateLeaderboardTable();

        leaderboardLayout.getChildren().addAll(leaderboardTitle, difficultySwitcher, leaderboardTable);
        mainMenuRoot.setLeft(leaderboardLayout);

        VBox menuVerticalLayout = new VBox(20);
        menuVerticalLayout.setAlignment(Pos.CENTER);
        menuVerticalLayout.setPadding(new Insets(0, 50, 80, 0));

        Button selectEasyButton = new Button("Play Easy");       selectEasyButton.setPrefWidth(150);
        Button selectHardButton = new Button("Play Hard");       selectHardButton.setPrefWidth(150);
        Button selectExtremeButton = new Button("Play Extreme"); selectExtremeButton.setPrefWidth(150);
        Button selectOptionsButton = new Button("Options");      selectOptionsButton.setPrefWidth(150);
        Button quitDesktopButton = new Button("Quit Game");      quitDesktopButton.setPrefWidth(150);

        selectEasyButton.setOnAction(e -> controlGameInitialization(9, 9, 10, "Easy"));
        selectHardButton.setOnAction(e -> controlGameInitialization(15, 15, 30, "Hard"));
        selectExtremeButton.setOnAction(e -> controlGameInitialization(20, 20, 80, "Extreme"));

        selectOptionsButton.setOnAction(e -> {
            optionsReturnButton.setOnAction(ev -> mainApplicationStage.setScene(mainMenuScene));
            mainApplicationStage.setScene(optionsMenuScene);
        });
        quitDesktopButton.setOnAction(e -> Platform.exit());

        menuVerticalLayout.getChildren().addAll(
                selectEasyButton, selectHardButton, selectExtremeButton,
                selectOptionsButton, quitDesktopButton
        );
        mainMenuRoot.setCenter(menuVerticalLayout);
        mainMenuScene = new Scene(mainMenuRoot);
    }

    private void updateLeaderboardTable() {
        leaderboardTable.getChildren().clear();
        leaderboardTable.add(createTableCell("Rank", true), 0, 0);
        leaderboardTable.add(createTableCell("Time", true), 1, 0);

        List<Integer> scores = DatabaseHelper.getTop5Scores(currentLeaderboardDifficulty);
        if (scores.isEmpty()) {
            StackPane emptyCell = createTableCell("No scores yet", false);
            emptyCell.setPrefWidth(200);
            leaderboardTable.add(emptyCell, 0, 1, 2, 1);
        } else {
            for (int i = 0; i < scores.size(); i++) {
                leaderboardTable.add(createTableCell("N" + (i + 1), false), 0, i + 1);
                int totalSeconds = scores.get(i);
                leaderboardTable.add(createTableCell(
                        String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60), false), 1, i + 1);
            }
        }
    }

    private StackPane createTableCell(String textContent, boolean isHeader) {
        String textColor = isDarkMode ? "#ffffff" : "#000000";
        Text text = new Text(textContent);
        text.setFont(Font.font("Arial", isHeader ? FontWeight.BOLD : FontWeight.NORMAL, 14));
        text.setFill(Color.web(textColor));

        StackPane cell = new StackPane(text);
        cell.setAlignment(Pos.CENTER);
        cell.setPrefWidth(100);
        cell.setPrefHeight(30);

        String cellBg = isDarkMode
                ? (isHeader ? "#323232" : "#4b4d4e")
                : (isHeader ? "#dee2e6" : "#ffffff");
        cell.setStyle("-fx-background-color: " + cellBg + ";");
        return cell;
    }

    private void buildOptionsMenuInterface() {
        optionsVerticalLayout = new VBox(20);
        optionsVerticalLayout.setAlignment(Pos.CENTER);
        optionsVerticalLayout.setPrefSize(800, 600);

        optionsTitleText = new Text("OPTIONS");
        optionsTitleText.setFont(Font.font("Arial", FontWeight.BOLD, 30));

        themeLabel = new Label("Game Theme");
        ComboBox<String> themeBox = new ComboBox<>();
        themeBox.getItems().addAll("Light Mode", "Dark Mode");
        themeBox.setValue(isDarkMode ? "Dark Mode" : "Light Mode");
        themeBox.setOnAction(e -> {
            isDarkMode = themeBox.getValue().equals("Dark Mode");
            DatabaseHelper.saveSetting("dark_mode", String.valueOf(isDarkMode));
            updateTheme();
        });

        musicTrackLabel = new Label("Background Music");
        ComboBox<String> musicBox = new ComboBox<>();
        List<String> musicFiles = AudioSystem.scanAudioDirectory("/audio/music/");
        Map<String, String> musicMap = new LinkedHashMap<>();
        for (String f : musicFiles) musicMap.put(stripExtension(f), f);

        if (musicMap.isEmpty()) {
            musicBox.getItems().add("— No tracks found —");
            musicBox.setValue("— No tracks found —");
            musicBox.setDisable(true);
        } else {
            musicBox.getItems().addAll(musicMap.keySet());

            String initialMusicDisplay = musicBox.getItems().get(0);
            if (savedMusicTrack != null) {
                for (Map.Entry<String, String> entry : musicMap.entrySet()) {
                    if (entry.getValue().equals(savedMusicTrack)) {
                        initialMusicDisplay = entry.getKey();
                        break;
                    }
                }
            }
            musicBox.setValue(initialMusicDisplay);
        }

        musicBox.setOnAction(e -> {
            if (!musicBox.isDisabled()) {
                String actual = musicMap.get(musicBox.getValue());
                if (actual != null) {
                    currentAudioSystem.setMusicTrack(actual);
                    DatabaseHelper.saveSetting("music_track", actual);
                    savedMusicTrack = actual;
                }
            }
        });

        clickSoundLabel = new Label("Click Sound Effect");
        ComboBox<String> soundBox = new ComboBox<>();
        List<String> clickFiles = AudioSystem.scanAudioDirectory("/audio/clicks/");
        Map<String, String> clickMap = new LinkedHashMap<>();
        for (String f : clickFiles) clickMap.put(stripExtension(f), f);

        if (clickMap.isEmpty()) {
            soundBox.getItems().add("— No sounds found —");
            soundBox.setValue("— No sounds found —");
            soundBox.setDisable(true);
        } else {
            soundBox.getItems().addAll(clickMap.keySet());

            String initialClickDisplay = soundBox.getItems().get(0);
            if (savedClickSound != null) {
                for (Map.Entry<String, String> entry : clickMap.entrySet()) {
                    if (entry.getValue().equals(savedClickSound)) {
                        initialClickDisplay = entry.getKey();
                        break;
                    }
                }
            }
            soundBox.setValue(initialClickDisplay);
        }

        soundBox.setOnAction(e -> {
            if (!soundBox.isDisabled()) {
                String actual = clickMap.get(soundBox.getValue());
                if (actual != null) {
                    currentAudioSystem.setClickSound(actual);
                    DatabaseHelper.saveSetting("click_sound", actual);
                    savedClickSound = actual;
                }
            }
        });

        musicControlLabel = new Label("Music Volume");
        Slider musicControlSlider = new Slider(0, 1, 0.5);
        musicControlSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                currentAudioSystem.setMusicVolume(newVal.doubleValue()));

        soundControlLabel = new Label("Sound Effects Volume");
        Slider soundControlSlider = new Slider(0, 1, 0.5);
        soundControlSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                currentAudioSystem.setSoundVolume(newVal.doubleValue()));

        optionsReturnButton = new Button("Back");

        optionsVerticalLayout.getChildren().addAll(
                optionsTitleText,
                themeLabel, themeBox,
                musicTrackLabel, musicBox, musicControlLabel, musicControlSlider,
                clickSoundLabel, soundBox, soundControlLabel, soundControlSlider,
                optionsReturnButton
        );
        optionsMenuScene = new Scene(optionsVerticalLayout);
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private void updateTheme() {
        String bgColor = isDarkMode ? "#2b2b2b" : "#f4f4f4";
        String textColor = isDarkMode ? "#ffffff" : "#000000";
        String leaderboardBg = isDarkMode ? "#3c3f41" : "#e9ecef";
        String leaderboardBorder = isDarkMode ? "#555555" : "#ced4da";

        if (mainMenuRoot != null)
            mainMenuRoot.setStyle("-fx-background-color: " + bgColor + ";");
        if (optionsVerticalLayout != null)
            optionsVerticalLayout.setStyle("-fx-background-color: " + bgColor + ";");
        if (leaderboardLayout != null)
            leaderboardLayout.setStyle(
                    "-fx-background-color: " + leaderboardBg + ";" +
                            " -fx-border-color: " + leaderboardBorder + ";" +
                            " -fx-border-width: 0 2 0 0;");

        if (titleDisplayText != null) titleDisplayText.setFill(Color.web(textColor));
        if (optionsTitleText != null) optionsTitleText.setFill(Color.web(textColor));
        if (leaderboardTitle != null) leaderboardTitle.setFill(Color.web(textColor));
        if (musicControlLabel != null) musicControlLabel.setTextFill(Color.web(textColor));
        if (soundControlLabel != null) soundControlLabel.setTextFill(Color.web(textColor));
        if (themeLabel != null) themeLabel.setTextFill(Color.web(textColor));
        if (musicTrackLabel != null) musicTrackLabel.setTextFill(Color.web(textColor));
        if (clickSoundLabel != null) clickSoundLabel.setTextFill(Color.web(textColor));

        updateLeaderboardTable();
    }

    private void controlGameInitialization(int targetColumns, int targetRows, int targetMines, String difficultyName) {
        this.currentGridColumns = targetColumns;
        this.currentGridRows = targetRows;
        this.currentTotalMines = targetMines;
        this.availableFlagsCount = targetMines;
        this.currentDifficulty = difficultyName;
        this.isGameCurrentlyOver = false;
        this.isGamePaused = false;

        this.timeSecondsElapsed = 0;
        this.isFirstClick = true;
        if (gameTimer != null) gameTimer.stop();
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeSecondsElapsed++;
            timerText.setText(String.format("Time: %d:%02d",
                    timeSecondsElapsed / 60, timeSecondsElapsed % 60));
        }));
        gameTimer.setCycleCount(Animation.INDEFINITE);

        activeGameGrid = new Cell[currentGridColumns][currentGridRows];
        primaryGameRootPane = new StackPane();
        primaryGameRootPane.setPrefSize(800, 600);
        primaryGameRootPane.setStyle("-fx-background-color: " + (isDarkMode ? "#2b2b2b" : "#f4f4f4") + ";");

        BorderPane borderOrganizationPane = new BorderPane();

        topStatisticsBar = new HBox(20);
        topStatisticsBar.setAlignment(Pos.CENTER);
        topStatisticsBar.setStyle("-fx-padding: 15; -fx-background-color: #333333;");

        timerText = new Text("Time: 0:00");
        timerText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        timerText.setFill(Color.WHITE);

        activeFlagCounterText = new Text("Flags: " + availableFlagsCount);
        activeFlagCounterText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        activeFlagCounterText.setFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        topStatisticsBar.getChildren().addAll(timerText, spacer, activeFlagCounterText);

        flagFlashTimeline = new Timeline(
                new KeyFrame(Duration.millis(0),   e -> activeFlagCounterText.setFill(Color.RED)),
                new KeyFrame(Duration.millis(100), e -> activeFlagCounterText.setFill(Color.BLACK)),
                new KeyFrame(Duration.millis(200), e -> activeFlagCounterText.setFill(Color.RED)),
                new KeyFrame(Duration.millis(300), e -> activeFlagCounterText.setFill(Color.BLACK)),
                new KeyFrame(Duration.millis(400), e -> activeFlagCounterText.setFill(Color.WHITE))
        );

        GridPane centralizedGridPane = new GridPane();
        centralizedGridPane.setAlignment(Pos.CENTER);
        populateGridWithCells(centralizedGridPane);

        borderOrganizationPane.setTop(topStatisticsBar);
        borderOrganizationPane.setCenter(centralizedGridPane);
        primaryGameRootPane.getChildren().add(borderOrganizationPane);

        activeGameScene = new Scene(primaryGameRootPane);
        activeGameScene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE && !isGameCurrentlyOver) {
                togglePauseMenu();
            }
        });

        mainApplicationStage.setScene(activeGameScene);
    }

    private void togglePauseMenu() {
        if (isGamePaused) {
            primaryGameRootPane.getChildren().remove(pauseMenuOverlay);
            isGamePaused = false;
            gameTimer.play();
        } else {
            if (pauseMenuOverlay == null) buildPauseMenu();
            gameTimer.pause();
            primaryGameRootPane.getChildren().add(pauseMenuOverlay);
            isGamePaused = true;
        }
    }

    private void buildPauseMenu() {
        pauseMenuOverlay = new VBox(20);
        pauseMenuOverlay.setAlignment(Pos.CENTER);
        pauseMenuOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

        Text pauseTitle = new Text("GAME PAUSED");
        pauseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        pauseTitle.setFill(Color.WHITE);

        Button resumeButton = new Button("Resume");
        resumeButton.setOnAction(e -> togglePauseMenu());

        Button settingsButton = new Button("Settings");
        settingsButton.setOnAction(e -> {
            optionsReturnButton.setOnAction(ev -> mainApplicationStage.setScene(activeGameScene));
            mainApplicationStage.setScene(optionsMenuScene);
        });

        Button mainMenuButton = new Button("Quit to Main Menu");
        mainMenuButton.setOnAction(e -> {
            isGamePaused = false;
            if (gameTimer != null) gameTimer.stop();
            updateLeaderboardTable();
            mainApplicationStage.setScene(mainMenuScene);
        });

        Button quitDesktopButton = new Button("Quit to Desktop");
        quitDesktopButton.setOnAction(e -> Platform.exit());

        pauseMenuOverlay.getChildren().addAll(pauseTitle, resumeButton, settingsButton, mainMenuButton, quitDesktopButton);
    }

    private void populateGridWithCells(GridPane activeGridPane) {
        boolean[][] minePlacementMatrix = new boolean[currentGridColumns][currentGridRows];
        Random rand = new Random();

        int placed = 0;
        while (placed < currentTotalMines) {
            int col = rand.nextInt(currentGridColumns);
            int row = rand.nextInt(currentGridRows);
            if (!minePlacementMatrix[col][row]) {
                minePlacementMatrix[col][row] = true;
                placed++;
            }
        }

        for (int row = 0; row < currentGridRows; row++) {
            for (int col = 0; col < currentGridColumns; col++) {
                Cell newlyCreatedCell = new Cell(col, row, minePlacementMatrix[col][row]);
                activeGameGrid[col][row] = newlyCreatedCell;
                activeGridPane.add(newlyCreatedCell, col, row);

                newlyCreatedCell.setOnMouseClicked(mouseEvent -> {
                    if (isGameCurrentlyOver || isGamePaused) return;
                    if (isFirstClick) { gameTimer.play(); isFirstClick = false; }

                    if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                        controlPrimaryMouseClick(newlyCreatedCell);
                    } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                        controlSecondaryMouseClick(newlyCreatedCell);
                    }
                    evaluateCurrentWinCondition();
                });
            }
        }

        for (int row = 0; row < currentGridRows; row++) {
            for (int col = 0; col < currentGridColumns; col++) {
                if (!activeGameGrid[col][row].getIsMine()) {
                    long count = retrieveNeighboringCellsList(activeGameGrid[col][row])
                            .stream().filter(Cell::getIsMine).count();
                    activeGameGrid[col][row].setNeighboringMineCount((int) count);
                }
            }
        }
    }

    private List<Cell> retrieveNeighboringCellsList(Cell centerTargetCell) {
        List<Cell> neighbors = new ArrayList<>();
        int[] offsets = { -1,-1, 0,-1, 1,-1, -1,0, 1,0, -1,1, 0,1, 1,1 };

        for (int i = 0; i < offsets.length; i += 2) {
            int adjCol = centerTargetCell.getColumnIndex() + offsets[i];
            int adjRow = centerTargetCell.getRowIndex() + offsets[i + 1];
            if (adjCol >= 0 && adjCol < currentGridColumns && adjRow >= 0 && adjRow < currentGridRows) {
                neighbors.add(activeGameGrid[adjCol][adjRow]);
            }
        }
        return neighbors;
    }

    private void controlPrimaryMouseClick(Cell targetedCell) {
        if (targetedCell.getIsRevealed() || targetedCell.getIsFlagged()) return;

        targetedCell.revealCell();
        currentAudioSystem.playClickSound();

        if (targetedCell.getIsMine()) {
            currentAudioSystem.playExplosionSound();
            controlGameTerminationSequence(false);
            return;
        }

        long surroundingMines = retrieveNeighboringCellsList(targetedCell)
                .stream().filter(Cell::getIsMine).count();
        if (surroundingMines == 0) {
            for (Cell neighbor : retrieveNeighboringCellsList(targetedCell)) {
                controlPrimaryMouseClick(neighbor);
            }
        }
    }

    private void controlSecondaryMouseClick(Cell targetedCell) {
        if (targetedCell.getIsRevealed()) return;

        if (!targetedCell.getIsFlagged() && availableFlagsCount <= 0) {
            if (flagFlashTimeline != null && flagFlashTimeline.getStatus() != Animation.Status.RUNNING) {
                flagFlashTimeline.playFromStart();
            }
            return;
        }

        boolean flagged = targetedCell.toggleFlagState();
        availableFlagsCount += flagged ? -1 : 1;
        activeFlagCounterText.setText("Flags: " + availableFlagsCount);
        currentAudioSystem.playClickSound();
    }

    private void evaluateCurrentWinCondition() {
        outer:
        for (int row = 0; row < currentGridRows; row++) {
            for (int col = 0; col < currentGridColumns; col++) {
                Cell cell = activeGameGrid[col][row];
                if (!cell.getIsMine() && !cell.getIsRevealed()) {
                    break outer;
                }
                if (col == currentGridColumns - 1 && row == currentGridRows - 1) {
                    controlGameTerminationSequence(true);
                }
            }
        }
        boolean won = true;
        for (int row = 0; row < currentGridRows; row++) {
            for (int col = 0; col < currentGridColumns; col++) {
                if (!activeGameGrid[col][row].getIsMine() && !activeGameGrid[col][row].getIsRevealed()) {
                    won = false;
                    break;
                }
            }
            if (!won) break;
        }
        if (won) controlGameTerminationSequence(true);
    }

    private void controlGameTerminationSequence(boolean playerHasWon) {
        if (isGameCurrentlyOver) return;
        isGameCurrentlyOver = true;
        if (gameTimer != null) gameTimer.stop();

        System.out.println("game over - won: " + playerHasWon);

        if (playerHasWon) DatabaseHelper.saveScore(currentDifficulty, timeSecondsElapsed);

        for (int row = 0; row < currentGridRows; row++) {
            for (int col = 0; col < currentGridColumns; col++) {
                if (activeGameGrid[col][row].getIsMine()) {
                    activeGameGrid[col][row].revealCell();
                }
            }
        }

        displayEndGameOverlayInterface(playerHasWon);
    }

    private void displayEndGameOverlayInterface(boolean playerHasWon) {
        VBox endGameOverlayLayout = new VBox(20);
        endGameOverlayLayout.setAlignment(Pos.CENTER);
        endGameOverlayLayout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

        Text displayResultText = new Text(playerHasWon ? "VICTORY!" : "GAME OVER");
        displayResultText.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        displayResultText.setFill(playerHasWon ? Color.LIMEGREEN : Color.RED);

        Text finalTimeText = new Text(String.format("Time: %d:%02d",
                timeSecondsElapsed / 60, timeSecondsElapsed % 60));
        finalTimeText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        finalTimeText.setFill(Color.WHITE);

        Button executeRestartButton = new Button("Restart");
        executeRestartButton.setOnAction(e ->
                controlGameInitialization(currentGridColumns, currentGridRows, currentTotalMines, currentDifficulty));

        Button viewBoardButton = new Button("View Board");
        viewBoardButton.setOnAction(e -> {
            primaryGameRootPane.getChildren().remove(endGameOverlayLayout);

            Button floatingRestartButton = new Button("Restart Game");
            floatingRestartButton.setOnAction(re ->
                    controlGameInitialization(currentGridColumns, currentGridRows, currentTotalMines, currentDifficulty));

            Button floatingMenuButton = new Button("Return to Menu");
            floatingMenuButton.setOnAction(re -> {
                updateLeaderboardTable();
                mainApplicationStage.setScene(mainMenuScene);
            });

            topStatisticsBar.getChildren().clear();
            topStatisticsBar.getChildren().addAll(floatingRestartButton, floatingMenuButton);
        });

        Button returnToMenuButton = new Button("Quit to Main Menu");
        returnToMenuButton.setOnAction(e -> {
            updateLeaderboardTable();
            mainApplicationStage.setScene(mainMenuScene);
        });

        endGameOverlayLayout.getChildren().addAll(
                displayResultText, finalTimeText,
                executeRestartButton, viewBoardButton, returnToMenuButton
        );
        primaryGameRootPane.getChildren().add(endGameOverlayLayout);
    }

    public static void main(String[] launchArguments) {
        launch(launchArguments);
    }
}