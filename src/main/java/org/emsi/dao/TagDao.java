package org.emsi.dao;

import org.emsi.entities.Tag;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO pour l'entité Tag
 */
public class TagDao extends GenericDao<Tag, Long> {

    public TagDao() {
        super(Tag.class);
    }

    /**
     * Trouver un tag par nom
     */
    public Tag findByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Tag WHERE name = :name";
            Query<Tag> query = session.createQuery(hql, Tag.class);
            query.setParameter("name", name);
            return query.uniqueResult();
        }
    }

    /**
     * Rechercher des tags par nom (LIKE)
     */
    public List<Tag> searchByName(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT t FROM Tag t LEFT JOIN FETCH t.resources WHERE t.name LIKE :keyword ORDER BY t.name";
            Query<Tag> query = session.createQuery(hql, Tag.class);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.list();
        }
    }

    /**
     * Obtenir tous les tags triés par nom
     */
    @Override
    public List<Tag> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Utilisation de LEFT JOIN FETCH pour charger les ressources (évite
            // LazyInitException dans l'UI)
            String hql = "SELECT DISTINCT t FROM Tag t LEFT JOIN FETCH t.resources ORDER BY t.name";
            Query<Tag> query = session.createQuery(hql, Tag.class);
            return query.list();
        }
    }

    /**
     * Vérifier si un tag existe
     */
    public boolean exists(String name) {
        return findByName(name) != null;
    }
}
