package org.emsi.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.emsi.ui.controllers.UserDashboardController;

import java.io.IOException;

/**
 * Tableau de bord Utilisateur
 * Consultation et recherche des ressources pédagogiques
 */
public class UserDashboard {

    private BorderPane mainLayout;

    public UserDashboard(Stage stage, Runnable onLogout) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_dashboard.fxml"));
            mainLayout = loader.load();
            UserDashboardController controller = loader.getController();
            controller.setContext(stage, onLogout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BorderPane getView() {
        return mainLayout;
    }
}
