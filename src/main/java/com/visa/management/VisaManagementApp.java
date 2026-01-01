package com.visa.management;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main Application Entry Point for Visa Management System
 */
public class VisaManagementApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        
        // Load the home/dashboard screen
        FXMLLoader fxmlLoader = new FXMLLoader(VisaManagementApp.class.getResource("/fxml/home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        
        // Add stylesheet
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        primaryStage.setTitle("Visa Management & Processing System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setMaximized(true);  // Open maximized to full screen
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void changeScene(String fxmlPath, String title) {
        try {
            boolean wasMaximized = primaryStage.isMaximized();
            FXMLLoader fxmlLoader = new FXMLLoader(VisaManagementApp.class.getResource(fxmlPath));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(VisaManagementApp.class.getResource("/css/styles.css").toExternalForm());
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T changeSceneWithController(String fxmlPath, String title) {
        try {
            boolean wasMaximized = primaryStage.isMaximized();
            FXMLLoader fxmlLoader = new FXMLLoader(VisaManagementApp.class.getResource(fxmlPath));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(VisaManagementApp.class.getResource("/css/styles.css").toExternalForm());
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            }
            return fxmlLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
