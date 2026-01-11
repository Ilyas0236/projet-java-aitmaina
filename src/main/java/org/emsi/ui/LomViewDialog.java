package org.emsi.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import org.emsi.entities.LomSchema;
import org.emsi.ui.controllers.LomViewController;

import java.io.IOException;

/**
 * Dialogue de visualisation des métadonnées LOM
 * Affichage en lecture seule de toutes les métadonnées
 */
public class LomViewDialog extends Dialog<Void> {

    public LomViewDialog(LomSchema lom) {
        this.setTitle("Détails de la ressource");
        this.setHeaderText("📚 " + (lom != null ? lom.getResourceTitle() : ""));

        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lom_view_dialog.fxml"));
            TabPane tabPane = loader.load();
            getDialogPane().setContent(tabPane);

            LomViewController controller = loader.getController();
            controller.setLom(lom);

        } catch (IOException e) {
            e.printStackTrace();
            // Afficher un message d'erreur à l'utilisateur
            VBox errorBox = new VBox(10);
            errorBox.getChildren().addAll(
                    new Label("❌ Erreur de chargement du dialogue:"),
                    new Label(e.getMessage() != null ? e.getMessage() : "Erreur inconnue"));
            getDialogPane().setContent(errorBox);
        } catch (Exception e) {
            e.printStackTrace();
            // Capturer toute autre exception
            VBox errorBox = new VBox(10);
            errorBox.getChildren().addAll(
                    new Label("❌ Erreur inattendue:"),
                    new Label(e.getClass().getSimpleName() + ": " + e.getMessage()));
            getDialogPane().setContent(errorBox);
        }
    }
}
