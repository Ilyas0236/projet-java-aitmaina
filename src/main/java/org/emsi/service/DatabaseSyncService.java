package org.emsi.service;

import org.emsi.dao.GenericDao;
import org.emsi.dao.HibernateUtil;
import org.emsi.entities.LomSchema;
import org.emsi.exceptions.LomException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Service démontrant Threads + Base de Données
 * 
 * Ce service implémente les concepts de l'Exercice 7:
 * - Accès concurrent à la base de données
 * - Synchronisation des transactions
 * - Thread-safety avec locks
 * - Gestion du pool de connexions Hibernate
 * 
 * @author Projet LOM - EMSI
 */
public class DatabaseSyncService {

    private static DatabaseSyncService instance;

    // LOCK: ReadWriteLock pour gérer les accès lecture/écriture
    // Permet plusieurs lecteurs simultanés mais un seul écrivain
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    // LOCK: ReentrantLock pour les opérations critiques
    private final ReentrantLock criticalLock = new ReentrantLock();

    // ATOMIC: Compteur thread-safe pour les statistiques
    private final AtomicInteger queryCount = new AtomicInteger(0);
    private final AtomicInteger updateCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);

    // EXECUTORSERVICE: Pool de threads pour opérations BDD
    private final ExecutorService dbExecutor;

    // CACHE: Cache thread-safe avec ConcurrentHashMap
    private final ConcurrentHashMap<Long, LomSchema> resourceCache;

    private final GenericDao<LomSchema, Long> lomSchemaDao;

    private DatabaseSyncService() {
        this.lomSchemaDao = new GenericDao<>(LomSchema.class);
        this.resourceCache = new ConcurrentHashMap<>();

        // THREADPOOLEXECUTOR: Pool personnalisé pour les opérations BDD
        // Limite le nombre de connexions simultanées à la BDD
        this.dbExecutor = new ThreadPoolExecutor(
                2, // corePoolSize - 2 threads minimum
                5, // maxPoolSize - 5 threads maximum
                30L, // keepAliveTime
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(50), // File d'attente de 50 tâches
                r -> {
                    Thread t = new Thread(r, "DB-Worker-" + Thread.currentThread().getId());
                    t.setDaemon(true);
                    return t;
                });
    }

    public static synchronized DatabaseSyncService getInstance() {
        if (instance == null) {
            instance = new DatabaseSyncService();
        }
        return instance;
    }

    // =========================================================================
    // MÉTHODES AVEC READWRITELOCK - Exercice 6: Synchronisation
    // =========================================================================

    /**
     * READLOCK: Lecture synchronisée - plusieurs threads peuvent lire
     * 
     * Démontre l'utilisation de ReadWriteLock.readLock()
     * Permet des lectures concurrentes sans bloquer les autres lecteurs
     */
    public LomSchema readResourceSafe(Long id) {
        // READLOCK.LOCK(): Acquérir le verrou de lecture
        readWriteLock.readLock().lock();
        try {
            queryCount.incrementAndGet(); // Compteur atomique

            // Vérifier le cache d'abord (CONCURRENTHASHMAP)
            LomSchema cached = resourceCache.get(id);
            if (cached != null) {
                System.out.println("📖 [" + Thread.currentThread().getName() + "] Cache hit pour ID: " + id);
                return cached;
            }

            // Sinon, lire depuis la BDD
            LomSchema resource = lomSchemaDao.findById(id);
            if (resource != null) {
                resourceCache.put(id, resource); // Mettre en cache
            }

            System.out.println("📖 [" + Thread.currentThread().getName() + "] Lecture BDD ID: " + id);
            return resource;

        } finally {
            // READLOCK.UNLOCK(): Toujours libérer le verrou dans finally
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * WRITELOCK: Écriture synchronisée - un seul thread à la fois
     * 
     * Démontre l'utilisation de ReadWriteLock.writeLock()
     * Bloque tous les autres threads (lecteurs et écrivains)
     */
    public void updateResourceSafe(LomSchema resource) {
        // WRITELOCK.LOCK(): Acquérir le verrou d'écriture (exclusif)
        readWriteLock.writeLock().lock();
        try {
            updateCount.incrementAndGet();

            // Mettre à jour en BDD
            lomSchemaDao.update(resource);

            // Invalider le cache pour cette ressource
            resourceCache.remove(resource.getId());

            System.out.println("✏️ [" + Thread.currentThread().getName() + "] Mise à jour ID: " + resource.getId());

        } finally {
            // WRITELOCK.UNLOCK(): Libérer le verrou d'écriture
            readWriteLock.writeLock().unlock();
        }
    }

    // =========================================================================
    // MÉTHODES AVEC REENTRANTLOCK - Exercice 6: Synchronisation
    // =========================================================================

    /**
     * REENTRANTLOCK: Section critique avec timeout
     * 
     * Démontre l'utilisation de tryLock() avec timeout
     * Évite les deadlocks en limitant le temps d'attente
     */
    public boolean deleteResourceWithTimeout(Long id, long timeoutSeconds) {
        boolean lockAcquired = false;

        try {
            // TRYLOCK: Tenter d'acquérir le verrou avec timeout
            lockAcquired = criticalLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);

            if (lockAcquired) {
                System.out.println("🗑️ [" + Thread.currentThread().getName() + "] Lock acquis, suppression ID: " + id);

                // Supprimer de la BDD
                LomSchema resource = lomSchemaDao.findById(id);
                if (resource != null) {
                    lomSchemaDao.delete(resource);
                    resourceCache.remove(id);
                    updateCount.incrementAndGet();
                    return true;
                }
            } else {
                System.out.println(
                        "⏱️ [" + Thread.currentThread().getName() + "] Timeout: impossible d'acquérir le lock");
                return false;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            errorCount.incrementAndGet();
            return false;
        } finally {
            // Libérer le verrou seulement si on l'a acquis
            if (lockAcquired) {
                criticalLock.unlock();
            }
        }
        return false;
    }

    // =========================================================================
    // MÉTHODES ASYNCHRONES AVEC EXECUTOR - Exercice 5 & 7
    // =========================================================================

    /**
     * CALLABLE + FUTURE: Exécution asynchrone avec résultat
     * 
     * Démontre l'exécution d'une requête BDD dans un thread séparé
     */
    public Future<List<LomSchema>> searchResourcesAsync(String keyword) {
        // SUBMIT CALLABLE: Soumission d'une tâche qui retourne un résultat
        return dbExecutor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("🔍 [" + threadName + "] Recherche async: " + keyword);

            queryCount.incrementAndGet();

            // Simuler une recherche (remplacer par vraie requête HQL)
            List<LomSchema> all = lomSchemaDao.findAll();

            // STREAM: Filtrage avec Stream API
            return all.stream()
                    .filter(r -> r.getResourceTitle() != null)
                    .filter(r -> r.getResourceTitle().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        });
    }

    /**
     * COMPLETABLEFUTURE: Opérations chaînées asynchrones
     * 
     * Démontre les opérations chaînées avec CompletableFuture
     */
    public CompletableFuture<Map<String, Long>> getStatisticsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("📊 [" + Thread.currentThread().getName() + "] Calcul statistiques...");

            List<LomSchema> all = lomSchemaDao.findAll();
            queryCount.incrementAndGet();

            // STREAM + GROUPINGBY: Statistiques par langue
            return all.stream()
                    .filter(r -> r.getGeneral() != null)
                    .filter(r -> r.getGeneral().getLanguage() != null)
                    .collect(Collectors.groupingBy(
                            r -> r.getGeneral().getLanguage(),
                            Collectors.counting()));
        }, dbExecutor);
    }

    /**
     * COUNTDOWNLATCH: Synchronisation de plusieurs requêtes BDD
     * 
     * Exécute plusieurs requêtes en parallèle et attend leur complétion
     */
    public List<LomSchema> loadMultipleResourcesSynced(List<Long> ids, int timeoutSeconds)
            throws InterruptedException {

        // COUNTDOWNLATCH: Compteur pour synchroniser N tâches
        CountDownLatch latch = new CountDownLatch(ids.size());

        // Collection thread-safe pour les résultats
        List<LomSchema> results = new CopyOnWriteArrayList<>();

        // Soumettre une tâche par ID
        for (Long id : ids) {
            dbExecutor.execute(() -> {
                try {
                    LomSchema resource = readResourceSafe(id);
                    if (resource != null) {
                        results.add(resource);
                    }
                } finally {
                    // COUNTDOWN: Signal que cette tâche est terminée
                    latch.countDown();
                }
            });
        }

        // AWAIT: Attendre que toutes les tâches soient terminées
        boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);

        if (!completed) {
            System.out.println("⚠️ Timeout: certaines ressources n'ont pas été chargées");
        }

        return results;
    }

    // =========================================================================
    // MÉTHODES AVEC TRANSACTIONS HIBERNATE - Exercice 7 & 8
    // =========================================================================

    /**
     * TRANSACTION: Opérations multiples dans une transaction
     * 
     * Démontre la gestion manuelle des transactions Hibernate
     * avec commit/rollback approprié
     */
    public void batchUpdateInTransaction(List<LomSchema> resources) throws LomException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            // TRANSACTION.BEGIN: Démarrer la transaction
            transaction = session.beginTransaction();

            System.out.println("🔄 [" + Thread.currentThread().getName() +
                    "] Début transaction batch (" + resources.size() + " ressources)");

            for (int i = 0; i < resources.size(); i++) {
                LomSchema resource = resources.get(i);
                session.update(resource);

                // FLUSH périodique pour éviter OutOfMemory
                if (i % 20 == 0) {
                    session.flush();
                    session.clear();
                }

                updateCount.incrementAndGet();
            }

            // TRANSACTION.COMMIT: Valider la transaction
            transaction.commit();

            // Invalider le cache pour ces ressources
            resources.forEach(r -> resourceCache.remove(r.getId()));

            System.out.println("✅ Transaction batch réussie");

        } catch (Exception e) {
            // TRANSACTION.ROLLBACK: Annuler en cas d'erreur
            if (transaction != null) {
                transaction.rollback();
            }
            errorCount.incrementAndGet();
            throw new LomException("Échec de la mise à jour batch", e);

        } finally {
            session.close();
        }
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES
    // =========================================================================

    /**
     * Obtenir les statistiques du service
     */
    public Map<String, Integer> getServiceStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("queries", queryCount.get());
        stats.put("updates", updateCount.get());
        stats.put("errors", errorCount.get());
        stats.put("cacheSize", resourceCache.size());
        return stats;
    }

    /**
     * Vider le cache
     */
    public void clearCache() {
        resourceCache.clear();
        System.out.println("🗑️ Cache vidé");
    }

    /**
     * Arrêter le service proprement
     */
    public void shutdown() {
        dbExecutor.shutdown();
        try {
            if (!dbExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                dbExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            dbExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
