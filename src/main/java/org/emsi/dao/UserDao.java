package org.emsi.dao;

import org.emsi.entities.User;

/**
 * DAO pour l'entité User
 * Gestion des utilisateurs et authentification
 */
public class UserDao extends GenericDao<User, Long> {

    public UserDao() {
        super(User.class);
    }

    /**
     * Trouver un utilisateur par son nom d'utilisateur
     */
    public User findByUsername(String username) {
        String hql = "FROM User WHERE username = ?0";
        return findSingleByQuery(hql, username);
    }

    /**
     * Authentifier un utilisateur
     */
    public User authenticate(String username, String password) {
        String hql = "FROM User WHERE username = ?0 AND password = ?1";
        return findSingleByQuery(hql, username, password);
    }

    /**
     * Vérifier si un nom d'utilisateur existe
     */
    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    /**
     * Trouver tous les administrateurs
     */
    public java.util.List<User> findAdmins() {
        String hql = "FROM User WHERE role = ?0";
        return findByQuery(hql, "ADMIN");
    }

    /**
     * Trouver tous les utilisateurs standards
     */
    public java.util.List<User> findUsers() {
        String hql = "FROM User WHERE role = ?0";
        return findByQuery(hql, "USER");
    }
}
