package org.emsi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.emsi.dao.HibernateUtil;
import org.emsi.service.AuthService;
import org.emsi.ui.AdminDashboard;
import org.emsi.ui.UserDashboard;
import org.emsi.ui.controllers.LoginController;

import java.io.IOException;

/**
 * Application principale JavaFX
 * Point d'entrée de l'application LOM 1.0
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private AuthService authService;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.authService = AuthService.getInstance();

        // Initialiser les utilisateurs par défaut
        try {
            authService.initDefaultUsers();
        } catch (Exception e) {
            showError("Erreur de connexion à la base de données",
                    "Assurez-vous que MySQL est démarré via Docker.\n" + e.getMessage());
        }

        // Afficher l'écran de connexion
        showLoginScreen();
    }

    /**
     * Afficher l'écran de connexion
     */
    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setMainApp(this);

            Scene scene = new Scene(root, 600, 500);
            primaryStage.setTitle("LOM 1.0 - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur UI", "Impossible de charger l'interface de connexion.");
        }
    }

    /**
     * Afficher le dialogue d'inscription
     */
    public void showSignUpDialog() {
        org.emsi.ui.SignUpDialog dialog = new org.emsi.ui.SignUpDialog();
        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Inscription réussie");
                alert.setHeaderText(null);
                alert.setContentText("Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.");
                alert.showAndWait();
            }
        });
    }

    /**
     * Afficher le tableau de bord approprié selon le rôle
     */
    public void showDashboard() {
        if (authService.isAdmin()) {
            showAdminDashboard();
        } else {
            showUserDashboard();
        }
    }

    /**
     * Afficher le tableau de bord administrateur
     */
    private void showAdminDashboard() {
        AdminDashboard adminDashboard = new AdminDashboard(primaryStage, this::showLoginScreen);
        Scene scene = new Scene(adminDashboard.getView(), 1200, 800);
        primaryStage.setTitle("LOM 1.0 - Administration");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
    }

    /**
     * Afficher le tableau de bord utilisateur
     */
    private void showUserDashboard() {
        UserDashboard userDashboard = new UserDashboard(primaryStage, this::showLoginScreen);
        Scene scene = new Scene(userDashboard.getView(), 1000, 700);
        primaryStage.setTitle("LOM 1.0 - Consultation");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
    }

    /**
     * Afficher une erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        // Fermer la SessionFactory Hibernate
        HibernateUtil.shutdown();
        System.out.println("Application fermée");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
