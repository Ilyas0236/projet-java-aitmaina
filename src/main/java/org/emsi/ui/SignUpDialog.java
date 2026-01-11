package org.emsi.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Pane;
import org.emsi.ui.controllers.SignUpController;

import java.io.IOException;

/**
 * Dialogue d'inscription
 */
public class SignUpDialog extends Dialog<Boolean> {

    public SignUpDialog() {
        setTitle("Inscription");
        setHeaderText("Créer un nouveau compte");

        ButtonType registerButtonType = new ButtonType("S'inscrire", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup_dialog.fxml"));
            Pane content = loader.load();
            getDialogPane().setContent(content);

            SignUpController controller = loader.getController();

            final Button registerButton = (Button) getDialogPane().lookupButton(registerButtonType);
            registerButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!controller.validateAndRegister()) {
                    event.consume();
                }
            });

            setResultConverter(dialogButton -> {
                if (dialogButton == registerButtonType) {
                    return true;
                }
                return null;
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
