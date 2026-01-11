package org.emsi.service;

import org.emsi.dao.LomSchemaDao;
import org.emsi.entities.LomSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Service d'import en batch utilisant ThreadPoolExecutor et ExecutorService
 * 
 * Ce service démontre l'utilisation de:
 * - ThreadPoolExecutor pour la gestion du pool de threads
 * - ExecutorService pour l'exécution parallèle
 * - Future pour récupérer les résultats asynchrones
 * - Synchronisation avec CountDownLatch
 * - Gestion des exceptions dans un contexte multi-thread
 * 
 * @author Projet LOM - EMSI
 */
public class BatchImportService {

    private static BatchImportService instance;
    private final LomService lomService;

    // THREADPOOL: Configuration du pool de threads
    // CorePoolSize: 4 threads de base
    // MaxPoolSize: 10 threads maximum
    // KeepAliveTime: 60 secondes d'inactivité avant suppression
    private final ExecutorService executorService;

    // Pour la notification de progression
    private ImportProgressListener progressListener;

    private BatchImportService() {
        this.lomService = LomService.getInstance();

        // THREADPOOLEXECUTOR: Création d'un pool personnalisé
        // Cela permet un contrôle fin sur l'exécution parallèle
        this.executorService = new ThreadPoolExecutor(
                4, // corePoolSize - threads toujours actifs
                10, // maximumPoolSize - threads max en cas de charge
                60L, // keepAliveTime - durée avant suppression thread inactif
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100), // file d'attente de 100 tâches
                new ThreadFactory() {
                    private int counter = 0;

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "LOM-Import-Thread-" + counter++);
                        t.setDaemon(true); // Thread démon : n'empêche pas l'arrêt JVM
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // Politique si file pleine
        );
    }

    public static BatchImportService getInstance() {
        if (instance == null) {
            // SYNCHRONISATION: Double-checked locking pour singleton thread-safe
            synchronized (BatchImportService.class) {
                if (instance == null) {
                    instance = new BatchImportService();
                }
            }
        }
        return instance;
    }

    /**
     * Interface listener pour suivre la progression de l'import
     */
    public interface ImportProgressListener {
        void onProgress(int current, int total, String currentItem);

        void onComplete(int success, int failed);

        void onError(String message, Exception e);
    }

    public void setProgressListener(ImportProgressListener listener) {
        this.progressListener = listener;
    }

    /**
     * EXECUTORSERVICE + FUTURE: Import parallèle de ressources
     * 
     * Cette méthode démontre:
     * - Soumission de tâches Callable au ExecutorService
     * - Récupération des résultats via Future
     * - Gestion des exceptions ExecutionException
     * 
     * @param resourcesData Liste de données à importer
     * @return ImportResult résultat de l'import
     */
    public ImportResult importResourcesParallel(List<ResourceData> resourcesData) {
        int total = resourcesData.size();
        List<Future<LomSchema>> futures = new ArrayList<>();

        // EXECUTORSERVICE: Soumission des tâches d'import
        // Chaque import est soumis comme une tâche Callable
        for (ResourceData data : resourcesData) {
            Callable<LomSchema> importTask = () -> {
                try {
                    // Simulation d'un import qui prend du temps
                    Thread.sleep(100);
                    return lomService.createResource(data.title, data.url);
                } catch (Exception e) {
                    // GESTION EXCEPTIONS: Log et propagation
                    System.err.println("❌ Erreur import: " + data.title + " - " + e.getMessage());
                    throw new ImportException("Échec import: " + data.title, e);
                }
            };

            // FUTURE: Soumission et récupération du Future
            futures.add(executorService.submit(importTask));
        }

        // Récupération des résultats
        List<LomSchema> imported = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int current = 0;

        for (int i = 0; i < futures.size(); i++) {
            Future<LomSchema> future = futures.get(i);
            ResourceData data = resourcesData.get(i);
            current++;

            try {
                // FUTURE.GET(): Attente du résultat (bloquant)
                // Timeout de 30 secondes par ressource
                LomSchema result = future.get(30, TimeUnit.SECONDS);
                if (result != null) {
                    imported.add(result);
                }

                // Notification progression
                if (progressListener != null) {
                    progressListener.onProgress(current, total, data.title);
                }

            } catch (InterruptedException e) {
                // GESTION EXCEPTIONS: Thread interrompu
                Thread.currentThread().interrupt();
                errors.add(data.title + " (interrompu)");

            } catch (ExecutionException e) {
                // GESTION EXCEPTIONS: Erreur dans la tâche
                errors.add(data.title + " (" + e.getCause().getMessage() + ")");
                if (progressListener != null) {
                    progressListener.onError(data.title, (Exception) e.getCause());
                }

            } catch (TimeoutException e) {
                // GESTION EXCEPTIONS: Timeout dépassé
                future.cancel(true);
                errors.add(data.title + " (timeout)");
            }
        }

        // Notification de fin
        if (progressListener != null) {
            progressListener.onComplete(imported.size(), errors.size());
        }

        return new ImportResult(imported, errors);
    }

    /**
     * COUNTDOWNLATCH: Import synchronisé avec barrière
     * 
     * Démontre l'utilisation de CountDownLatch pour:
     * - Attendre que toutes les tâches soient terminées
     * - Synchroniser plusieurs threads
     * 
     * @param resourcesData  données à importer
     * @param maxWaitSeconds temps max d'attente
     * @return ImportResult résultat
     */
    public ImportResult importWithSynchronization(List<ResourceData> resourcesData, int maxWaitSeconds)
            throws InterruptedException {

        int total = resourcesData.size();

        // COUNTDOWNLATCH: Crée une barrière de synchronisation
        // Elle sera décrémentée par chaque thread qui termine
        CountDownLatch latch = new CountDownLatch(total);

        // Collections thread-safe pour les résultats
        // COLLECTIONS SYNCHRONISÉES: Nécessaire car accès concurrent
        List<LomSchema> imported = new CopyOnWriteArrayList<>();
        List<String> errors = new CopyOnWriteArrayList<>();

        for (ResourceData data : resourcesData) {
            // EXECUTORSERVICE.EXECUTE(): Fire-and-forget
            executorService.execute(() -> {
                try {
                    LomSchema result = lomService.createResource(data.title, data.url);
                    if (result != null) {
                        // THREAD-SAFE: CopyOnWriteArrayList gère la concurrence
                        imported.add(result);
                    }
                } catch (Exception e) {
                    errors.add(data.title + ": " + e.getMessage());
                } finally {
                    // COUNTDOWNLATCH.COUNTDOWN(): Signale fin de cette tâche
                    latch.countDown();
                }
            });
        }

        // COUNTDOWNLATCH.AWAIT(): Attend que toutes les tâches finissent
        // ou que le timeout soit atteint
        boolean completed = latch.await(maxWaitSeconds, TimeUnit.SECONDS);

        if (!completed) {
            System.err.println("⚠️ Timeout: certaines tâches n'ont pas terminé");
        }

        return new ImportResult(new ArrayList<>(imported), new ArrayList<>(errors));
    }

    /**
     * PARALLELSTREAM: Import utilisant les parallel streams
     * 
     * Démontre l'utilisation de parallelStream() pour:
     * - Parallélisation automatique par la JVM
     * - Simplification du code concurrent
     * 
     * @param resourcesData données à importer
     * @return List<LomSchema> ressources importées
     */
    public List<LomSchema> importWithParallelStream(List<ResourceData> resourcesData) {
        // PARALLELSTREAM: La JVM gère automatiquement le parallélisme
        // Utilise le ForkJoinPool.commonPool() par défaut
        return resourcesData.parallelStream()
                .map(data -> {
                    try {
                        return lomService.createResource(data.title, data.url);
                    } catch (Exception e) {
                        System.err.println("❌ Erreur: " + data.title);
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull) // Filtrer les échecs
                .collect(Collectors.toList());
    }

    /**
     * Arrêter proprement le service
     */
    public void shutdown() {
        // EXECUTORSERVICE.SHUTDOWN(): Arrêt gracieux
        // N'accepte plus de nouvelles tâches mais termine les en cours
        executorService.shutdown();
        try {
            // Attendre la fin des tâches en cours (max 60s)
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                // EXECUTORSERVICE.SHUTDOWNNOW(): Arrêt forcé
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // =========================================================================
    // CLASSES INTERNES
    // =========================================================================

    /**
     * Classe de données pour une ressource à importer
     */
    public static class ResourceData {
        public final String title;
        public final String url;
        public final String description;
        public final String language;

        public ResourceData(String title, String url, String description, String language) {
            this.title = title;
            this.url = url;
            this.description = description;
            this.language = language;
        }

        public ResourceData(String title, String url) {
            this(title, url, null, null);
        }
    }

    /**
     * Résultat d'un import en batch
     */
    public static class ImportResult {
        public final List<LomSchema> imported;
        public final List<String> errors;
        public final int successCount;
        public final int errorCount;

        public ImportResult(List<LomSchema> imported, List<String> errors) {
            this.imported = imported;
            this.errors = errors;
            this.successCount = imported.size();
            this.errorCount = errors.size();
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public double getSuccessRate() {
            int total = successCount + errorCount;
            return total > 0 ? (successCount * 100.0 / total) : 0;
        }
    }

    /**
     * EXCEPTION PERSONNALISÉE: Pour les erreurs d'import
     */
    public static class ImportException extends RuntimeException {
        public ImportException(String message) {
            super(message);
        }

        public ImportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
