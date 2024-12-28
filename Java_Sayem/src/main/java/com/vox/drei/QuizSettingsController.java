package com.vox.drei;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.application.Platform;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import java.util.Locale;

public class QuizSettingsController {

    @FXML private TextField numQuestionsField;
    @FXML private TextField timePerQuestionField;
    @FXML private CheckBox timerEnabledCheckBox;
    @FXML private Label notificationLabel;
    @FXML private CheckBox animationEnabledCheckBox;
    @FXML private CheckBox immediateAnswerCheckBox;
    @FXML private ComboBox<String> languageComboBox;
    private ResourceBundle bundle;

    private final Preferences prefs = Preferences.userNodeForPackage(QuizSettingsController.class);
    private String currentLanguage;

    private Map<String, String> languageMap;

    @FXML
    public void initialize() {
        // New: Initialize the language map
        languageMap = new LinkedHashMap<>();
        languageMap.put("en", "English");
        languageMap.put("ja", "日本語");
        languageMap.put("fil", "Filipino");
        languageMap.put("ko", "한국어");
        languageMap.put("zh", "中文");
        languageMap.put("de", "Deutsch");
        languageMap.put("ru", "Русский");
        languageMap.put("es", "Español");

        currentLanguage = prefs.get("language", "en");
        updateBundle(currentLanguage);

        numQuestionsField.setText(String.valueOf(prefs.getInt("numQuestions", 5)));
        timePerQuestionField.setText(String.valueOf(prefs.getInt("timePerQuestion", 15)));
        timerEnabledCheckBox.setSelected(prefs.getBoolean("timerEnabled", true));
        animationEnabledCheckBox.setSelected(prefs.getBoolean("animationEnabled", true));
        immediateAnswerCheckBox.setSelected(prefs.getBoolean("immediateAnswerEnabled", false));

        // Modified: Update the language combo box
        languageComboBox.setItems(FXCollections.observableArrayList(languageMap.values()));
        languageComboBox.setValue(languageMap.get(currentLanguage));

        updateLabels();

        // Check if there's a saved notification flag
        boolean showSavedNotification = prefs.getBoolean("showSavedNotification", false);
        if (showSavedNotification) {
            showNotification(bundle.getString("settings.saved"));
            prefs.putBoolean("showSavedNotification", false);
        }
    }

    private void updateBundle(String language) {
        bundle = ResourceBundle.getBundle("messages", new Locale(language));
        DreiMain.setLanguage(language);
    }

    private void updateLabels() {
        timerEnabledCheckBox.setText(bundle.getString("enable.timer"));
        animationEnabledCheckBox.setText(bundle.getString("enable.animation"));
        immediateAnswerCheckBox.setText(bundle.getString("immediate.answer"));
        // Update other UI elements...
    }

    @FXML
    private void saveSettings() {
        try {
            int numQuestions = Integer.parseInt(numQuestionsField.getText());
            int timePerQuestion = Integer.parseInt(timePerQuestionField.getText());
            boolean timerEnabled = timerEnabledCheckBox.isSelected();
            boolean animationEnabled = animationEnabledCheckBox.isSelected();
            boolean immediateAnswerEnabled = immediateAnswerCheckBox.isSelected();

            prefs.putInt("numQuestions", numQuestions);
            prefs.putInt("timePerQuestion", timePerQuestion);
            prefs.putBoolean("timerEnabled", timerEnabled);
            prefs.putBoolean("animationEnabled", animationEnabled);
            prefs.putBoolean("immediateAnswerEnabled", immediateAnswerEnabled);

            // Modified: Get the language code from the selected full name
            String selectedLanguage = languageComboBox.getValue();
            String localeCode = languageMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(selectedLanguage))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("en");

            // Save the language code to preferences
            prefs.put("language", localeCode);

            // Apply language change and refresh the page
            if (!localeCode.equals(currentLanguage)) {
                currentLanguage = localeCode;
                updateBundle(currentLanguage);
                prefs.putBoolean("showSavedNotification", true);
                Platform.runLater(this::refreshPage);
            } else {
                showNotification(bundle.getString("settings.saved"));
            }
        } catch (NumberFormatException e) {
            showNotification(bundle.getString("invalid.input"));
        }
    }

    private void refreshPage() {
        try {
            DreiMain.showQuizSettingsView();
        } catch (Exception e) {
            e.printStackTrace();
            showNotification(bundle.getString("refresh.error"));
        }
    }

    private void showNotification(String message) {
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);
    }

    @FXML
    private void backToMain() throws Exception {
        DreiMain.showMainView();
    }
}