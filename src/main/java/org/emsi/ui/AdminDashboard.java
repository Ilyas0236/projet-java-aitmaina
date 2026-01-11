package org.emsi.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.emsi.ui.controllers.AdminDashboardController;

import java.io.IOException;

/**
 * Tableau de bord Administrateur
 * Gestion complète des ressources et métadonnées LOM
 */
public class AdminDashboard {

    private BorderPane mainLayout;

    public AdminDashboard(Stage stage, Runnable onLogout) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
            mainLayout = loader.load();
            AdminDashboardController controller = loader.getController();
            controller.setContext(stage, onLogout);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error appropriately, maybe show an alert or log
        }
    }

    public BorderPane getView() {
        return mainLayout;
    }
}
