package com.vox.drei;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ManageQuestionsController {
    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<Question, Integer> numberColumn;
    @FXML private TableColumn<Question, String> questionColumn;
    @FXML private TableColumn<Question, String> typeColumn;
    @FXML private TableColumn<Question, Void> actionsColumn;
    @FXML private TextField searchField;

    private Quiz currentQuiz;
    private ObservableList<Question> observableQuestions;
    private FilteredList<Question> filteredQuestions;
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        bundle = DreiMain.getBundle();
        setupTable();
        questionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        numberColumn.setMaxWidth(1f * Integer.MAX_VALUE * 10);
        questionColumn.setMaxWidth(1f * Integer.MAX_VALUE * 50);
        typeColumn.setMaxWidth(1f * Integer.MAX_VALUE * 20);
        actionsColumn.setMaxWidth(1f * Integer.MAX_VALUE * 20);

        VBox.setVgrow(questionsTable, Priority.ALWAYS);

        observableQuestions = FXCollections.observableArrayList();
        filteredQuestions = new FilteredList<>(observableQuestions, p -> true);
        questionsTable.setItems(filteredQuestions);

        setupSearch();
        setupSorting();

        searchField.setPromptText(bundle.getString("search.questions.prompt"));
    }

    public void setQuiz(Quiz quiz) {
        this.currentQuiz = quiz;
        loadQuestions();
    }

    private void loadQuestions() {
        List<Question> questions = QuestionDatabase.getQuestionsForQuiz(currentQuiz.getId());
        observableQuestions.setAll(questions);
    }

    private void setupSorting() {
        SortedList<Question> sortedQuestions = new SortedList<>(filteredQuestions);
        sortedQuestions.comparatorProperty().bind(questionsTable.comparatorProperty());
        questionsTable.setItems(sortedQuestions);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredQuestions.setPredicate(question -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (question.getQuestion().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (question.getType().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
    }

    private void setupTable() {
        numberColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuestionNumber()).asObject());
        questionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getQuestion()));
        typeColumn.setCellValueFactory(cellData -> {
            Question question = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    question.getType().equals("MULTIPLE_CHOICE") ?
                            bundle.getString("multiple.choice") :
                            bundle.getString("identification")
            );
        });

        numberColumn.setText(bundle.getString("number.column"));
        questionColumn.setText(bundle.getString("question.column"));
        typeColumn.setText(bundle.getString("type.column"));
        actionsColumn.setText(bundle.getString("actions.column"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button(bundle.getString("edit.button"));
            private final Button deleteButton = new Button(bundle.getString("delete.button"));
            private final HBox buttonsBox = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(event -> editQuestion(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> deleteQuestion(getTableView().getItems().get(getIndex())));
                buttonsBox.setPadding(new Insets(2));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void editQuestion(Question question) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("edit.question.title"));
        dialog.setHeaderText(bundle.getString("edit.question.header"));

        ButtonType saveButtonType = new ButtonType(bundle.getString("save.button"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 150, 10, 10));

        TextField questionField = new TextField(question.getQuestion());
        questionField.setPromptText(bundle.getString("question.prompt"));
        ComboBox<String> typeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                bundle.getString("multiple.choice"),
                bundle.getString("identification")
        ));
        typeComboBox.setValue(question.getType().equals("MULTIPLE_CHOICE") ? bundle.getString("multiple.choice") : bundle.getString("identification"));
        VBox answersBox = new VBox(5);

        content.getChildren().addAll(
                new Label(bundle.getString("question.label")),
                questionField,
                new Label(bundle.getString("type.label")),
                typeComboBox,
                new Label(bundle.getString("answers.label")),
                answersBox
        );

        typeComboBox.setOnAction(e -> updateAnswersBox(answersBox, typeComboBox.getValue(), question));
        updateAnswersBox(answersBox, typeComboBox.getValue(), question);

        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400);

        dialog.getDialogPane().setContent(scrollPane);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        questionField.textProperty().addListener((obs, oldVal, newVal) -> validateEditForm(saveButton, questionField, answersBox, typeComboBox));
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateEditForm(saveButton, questionField, answersBox, typeComboBox));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                question.setQuestion(questionField.getText());
                question.setType(typeComboBox.getValue().equals(bundle.getString("multiple.choice")) ? "MULTIPLE_CHOICE" : "IDENTIFICATION");

                if (question.getType().equals("MULTIPLE_CHOICE")) {
                    List<String> answers = answersBox.getChildren().stream()
                            .filter(node -> node instanceof TextField)
                            .map(node -> ((TextField) node).getText())
                            .collect(Collectors.toList());
                    question.setAnswers(answers);
                    ComboBox<String> correctAnswerComboBox = (ComboBox<String>) answersBox.getChildren().get(4);
                    String selectedAnswer = correctAnswerComboBox.getValue();
                    int correctAnswerIndex = Integer.parseInt(selectedAnswer.split(" ")[1]) - 1;
                    question.setCorrectAnswer(answers.get(correctAnswerIndex));
                } else {
                    String correctAnswer = ((TextField) answersBox.getChildren().get(0)).getText();
                    question.setAnswers(List.of(correctAnswer));
                    question.setCorrectAnswer(correctAnswer);
                }

                return question;
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(updatedQuestion -> {
            QuestionDatabase.updateQuestion(updatedQuestion);
            loadQuestions();
        });
    }

    private void updateAnswersBox(VBox answersBox, String type, Question question) {
        answersBox.getChildren().clear();
        if (type.equals(bundle.getString("multiple.choice"))) {
            ComboBox<String> correctAnswerComboBox = new ComboBox<>();
            correctAnswerComboBox.setPromptText(bundle.getString("select.correct.answer"));

            for (int i = 0; i < 4; i++) {
                TextField answerField = new TextField();
                answerField.setPromptText(bundle.getString("answer.prompt") + " " + (i + 1));
                if (question != null && i < question.getAnswers().size()) {
                    answerField.setText(question.getAnswers().get(i));
                }
                answersBox.getChildren().add(answerField);

                final int index = i;
                answerField.textProperty().addListener((obs, oldVal, newVal) -> {
                    updateCorrectAnswerOptions(answersBox, correctAnswerComboBox);
                    Button saveButton = (Button) answersBox.getScene().getWindow().getScene().getRoot().lookup(".button-bar .button:first-child");
                    validateEditForm(saveButton,
                            (TextField) answersBox.getParent().getChildrenUnmodifiable().get(1),
                            answersBox,
                            (ComboBox<String>) answersBox.getParent().getChildrenUnmodifiable().get(3));
                });
            }

            answersBox.getChildren().add(correctAnswerComboBox);
            updateCorrectAnswerOptions(answersBox, correctAnswerComboBox);

            if (question != null) {
                int correctAnswerIndex = question.getAnswers().indexOf(question.getCorrectAnswer()) + 1;
                correctAnswerComboBox.setValue(bundle.getString("answer.prompt") + " " + correctAnswerIndex);
            }

            correctAnswerComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                Button saveButton = (Button) answersBox.getScene().getWindow().getScene().getRoot().lookup(".button-bar .button:first-child");
                validateEditForm(saveButton,
                        (TextField) answersBox.getParent().getChildrenUnmodifiable().get(1),
                        answersBox,
                        (ComboBox<String>) answersBox.getParent().getChildrenUnmodifiable().get(3));
            });
        } else {
            TextField answerField = new TextField();
            answerField.setPromptText(bundle.getString("correct.answer.prompt"));
            if (question != null && !question.getAnswers().isEmpty()) {
                answerField.setText(question.getCorrectAnswer());
            }
            answersBox.getChildren().add(answerField);

            answerField.textProperty().addListener((obs, oldVal, newVal) -> {
                Button saveButton = (Button) answersBox.getScene().getWindow().getScene().getRoot().lookup(".button-bar .button:first-child");
                validateEditForm(saveButton,
                        (TextField) answersBox.getParent().getChildrenUnmodifiable().get(1),
                        answersBox,
                        (ComboBox<String>) answersBox.getParent().getChildrenUnmodifiable().get(3));
            });
        }
    }

    private void updateCorrectAnswerOptions(VBox answersBox, ComboBox<String> correctAnswerComboBox) {
        List<String> options = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            TextField field = (TextField) answersBox.getChildren().get(i);
            String text = field.getText().trim();
            if (!text.isEmpty()) {
                options.add(bundle.getString("answer.prompt") + " " + (i + 1));
            }
        }
        correctAnswerComboBox.setItems(FXCollections.observableArrayList(options));
    }

    private void validateEditForm(Button saveButton, TextField questionField, VBox answersBox, ComboBox<String> typeComboBox) {
        boolean isValid = !questionField.getText().trim().isEmpty();

        if (bundle.getString("multiple.choice").equals(typeComboBox.getValue())) {
            int nonEmptyAnswers = (int) answersBox.getChildren().stream()
                    .filter(node -> node instanceof TextField)
                    .map(node -> ((TextField) node).getText().trim())
                    .filter(text -> !text.isEmpty())
                    .count();
            ComboBox<String> correctAnswerComboBox = (ComboBox<String>) answersBox.getChildren().get(4);
            isValid = isValid && nonEmptyAnswers >= 4 && correctAnswerComboBox.getValue() != null;
        } else {
            TextField answerField = (TextField) answersBox.getChildren().get(0);
            isValid = isValid && !answerField.getText().trim().isEmpty();
        }

        saveButton.setDisable(!isValid);
    }

    @FXML
    private void addNewQuestion() {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("add.new.question.title"));
        dialog.setHeaderText(bundle.getString("add.new.question.header"));

        ButtonType saveButtonType = new ButtonType(bundle.getString("save.button"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 20, 10, 20));

        TextField questionField = new TextField();
        questionField.setPromptText(bundle.getString("enter.question.prompt"));

        ComboBox<String> typeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                bundle.getString("multiple.choice"),
                bundle.getString("identification")
        ));
        typeComboBox.setPromptText(bundle.getString("select.question.type"));

        VBox answersBox = new VBox(5);
        Label answersLabel = new  Label(bundle.getString("answers.label"));

        content.getChildren().addAll(
                new Label(bundle.getString("question.label")),
                questionField,
                new Label(bundle.getString("type.label")),
                typeComboBox,
                answersLabel,
                answersBox
        );

        typeComboBox.setOnAction(e -> updateAnswersBox(answersBox, typeComboBox.getValue(), null));

        scrollPane.setContent(content);
        dialog.getDialogPane().setContent(scrollPane);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        questionField.textProperty().addListener((observable, oldValue, newValue) ->
                validateEditForm(saveButton, questionField, answersBox, typeComboBox));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String questionText = questionField.getText();
                String type = typeComboBox.getValue().equals(bundle.getString("multiple.choice")) ? "MULTIPLE_CHOICE" : "IDENTIFICATION";
                List<String> answers;
                String correctAnswer;

                if (type.equals("MULTIPLE_CHOICE")) {
                    answers = answersBox.getChildren().stream()
                            .filter(node -> node instanceof TextField)
                            .map(node -> ((TextField) node).getText())
                            .collect(Collectors.toList());
                    ComboBox<String> correctAnswerComboBox = (ComboBox<String>) answersBox.getChildren().get(4);
                    String selectedAnswer = correctAnswerComboBox.getValue();
                    int correctAnswerIndex = Integer.parseInt(selectedAnswer.split(" ")[1]) - 1;
                    correctAnswer = answers.get(correctAnswerIndex);
                } else {
                    correctAnswer = ((TextField) answersBox.getChildren().get(0)).getText();
                    answers = List.of(correctAnswer);
                }

                return new Question(questionText, answers, correctAnswer, type);
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(question -> {
            currentQuiz.addQuestion(question);
            QuestionDatabase.addQuestion(question, currentQuiz.getId());
            loadQuestions();
        });
    }

    private void deleteQuestion(Question question) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString("delete.question.title"));
        alert.setHeaderText(bundle.getString("delete.question.header"));
        alert.setContentText(bundle.getString("delete.question.content"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            currentQuiz.removeQuestion(question);
            QuestionDatabase.deleteQuestion(question.getId());
            loadQuestions();
        }
    }

    @FXML
    private void backToManageQuizzes() throws Exception {
        DreiMain.showManageQuizzesView();
    }
}