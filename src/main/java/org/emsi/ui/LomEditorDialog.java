package org.emsi.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TabPane;
import org.emsi.entities.LomSchema;
import org.emsi.ui.controllers.LomEditorController;

import java.io.IOException;

/**
 * Dialogue d'édition des métadonnées LOM
 * Permet de modifier toutes les catégories LOM
 */
public class LomEditorDialog extends Dialog<LomSchema> {

    public LomEditorDialog(LomSchema lom) {
        this.setTitle("Éditer les métadonnées LOM");
        this.setHeaderText("📝 " + (lom != null ? lom.getResourceTitle() : "Nouvelle Ressource"));

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lom_editor_dialog.fxml"));
            TabPane tabPane = loader.load();
            getDialogPane().setContent(tabPane);

            LomEditorController controller = loader.getController();
            controller.loadValues(lom);

            setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    controller.saveValues(lom);
                    return lom;
                }
                return null;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
