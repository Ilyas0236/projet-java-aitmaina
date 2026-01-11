package org.emsi.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.emsi.service.AuthService;

public class SignUpController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    private final AuthService authService = AuthService.getInstance();

    public boolean validateAndRegister() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();
        String full = fullNameField.getText();
        String email = emailField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Le nom d'utilisateur et le mot de passe sont obligatoires.");
            return false;
        }

        if (authService.register(user, pass, email, full)) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Ce nom d'utilisateur existe déjà. Veuillez en choisir un autre.");
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
