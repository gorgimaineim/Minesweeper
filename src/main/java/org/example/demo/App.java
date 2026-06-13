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
import java.util.List;
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

    // Timer Variables
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

    // Leaderboard Variables
    private GridPane leaderboardTable;
    private String currentLeaderboardDifficulty = "Easy";

    @Override
    public void start(Stage applicationStage) {
        DatabaseHelper.initializeDatabase();

        this.mainApplicationStage = applicationStage;
        this.currentAudioSystem = new AudioSystem();

        buildOptionsMenuInterface();
        buildMainMenuInterface();

        mainApplicationStage.setTitle("JavaFX Minesweeper");
        mainApplicationStage.setScene(mainMenuScene);
        mainApplicationStage.setResizable(false);
        mainApplicationStage.show();
    }

    private void buildMainMenuInterface() {
        BorderPane rootLayout = new BorderPane();
        rootLayout.setPrefSize(800, 600);
        rootLayout.setStyle("-fx-background-color: #f4f4f4;");

        // --- TOP: TITLE ---
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(40, 0, 20, 0)); // Pushes it slightly below the "roof"
        Text titleDisplayText = new Text("MINESWEEPER");
        titleDisplayText.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        titleBox.getChildren().add(titleDisplayText);

        rootLayout.setTop(titleBox);

        // --- LEFT SIDE: LEADERBOARD ---
        VBox leaderboardLayout = new VBox(15);
        leaderboardLayout.setAlignment(Pos.TOP_CENTER);
        leaderboardLayout.setPrefWidth(280);
        leaderboardLayout.setPadding(new Insets(20, 20, 20, 20)); // Sticks to the wall, with inner breathing room
        leaderboardLayout.setStyle("-fx-background-color: #e9ecef; -fx-border-color: #ced4da; -fx-border-width: 0 2 0 0;");

        Text leaderboardTitle = new Text("LEADERBOARDS");
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
        // This creates the "Excel" grid lines by showing the background color through the gaps
        leaderboardTable.setStyle("-fx-background-color: #adb5bd; -fx-border-color: #adb5bd; -fx-border-width: 1;");
        leaderboardTable.setHgap(1);
        leaderboardTable.setVgap(1);

        updateLeaderboardTable();

        leaderboardLayout.getChildren().addAll(leaderboardTitle, difficultySwitcher, leaderboardTable);
        rootLayout.setLeft(leaderboardLayout);

        // --- CENTER: MAIN MENU BUTTONS ---
        VBox menuVerticalLayout = new VBox(20);
        menuVerticalLayout.setAlignment(Pos.CENTER);
        // We push it slightly left so it centers perfectly in the remaining visual space
        menuVerticalLayout.setPadding(new Insets(0, 50, 80, 0));

        Button selectEasyButton = new Button("Play Easy");
        selectEasyButton.setPrefWidth(150);
        Button selectHardButton = new Button("Play Hard");
        selectHardButton.setPrefWidth(150);
        Button selectExtremeButton = new Button("Play Extreme");
        selectExtremeButton.setPrefWidth(150);
        Button selectOptionsButton = new Button("Options");
        selectOptionsButton.setPrefWidth(150);
        Button quitDesktopButton = new Button("Quit Game");
        quitDesktopButton.setPrefWidth(150);

        selectEasyButton.setOnAction(actionEvent -> controlGameInitialization(9, 9, 10, "Easy"));
        selectHardButton.setOnAction(actionEvent -> controlGameInitialization(15, 15, 30, "Hard"));
        selectExtremeButton.setOnAction(actionEvent -> controlGameInitialization(20, 20, 80, "Extreme"));

        selectOptionsButton.setOnAction(actionEvent -> {
            optionsReturnButton.setOnAction(e -> mainApplicationStage.setScene(mainMenuScene));
            mainApplicationStage.setScene(optionsMenuScene);
        });

        quitDesktopButton.setOnAction(actionEvent -> Platform.exit());

        menuVerticalLayout.getChildren().addAll(selectEasyButton, selectHardButton, selectExtremeButton, selectOptionsButton, quitDesktopButton);

        rootLayout.setCenter(menuVerticalLayout);
        mainMenuScene = new Scene(rootLayout);
    }

    private void updateLeaderboardTable() {
        leaderboardTable.getChildren().clear();

        // Table Headers
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
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;
                leaderboardTable.add(createTableCell(String.format("%d:%02d", minutes, seconds), false), 1, i + 1);
            }
        }
    }

    // Helper method to create individual cells for the "Excel" look
    private StackPane createTableCell(String textContent, boolean isHeader) {
        Text text = new Text(textContent);
        text.setFont(Font.font("Arial", isHeader ? FontWeight.BOLD : FontWeight.NORMAL, 14));

        StackPane cell = new StackPane(text);
        cell.setAlignment(Pos.CENTER);
        cell.setPrefWidth(100);
        cell.setPrefHeight(30);

        // Headers get a slight grey tint, data cells are pure white
        cell.setStyle("-fx-background-color: " + (isHeader ? "#dee2e6" : "#ffffff") + ";");
        return cell;
    }

    private void buildOptionsMenuInterface() {
        VBox optionsVerticalLayout = new VBox(20);
        optionsVerticalLayout.setAlignment(Pos.CENTER);
        optionsVerticalLayout.setPrefSize(800, 600);

        Text optionsTitleText = new Text("OPTIONS");
        optionsTitleText.setFont(Font.font("Arial", FontWeight.BOLD, 30));

        Label musicControlLabel = new Label("Music Volume");
        Slider musicControlSlider = new Slider(0, 1, 0.5);
        musicControlSlider.valueProperty().addListener((observableValue, oldSliderValue, newSliderValue) ->
                currentAudioSystem.setMusicVolume(newSliderValue.doubleValue()));

        Label soundControlLabel = new Label("Sound Effects Volume");
        Slider soundControlSlider = new Slider(0, 1, 0.5);
        soundControlSlider.valueProperty().addListener((observableValue, oldSliderValue, newSliderValue) ->
                currentAudioSystem.setSoundVolume(newSliderValue.doubleValue()));

        optionsReturnButton = new Button("Back");

        optionsVerticalLayout.getChildren().addAll(optionsTitleText, musicControlLabel, musicControlSlider, soundControlLabel, soundControlSlider, optionsReturnButton);
        optionsMenuScene = new Scene(optionsVerticalLayout);
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
            int minutes = timeSecondsElapsed / 60;
            int seconds = timeSecondsElapsed % 60;
            timerText.setText(String.format("Time: %d:%02d", minutes, seconds));
        }));
        gameTimer.setCycleCount(Animation.INDEFINITE);

        activeGameGrid = new Cell[currentGridColumns][currentGridRows];
        primaryGameRootPane = new StackPane();
        primaryGameRootPane.setPrefSize(800, 600);
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
                new KeyFrame(Duration.millis(0), actionEvent -> activeFlagCounterText.setFill(Color.RED)),
                new KeyFrame(Duration.millis(100), actionEvent -> activeFlagCounterText.setFill(Color.BLACK)),
                new KeyFrame(Duration.millis(200), actionEvent -> activeFlagCounterText.setFill(Color.RED)),
                new KeyFrame(Duration.millis(300), actionEvent -> activeFlagCounterText.setFill(Color.BLACK)),
                new KeyFrame(Duration.millis(400), actionEvent -> activeFlagCounterText.setFill(Color.WHITE))
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
            if (pauseMenuOverlay == null) {
                buildPauseMenu();
            }
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
        resumeButton.setOnAction(actionEvent -> togglePauseMenu());

        Button settingsButton = new Button("Settings");
        settingsButton.setOnAction(actionEvent -> {
            optionsReturnButton.setOnAction(e -> mainApplicationStage.setScene(activeGameScene));
            mainApplicationStage.setScene(optionsMenuScene);
        });

        Button mainMenuButton = new Button("Quit to Main Menu");
        mainMenuButton.setOnAction(actionEvent -> {
            isGamePaused = false;
            if(gameTimer != null) gameTimer.stop();
            updateLeaderboardTable();
            mainApplicationStage.setScene(mainMenuScene);
        });

        Button quitDesktopButton = new Button("Quit to Desktop");
        quitDesktopButton.setOnAction(actionEvent -> Platform.exit());

        pauseMenuOverlay.getChildren().addAll(pauseTitle, resumeButton, settingsButton, mainMenuButton, quitDesktopButton);
    }

    private void populateGridWithCells(GridPane activeGridPane) {
        boolean[][] minePlacementMatrix = new boolean[currentGridColumns][currentGridRows];
        Random matrixRandomGenerator = new Random();
        int currentlyPlacedMinesCount = 0;

        while (currentlyPlacedMinesCount < currentTotalMines) {
            int randomColumnIndex = matrixRandomGenerator.nextInt(currentGridColumns);
            int randomRowIndex = matrixRandomGenerator.nextInt(currentGridRows);
            if (!minePlacementMatrix[randomColumnIndex][randomRowIndex]) {
                minePlacementMatrix[randomColumnIndex][randomRowIndex] = true;
                currentlyPlacedMinesCount++;
            }
        }

        for (int iterationRowIndex = 0; iterationRowIndex < currentGridRows; iterationRowIndex++) {
            for (int iterationColumnIndex = 0; iterationColumnIndex < currentGridColumns; iterationColumnIndex++) {
                Cell newlyCreatedCell = new Cell(iterationColumnIndex, iterationRowIndex, minePlacementMatrix[iterationColumnIndex][iterationRowIndex]);
                activeGameGrid[iterationColumnIndex][iterationRowIndex] = newlyCreatedCell;
                activeGridPane.add(newlyCreatedCell, iterationColumnIndex, iterationRowIndex);

                newlyCreatedCell.setOnMouseClicked(mouseEvent -> {
                    if (isGameCurrentlyOver || isGamePaused) return;

                    if (isFirstClick) {
                        gameTimer.play();
                        isFirstClick = false;
                    }

                    if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                        controlPrimaryMouseClick(newlyCreatedCell);
                    } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                        controlSecondaryMouseClick(newlyCreatedCell);
                    }
                    evaluateCurrentWinCondition();
                });
            }
        }

        for (int verificationRowIndex = 0; verificationRowIndex < currentGridRows; verificationRowIndex++) {
            for (int verificationColumnIndex = 0; verificationColumnIndex < currentGridColumns; verificationColumnIndex++) {
                if (!activeGameGrid[verificationColumnIndex][verificationRowIndex].getIsMine()) {
                    long totalSurroundingMines = retrieveNeighboringCellsList(activeGameGrid[verificationColumnIndex][verificationRowIndex])
                            .stream()
                            .filter(Cell::getIsMine)
                            .count();
                    activeGameGrid[verificationColumnIndex][verificationRowIndex].setNeighboringMineCount((int) totalSurroundingMines);
                }
            }
        }
    }

    private List<Cell> retrieveNeighboringCellsList(Cell centerTargetCell) {
        List<Cell> extractedNeighborsList = new ArrayList<>();
        int[] coordinateDirectionOffsets = {
                -1, -1,   0, -1,   1, -1,
                -1,  0,            1,  0,
                -1,  1,   0,  1,   1,  1
        };

        for (int arrayOffsetIndex = 0; arrayOffsetIndex < coordinateDirectionOffsets.length; arrayOffsetIndex += 2) {
            int deltaColumnModification = coordinateDirectionOffsets[arrayOffsetIndex];
            int deltaRowModification = coordinateDirectionOffsets[arrayOffsetIndex + 1];

            int computedAdjacentColumn = centerTargetCell.getColumnIndex() + deltaColumnModification;
            int computedAdjacentRow = centerTargetCell.getRowIndex() + deltaRowModification;

            if (computedAdjacentColumn >= 0 && computedAdjacentColumn < currentGridColumns && computedAdjacentRow >= 0 && computedAdjacentRow < currentGridRows) {
                extractedNeighborsList.add(activeGameGrid[computedAdjacentColumn][computedAdjacentRow]);
            }
        }
        return extractedNeighborsList;
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

        long surroundingMinesDetectedCount = retrieveNeighboringCellsList(targetedCell).stream().filter(Cell::getIsMine).count();
        if (surroundingMinesDetectedCount == 0) {
            for (Cell adjacentEmptyCell : retrieveNeighboringCellsList(targetedCell)) {
                controlPrimaryMouseClick(adjacentEmptyCell);
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

        boolean isCellCurrentlyFlagged = targetedCell.toggleFlagState();
        if (isCellCurrentlyFlagged) {
            availableFlagsCount--;
        } else {
            availableFlagsCount++;
        }
        activeFlagCounterText.setText("Flags: " + availableFlagsCount);
        currentAudioSystem.playClickSound();
    }

    private void evaluateCurrentWinCondition() {
        boolean isVictoryAchieved = true;
        for (int evaluationRowIndex = 0; evaluationRowIndex < currentGridRows; evaluationRowIndex++) {
            for (int evaluationColumnIndex = 0; evaluationColumnIndex < currentGridColumns; evaluationColumnIndex++) {
                Cell currentlyEvaluatedCell = activeGameGrid[evaluationColumnIndex][evaluationRowIndex];
                if (!currentlyEvaluatedCell.getIsMine() && !currentlyEvaluatedCell.getIsRevealed()) {
                    isVictoryAchieved = false;
                    break;
                }
            }
        }
        if (isVictoryAchieved) {
            controlGameTerminationSequence(true);
        }
    }

    private void controlGameTerminationSequence(boolean playerHasWon) {
        isGameCurrentlyOver = true;
        if (gameTimer != null) gameTimer.stop();

        if (playerHasWon) {
            DatabaseHelper.saveScore(currentDifficulty, timeSecondsElapsed);
        }

        for (int finalRevealRowIndex = 0; finalRevealRowIndex < currentGridRows; finalRevealRowIndex++) {
            for (int finalRevealColumnIndex = 0; finalRevealColumnIndex < currentGridColumns; finalRevealColumnIndex++) {
                if (activeGameGrid[finalRevealColumnIndex][finalRevealRowIndex].getIsMine()) {
                    activeGameGrid[finalRevealColumnIndex][finalRevealRowIndex].revealCell();
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

        int minutes = timeSecondsElapsed / 60;
        int seconds = timeSecondsElapsed % 60;
        Text finalTimeText = new Text(String.format("Time: %d:%02d", minutes, seconds));
        finalTimeText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        finalTimeText.setFill(Color.WHITE);

        Button executeRestartButton = new Button("Restart");
        executeRestartButton.setOnAction(actionEvent -> controlGameInitialization(currentGridColumns, currentGridRows, currentTotalMines, currentDifficulty));

        Button viewBoardButton = new Button("View Board");
        viewBoardButton.setOnAction(actionEvent -> {
            primaryGameRootPane.getChildren().remove(endGameOverlayLayout);

            Button floatingRestartButton = new Button("Restart Game");
            floatingRestartButton.setOnAction(restartEvent -> controlGameInitialization(currentGridColumns, currentGridRows, currentTotalMines, currentDifficulty));

            Button floatingMenuButton = new Button("Return to Menu");
            floatingMenuButton.setOnAction(returnEvent -> {
                updateLeaderboardTable();
                mainApplicationStage.setScene(mainMenuScene);
            });

            topStatisticsBar.getChildren().clear();
            topStatisticsBar.getChildren().addAll(floatingRestartButton, floatingMenuButton);
        });

        Button returnToMenuButton = new Button("Quit to Main Menu");
        returnToMenuButton.setOnAction(actionEvent -> {
            updateLeaderboardTable();
            mainApplicationStage.setScene(mainMenuScene);
        });

        endGameOverlayLayout.getChildren().addAll(displayResultText, finalTimeText, executeRestartButton, viewBoardButton, returnToMenuButton);
        primaryGameRootPane.getChildren().add(endGameOverlayLayout);
    }

    public static void main(String[] launchArguments) {
        launch(launchArguments);
    }
}