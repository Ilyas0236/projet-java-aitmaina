package org.emsi.dao;

import org.emsi.entities.Classification;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO pour l'entité Classification
 * Gestion des classifications et taxonomies
 */
public class ClassificationDao extends GenericDao<Classification, Long> {

    public ClassificationDao() {
        super(Classification.class);
    }

    /**
     * Trouver les classifications par objectif
     */
    public List<Classification> findByPurpose(String purpose) {
        String hql = "FROM Classification WHERE purpose = ?0";
        return findByQuery(hql, purpose);
    }

    /**
     * Trouver les classifications d'une ressource LOM
     */
    public List<Classification> findByLomSchemaId(Long lomSchemaId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT c FROM Classification c " +
                    "LEFT JOIN FETCH c.taxonPaths " +
                    "WHERE c.lomSchema.id = :lomSchemaId";
            Query<Classification> query = session.createQuery(hql, Classification.class);
            query.setParameter("lomSchemaId", lomSchemaId);
            return query.list();
        }
    }

    /**
     * Rechercher par mot-clé
     */
    public List<Classification> findByKeyword(String keyword) {
        String hql = "FROM Classification WHERE keyword LIKE ?0";
        return findByQuery(hql, "%" + keyword + "%");
    }

    /**
     * Obtenir toutes les classifications avec leurs taxons
     */
    public List<Classification> findAllWithTaxons() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT c FROM Classification c " +
                    "LEFT JOIN FETCH c.taxonPaths tp " +
                    "LEFT JOIN FETCH tp.taxons";
            Query<Classification> query = session.createQuery(hql, Classification.class);
            return query.list();
        }
    }
}
