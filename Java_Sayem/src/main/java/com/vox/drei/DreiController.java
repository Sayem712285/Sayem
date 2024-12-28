package com.vox.drei;

import javafx.fxml.FXML;

public class DreiController {

    @FXML
    private void startQuiz() throws Exception {
        DreiMain.showQuizSelectionView();
    }

    @FXML
    private void manageQuizzes() throws Exception {
        DreiMain.showManageQuizzesView();
    }

    @FXML
    private void openSettings() throws Exception {
        DreiMain.showQuizSettingsView();
    }

    @FXML
    private void openAbout() throws Exception {
        DreiMain.showAboutView();
    }

    @FXML
    private void exitGame() {
        System.exit(0);
    }
}