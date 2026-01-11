package org.emsi.dao;

import org.emsi.entities.LomSchema;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO pour l'entité LomSchema
 * Gestion des ressources pédagogiques LOM
 */
public class LomSchemaDao extends GenericDao<LomSchema, Long> {

    public LomSchemaDao() {
        super(LomSchema.class);
    }

    /**
     * Obtenir toutes les ressources avec leurs relations principales (Optimisation
     * N+1)
     */
    @Override
    public List<LomSchema> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT ls FROM LomSchema ls " +
                    "LEFT JOIN FETCH ls.general " +
                    "LEFT JOIN FETCH ls.educational " +
                    "LEFT JOIN FETCH ls.technical " +
                    "LEFT JOIN FETCH ls.rights " +
                    "LEFT JOIN FETCH ls.lifecycle " +
                    "LEFT JOIN FETCH ls.metaMetadata " +
                    "ORDER BY ls.resourceTitle";
            Query<LomSchema> query = session.createQuery(hql, LomSchema.class);
            return query.list();
        }
    }

    /**
     * Rechercher par titre
     */
    public List<LomSchema> findByTitle(String title) {
        String hql = "FROM LomSchema WHERE resourceTitle LIKE ?0";
        return findByQuery(hql, "%" + title + "%");
    }

    /**
     * Rechercher par mot-clé (dans General)
     */
    public List<LomSchema> findByKeyword(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT ls FROM LomSchema ls " +
                    "LEFT JOIN FETCH ls.general g " +
                    "WHERE g.keyword LIKE :keyword";
            Query<LomSchema> query = session.createQuery(hql, LomSchema.class);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.list();
        }
    }

    /**
     * Rechercher par langue
     */
    public List<LomSchema> findByLanguage(String language) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT ls FROM LomSchema ls " +
                    "LEFT JOIN FETCH ls.general g " +
                    "WHERE g.language = :language";
            Query<LomSchema> query = session.createQuery(hql, LomSchema.class);
            query.setParameter("language", language);
            return query.list();
        }
    }

    /**
     * Rechercher par difficulté
     */
    public List<LomSchema> findByDifficulty(Integer difficulty) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT ls FROM LomSchema ls " +
                    "LEFT JOIN FETCH ls.educational e " +
                    "WHERE e.difficulty = :difficulty";
            Query<LomSchema> query = session.createQuery(hql, LomSchema.class);
            query.setParameter("difficulty", difficulty);
            return query.list();
        }
    }

    /**
     * Recherche multicritères
     */
    public List<LomSchema> search(String title, String keyword, String language, Integer difficulty) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT DISTINCT ls FROM LomSchema ls ");
            hql.append("LEFT JOIN FETCH ls.general g ");
            hql.append("LEFT JOIN FETCH ls.educational e ");
            hql.append("WHERE 1=1 ");

            if (title != null && !title.isEmpty()) {
                hql.append("AND ls.resourceTitle LIKE :title ");
            }
            if (keyword != null && !keyword.isEmpty()) {
                hql.append("AND g.keyword LIKE :keyword ");
            }
            if (language != null && !language.isEmpty()) {
                hql.append("AND g.language = :language ");
            }
            if (difficulty != null) {
                hql.append("AND e.difficulty = :difficulty ");
            }

            Query<LomSchema> query = session.createQuery(hql.toString(), LomSchema.class);

            if (title != null && !title.isEmpty()) {
                query.setParameter("title", "%" + title + "%");
            }
            if (keyword != null && !keyword.isEmpty()) {
                query.setParameter("keyword", "%" + keyword + "%");
            }
            if (language != null && !language.isEmpty()) {
                query.setParameter("language", language);
            }
            if (difficulty != null) {
                query.setParameter("difficulty", difficulty);
            }

            return query.list();
        }
    }

    /**
     * Recherche multicritères avancée (OR entre titre et mot-clé, AND pour le
     * reste)
     */
    public List<LomSchema> searchByCriteria(String queryStr, String language, Integer difficulty) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT DISTINCT ls FROM LomSchema ls ");
            hql.append("LEFT JOIN FETCH ls.general g ");
            hql.append("LEFT JOIN FETCH ls.educational e ");
            hql.append("WHERE 1=1 ");

            if (queryStr != null && !queryStr.trim().isEmpty()) {
                hql.append("AND (ls.resourceTitle LIKE :query OR g.keyword LIKE :query) ");
            }
            if (language != null && !language.isEmpty()) {
                hql.append("AND g.language = :language ");
            }
            if (difficulty != null) {
                hql.append("AND e.difficulty = :difficulty ");
            }

            Query<LomSchema> query = session.createQuery(hql.toString(), LomSchema.class);

            if (queryStr != null && !queryStr.trim().isEmpty()) {
                query.setParameter("query", "%" + queryStr + "%");
            }
            if (language != null && !language.isEmpty()) {
                query.setParameter("language", language);
            }
            if (difficulty != null) {
                query.setParameter("difficulty", difficulty);
            }

            return query.list();
        }
    }

    /**
     * Charger un LomSchema avec toutes ses relations
     */
    public LomSchema findByIdWithRelations(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT ls FROM LomSchema ls " +
                    "LEFT JOIN FETCH ls.general " +
                    "LEFT JOIN FETCH ls.lifecycle l " +
                    "LEFT JOIN FETCH l.contributes " +
                    "LEFT JOIN FETCH ls.metaMetadata " +
                    "LEFT JOIN FETCH ls.technical " +
                    "LEFT JOIN FETCH ls.educational " +
                    "LEFT JOIN FETCH ls.rights " +
                    "LEFT JOIN FETCH ls.annotations " +
                    "WHERE ls.id = :id";
            Query<LomSchema> query = session.createQuery(hql, LomSchema.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        }
    }

    /**
     * Trouver les ressources récentes
     */
    public List<LomSchema> findRecent(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM LomSchema ORDER BY createdAt DESC";
            Query<LomSchema> query = session.createQuery(hql, LomSchema.class);
            query.setMaxResults(limit);
            return query.list();
        }
    }
}
