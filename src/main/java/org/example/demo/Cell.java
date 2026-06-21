package org.example.demo;

import javafx.animation.ScaleTransition;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Cell extends StackPane {
    private final int columnIndex;
    private final int rowIndex;
    private final boolean isMine;
    private int neighboringMineCount = 0;
    private boolean isRevealed = false;
    private boolean isFlagged = false;
    private final Rectangle backgroundRectangle = new Rectangle(30, 30);
    private final Text numericText = new Text();
    private final Group mineVisualGroup = new Group();
    private final Circle explosionCircle = new Circle(2, Color.ORANGE);

    public Cell(int columnIndex, int rowIndex, boolean isMine) {
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.isMine = isMine;

        backgroundRectangle.setFill(Color.LIGHTGRAY);
        backgroundRectangle.setStroke(Color.GRAY);

        numericText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        numericText.setVisible(false);

        Circle mainMineBody = new Circle(8, Color.BLACK);
        Rectangle verticalSpike = new Rectangle(20, 4, Color.BLACK);
        verticalSpike.setTranslateX(-10);
        verticalSpike.setTranslateY(-2);
        Rectangle horizontalSpike = new Rectangle(4, 20, Color.BLACK);
        horizontalSpike.setTranslateX(-2);
        horizontalSpike.setTranslateY(-10);

        mineVisualGroup.getChildren().addAll(verticalSpike, horizontalSpike, mainMineBody);
        mineVisualGroup.setVisible(false);

        explosionCircle.setVisible(false);

        getChildren().addAll(backgroundRectangle, numericText, mineVisualGroup, explosionCircle);
    }

    public int getColumnIndex() { return columnIndex; }
    public int getRowIndex() { return rowIndex; }
    public boolean getIsMine() { return isMine; }
    public boolean getIsRevealed() { return isRevealed; }
    public boolean getIsFlagged() { return isFlagged; }

    public void setNeighboringMineCount(int targetCount) {
        this.neighboringMineCount = targetCount;
        if (targetCount > 0 && !isMine) {
            numericText.setText(String.valueOf(targetCount));
            numericText.setFill(determineNumericColor(targetCount));
        }
    }

    public void revealCell() {
        if (isRevealed || isFlagged) return;

        isRevealed = true;
        backgroundRectangle.setFill(Color.WHITE);

        if (isMine) {
            mineVisualGroup.setVisible(true);
            backgroundRectangle.setFill(Color.DARKRED);
            explosionCircle.setVisible(true);

            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), explosionCircle);
            scaleTransition.setToX(7);
            scaleTransition.setToY(7);
            scaleTransition.setOnFinished(actionEvent -> explosionCircle.setVisible(false));
            scaleTransition.play();
        } else {
            numericText.setVisible(true);
            if (neighboringMineCount == 0) {
                numericText.setText("");
            }
        }
    }

    public boolean toggleFlagState() {
        if (isRevealed) return false;

        if (!isFlagged) {
            isFlagged = true;
            numericText.setText("🚩");
            numericText.setFill(Color.RED);
            numericText.setVisible(true);
            return true;
        } else {
            isFlagged = false;
            numericText.setVisible(false);
            if (neighboringMineCount > 0 && !isMine) {
                numericText.setText(String.valueOf(neighboringMineCount));
                numericText.setFill(determineNumericColor(neighboringMineCount));
            } else {
                numericText.setText("");
            }
            return false;
        }
    }

    private Color determineNumericColor(int targetCount) {
        return switch (targetCount) {
            case 1 -> Color.BLUE;
            case 2 -> Color.GREEN;
            case 3 -> Color.RED;
            case 4 -> Color.PURPLE;
            case 5 -> Color.MAROON;
            default -> Color.BLACK;
        };
    }
}