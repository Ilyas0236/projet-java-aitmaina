package org.emsi.dao;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Classe utilitaire pour la gestion de la SessionFactory Hibernate
 * Pattern Singleton pour garantir une unique instance
 */
public class HibernateUtil {
    private static SessionFactory sessionFactory;

    static {
        try {
            // Charger la configuration depuis hibernate.cfg.xml
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");

            // Construire la SessionFactory
            sessionFactory = configuration.buildSessionFactory();

            System.out.println("✅ SessionFactory Hibernate initialisée avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation de SessionFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Obtenir la SessionFactory
     * 
     * @return SessionFactory unique
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Fermer la SessionFactory
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            System.out.println("SessionFactory fermée");
        }
    }
}
