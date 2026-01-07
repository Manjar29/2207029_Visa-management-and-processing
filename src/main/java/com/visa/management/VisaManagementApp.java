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
        Scene scene = new Scene(fxmlLoader.load());
        
        // Add stylesheet
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        primaryStage.setTitle("Visa Management & Processing System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.setMaximized(true);  // Open maximized to full screen
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void changeScene(String fxmlPath, String title) {
        try {
            boolean wasMaximized = primaryStage.isMaximized();
            double currentWidth = primaryStage.getWidth();
            double currentHeight = primaryStage.getHeight();
            
            FXMLLoader fxmlLoader = new FXMLLoader(VisaManagementApp.class.getResource(fxmlPath));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(VisaManagementApp.class.getResource("/css/styles.css").toExternalForm());
            
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            
            // Maintain window state
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
                primaryStage.centerOnScreen();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T changeSceneWithController(String fxmlPath, String title) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         CHANGING SCENE WITH CONTROLLER                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("FXML Path: " + fxmlPath);
        System.out.println("Title: " + title);
        
        try {
            boolean wasMaximized = primaryStage.isMaximized();
            double currentWidth = primaryStage.getWidth();
            double currentHeight = primaryStage.getHeight();
            
            System.out.println("Loading FXML...");
            
            FXMLLoader fxmlLoader = new FXMLLoader(VisaManagementApp.class.getResource(fxmlPath));
            System.out.println("FXML Loader created");
            
            Scene scene = new Scene(fxmlLoader.load());
            System.out.println("Scene loaded");
            
            scene.getStylesheets().add(VisaManagementApp.class.getResource("/css/styles.css").toExternalForm());
            System.out.println("Stylesheet added");
            
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            
            // Maintain window state
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
                primaryStage.centerOnScreen();
            }
            
            System.out.println("Scene set to stage");
            
            T controller = fxmlLoader.getController();
            System.out.println("Controller retrieved: " + (controller != null ? controller.getClass().getSimpleName() : "NULL"));
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");
            
            return controller;
        } catch (IOException e) {
            System.err.println("!!! FATAL ERROR LOADING SCENE !!!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("!!! UNEXPECTED ERROR !!!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
