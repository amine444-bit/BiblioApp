package com.bibliotheque.controller;

import com.bibliotheque.MainApp;
import com.bibliotheque.dao.UserDAO;
import com.bibliotheque.model.User;
import com.bibliotheque.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        txtPassword.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin();
        });
        txtUsername.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) txtPassword.requestFocus();
        });
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        User user = userDAO.authenticate(username, password);
        if (user != null) {
            SessionManager.setCurrentUser(user);
            if (user.isAdmin()) {
                MainApp.loadScene("admin_dashboard");
            } else {
                MainApp.loadScene("member_dashboard");
            }
        } else {
            showError("Identifiants incorrects. Veuillez réessayer.");
            txtPassword.clear();
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }
}
