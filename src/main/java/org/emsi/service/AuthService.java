package org.emsi.service;

import org.emsi.dao.UserDao;
import org.emsi.entities.User;

/**
 * Service d'authentification
 * Gère la connexion et la session utilisateur
 */
public class AuthService {

    private static AuthService instance;
    private final UserDao userDao;
    private User currentUser;

    private AuthService() {
        this.userDao = new UserDao();
    }

    /**
     * Obtenir l'instance unique (Singleton)
     */
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Authentifier un utilisateur
     * 
     * @return true si l'authentification réussit
     */
    public boolean login(String username, String password) {
        User user = userDao.authenticate(username, password);
        if (user != null) {
            this.currentUser = user;
            System.out.println("✅ Connexion réussie: " + user.getUsername() + " (" + user.getRole() + ")");
            return true;
        }
        System.out.println("❌ Échec de connexion pour: " + username);
        return false;
    }

    /**
     * Déconnecter l'utilisateur
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("Déconnexion de: " + currentUser.getUsername());
            currentUser = null;
        }
    }

    /**
     * Vérifier si un utilisateur est connecté
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Vérifier si l'utilisateur connecté est admin
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Obtenir l'utilisateur connecté
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Créer un nouvel utilisateur (admin uniquement)
     */
    public boolean createUser(String username, String password, String role, String email, String fullName) {
        if (!isAdmin()) {
            System.out.println("❌ Création d'utilisateur refusée: droits insuffisants");
            return false;
        }

        if (userDao.usernameExists(username)) {
            System.out.println("❌ Le nom d'utilisateur existe déjà: " + username);
            return false;
        }

        User newUser = new User(username, password, role);
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        userDao.save(newUser);

        System.out.println("✅ Utilisateur créé: " + username);
        return true;
    }

    /**
     * Inscription d'un nouvel utilisateur (Self-service)
     */
    public boolean register(String username, String password, String email, String fullName) {
        if (userDao.usernameExists(username)) {
            System.out.println("❌ Le nom d'utilisateur existe déjà: " + username);
            return false;
        }

        // Par défaut, l'inscription crée un utilisateur standard (USER)
        User newUser = new User(username, password, "USER");
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        userDao.save(newUser);

        System.out.println("✅ Inscription réussie: " + username);
        return true;
    }

    /**
     * Initialiser les utilisateurs par défaut
     */
    public void initDefaultUsers() {
        if (!userDao.usernameExists("admin")) {
            User admin = new User("admin", "admin", "ADMIN");
            admin.setEmail("admin@emsi.ma");
            admin.setFullName("Administrateur");
            userDao.save(admin);
            System.out.println("✅ Utilisateur admin créé");
        }

        if (!userDao.usernameExists("user")) {
            User user = new User("user", "user", "USER");
            user.setEmail("user@emsi.ma");
            user.setFullName("Utilisateur Standard");
            userDao.save(user);
            System.out.println("✅ Utilisateur standard créé");
        }
    }
}
