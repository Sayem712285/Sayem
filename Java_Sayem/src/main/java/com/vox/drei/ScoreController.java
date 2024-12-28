package com.vox.drei;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ScoreController {
    @FXML private VBox rootVBox;
    @FXML private Label quizNameLabel;
    @FXML private Label scoreLabel;
    @FXML private TableView<Question> answersTable;
    @FXML private TableColumn<Question, String> questionColumn;
    @FXML private TableColumn<Question, String> userAnswerColumn;
    @FXML private TableColumn<Question, String> correctAnswerColumn;

    private List<Question> questions;
    private String quizName;
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        bundle = DreiMain.getBundle();
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        answersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        questionColumn.setMaxWidth(1f * Integer.MAX_VALUE * 40);
        userAnswerColumn.setMaxWidth(1f * Integer.MAX_VALUE * 30);
        correctAnswerColumn.setMaxWidth(1f * Integer.MAX_VALUE * 30);

        answersTable.setVisible(false);
    }

    public void setScore(int score, int totalQuestions) {
        // Use MessageFormat to replace {0} and {1} with score and totalQuestions
        String formattedScore = MessageFormat.format(bundle.getString("your.score"), score, totalQuestions);
        scoreLabel.setText(formattedScore);  // Assuming scoreLabel is a Label
    }


    public void setQuestions(List<Question> questions) {
        this.questions = questions;
        populateAnswersTable();
    }

    public void setQuizName(String quizName) {
        this.quizName = quizName;
        quizNameLabel.setText(bundle.getString("quiz") + ": " + quizName);
    }

    private void populateAnswersTable() {
        questionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getQuestion()));
        userAnswerColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUserAnswer()));
        correctAnswerColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCorrectAnswer()));

        answersTable.getItems().setAll(questions);
    }

    @FXML
    private void backToMain() throws Exception {
        DreiMain.showMainView();
    }

    @FXML
    private void viewAnswers() {
        answersTable.setVisible(true);

        Stage stage = (Stage) rootVBox.getScene().getWindow();

        // Center the stage on the screen
        stage.centerOnScreen();
    }
}