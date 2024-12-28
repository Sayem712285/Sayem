package com.vox.drei;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.awt.Desktop;
import java.net.URI;
import java.util.ResourceBundle;

public class AboutController {

    @FXML
    private Label versionLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private VBox rootVBox;

    @FXML
    private Button instagramButton;

    @FXML
    private Button facebookButton;

    @FXML
    private Button linkedinButton; // Added LinkedIn button

    @FXML
    private Button githubButton;

    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        bundle = DreiMain.getBundle();

        // Update the labels
        versionLabel.setText(bundle.getString("version.label"));
        descriptionLabel.setText("Created By: Sayem");

        // Add fade-in animation
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), rootVBox);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();

        // Add scale animation for the logo
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1), rootVBox.getChildren().get(0));
        scaleTransition.setFromX(0.5);
        scaleTransition.setFromY(0.5);
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        scaleTransition.play();
    }

    @FXML
    private void openSocialMedia(javafx.event.ActionEvent event) {
        String url = "";
        if (event.getSource() == instagramButton) {
            url = "https://www.instagram.com/allfazsayem/";
        } else if (event.getSource() == facebookButton) {
            url = "https://www.facebook.com/mohammad.sayem.uddin.573814";
        } else if (event.getSource() == linkedinButton) {
            url = "https://www.linkedin.com/in/sayem-uddin-alfaz-1bb08a2a1/";
        } else if (event.getSource() == githubButton) {
            url = "https://github.com/Sayem712285";
        }

        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void backToMain() throws Exception {
        DreiMain.showMainView();
    }
}
