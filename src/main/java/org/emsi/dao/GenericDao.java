package org.emsi.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.List;

/**
 * DAO générique avec les opérations CRUD de base
 * 
 * @param <T>  Type de l'entité
 * @param <ID> Type de l'identifiant
 */
public class GenericDao<T, ID extends Serializable> {

    private final Class<T> entityClass;

    public GenericDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Sauvegarder une nouvelle entité
     */
    public ID save(T entity) {
        Transaction transaction = null;
        ID id = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            @SuppressWarnings("unchecked")
            ID tempId = (ID) session.save(entity);
            id = tempId;
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return id;
    }

    /**
     * Mettre à jour une entité existante
     */
    public void update(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    /**
     * Sauvegarder ou mettre à jour une entité
     */
    public void saveOrUpdate(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    /**
     * Supprimer une entité
     */
    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.delete(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    /**
     * Supprimer par ID
     */
    public void deleteById(ID id) {
        T entity = findById(id);
        if (entity != null) {
            delete(entity);
        }
    }

    /**
     * Trouver par ID
     */
    public T findById(ID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(entityClass, id);
        }
    }

    /**
     * Trouver toutes les entités
     */
    public List<T> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<T> query = session.createQuery("FROM " + entityClass.getSimpleName(), entityClass);
            return query.list();
        }
    }

    /**
     * Compter le nombre d'entités
     */
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(*) FROM " + entityClass.getSimpleName(), Long.class);
            return query.uniqueResult();
        }
    }

    /**
     * Rechercher avec une requête HQL personnalisée
     */
    protected List<T> findByQuery(String hql, Object... params) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i, params[i]);
            }
            return query.list();
        }
    }

    /**
     * Rechercher un résultat unique avec une requête HQL
     */
    protected T findSingleByQuery(String hql, Object... params) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i, params[i]);
            }
            return query.uniqueResult();
        }
    }
}
