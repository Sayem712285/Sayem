package com.vox.drei;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class DreiMain extends Application {

    private static final Logger LOGGER = Logger.getLogger(DreiMain.class.getName());
    private static Stage primaryStage;
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 800;
    private static StackPane root;
    private static Canvas backgroundCanvas;
    private static List<Particle> particles;
    private static final Random random = new Random();
    private static final Preferences prefs = Preferences.userNodeForPackage(DreiMain.class);

    private static ResourceBundle bundle;
    private static Locale currentLocale;

    @Override
    public void start(Stage primaryStage) throws Exception {
        DreiMain.primaryStage = primaryStage;

        // Initialize locale
        String savedLanguage = prefs.get("language", "en");
        currentLocale = new Locale(savedLanguage);
        updateBundle();

        root = new StackPane();
        backgroundCanvas = new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        root.getChildren().add(backgroundCanvas);

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle(bundle.getString("app.title"));

        // Set application icon
        try {
            primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load application icon", e);
        }

        initializeParticles();
        startBackgroundAnimation();

        showMainView();
        primaryStage.show();
    }

    public static void setLanguage(String languageCode) {
        currentLocale = new Locale(languageCode);
        updateBundle();
        prefs.put("language", languageCode);
    }

    private static void updateBundle() {
        try {
            bundle = ResourceBundle.getBundle("messages", currentLocale);
        } catch (MissingResourceException e) {
            LOGGER.log(Level.SEVERE, "Failed to load resource bundle for locale: " + currentLocale, e);
            bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        }
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static void showScoreView(int score, int totalQuestions, List<Question> questions, String quizName) throws Exception {
        FXMLLoader loader = new FXMLLoader(DreiMain.class.getResource("ScoreView.fxml"), bundle);
        Parent scoreView = loader.load();
        ScoreController controller = loader.getController();
        controller.setScore(score, totalQuestions);
        controller.setQuestions(questions);
        controller.setQuizName(quizName);
        setView(scoreView);
    }

    private static void initializeParticles() {
        particles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle());
        }
    }

    public static void showAboutView() throws Exception {
        FXMLLoader loader = new FXMLLoader(DreiMain.class.getResource("about-view.fxml"), bundle);
        Parent aboutView = loader.load();
        setView(aboutView);
    }

    private static void startBackgroundAnimation() {
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (prefs.getBoolean("animationEnabled", true)) {
                    gc.setFill(Color.rgb(240, 248, 255, 0.3)); // Light blue background with some transparency
                    gc.fillRect(0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());

                    for (Particle p : particles) {
                        p.update();
                        p.draw(gc);
                    }
                } else {
                    gc.setFill(Color.rgb(240, 248, 255, 1)); // Solid light blue background
                    gc.fillRect(0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
                }
            }
        }.start();
    }

    public static void showMainView() throws Exception {
        FXMLLoader loader = new FXMLLoader(DreiMain.class.getResource("drei-main.fxml"), bundle);
        Parent mainView = loader.load();
        setView(mainView);
        primaryStage.setTitle(bundle.getString("app.title"));
    }

    public static void showQuizSelectionView() throws Exception {
        FXMLLoader loader = new FXMLLoader(DreiMain.class.getResource("QuizSelectionView.fxml"), bundle);
        Parent quizSelectionView = loader.load();
        setView(quizSelectionView);
    }

    public static void showManageQuizzesView() throws Exception {
        FXMLLoader loader = new FXMLLoader(DreiMain.class.getResource("ManageQuizzesView.fxml"), bundle);
        Parent manageQuizzesView = loader.load();
        setView(manageQuizzesView);
    }

    public static void showManageQuestionsView(Quiz quiz) throws Exception {
        FXMLLoader loader = new FXMLLoader(DreiMain.class.getResource("ManageQuestionsView.fxml"), bundle);
        Parent manageQuestionsView = loader.load();
        ManageQuestionsController controller = loader.getController();
        controller.setQuiz(quiz);
        setView(manageQuestionsView);
    }

    public static void showQuizGameView() throws Exception {
        FXMLLoader loader = new FXMLLoader(DreiMain.class.getResource("QuizGameView.fxml"),  bundle);
        Parent quizGameView = loader.load();
        setView(quizGameView);
    }

    public static void showQuizSettingsView() throws Exception {
        FXMLLoader loader = new FXMLLoader(DreiMain.class.getResource("QuizSettingsView.fxml"), bundle);
        Parent quizSettingsView = loader.load();
        setView(quizSettingsView);
    }

    private static void setView(Parent view) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root.getChildren().get(root.getChildren().size() - 1));
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            root.getChildren().remove(root.getChildren().size() - 1);
            root.getChildren().add(view);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), view);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class Particle {
        private double x, y;
        private double speed;
        private double size;
        private Color color;

        public Particle() {
            reset();
        }

        private void reset() {
            x = random.nextDouble() * DEFAULT_WIDTH;
            y = random.nextDouble() * DEFAULT_HEIGHT;
            speed = 0.5 + random.nextDouble() * 1.5;
            size = 1 + random.nextDouble() * 3;
            color = Color.rgb(255, 255, 255, 0.5 + random.nextDouble() * 0.5);
        }

        public void update() {
            y += speed;
            if (y > DEFAULT_HEIGHT) {
                reset();
                y = 0;
            }
        }

        public void draw(GraphicsContext gc) {
            gc.setFill(color);
            gc.fillOval(x, y, size, size);
        }
    }
}