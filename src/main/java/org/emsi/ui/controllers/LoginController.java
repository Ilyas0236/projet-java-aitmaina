package org.emsi.ui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.emsi.MainApp;
import org.emsi.service.AuthService;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink signUpLink;

    private MainApp mainApp;
    private AuthService authService;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.authService = AuthService.getInstance();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            return;
        }

        if (authService.login(username, password)) {
            if (mainApp != null) {
                mainApp.showDashboard();
            }
        } else {
            errorLabel.setText("Nom d'utilisateur ou mot de passe incorrect");
            passwordField.clear();
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showSignUpDialog();
        }
    }
}
