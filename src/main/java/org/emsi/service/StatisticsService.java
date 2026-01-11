package org.emsi.service;

import org.emsi.dao.LomSchemaDao;
import org.emsi.entities.LomSchema;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service de statistiques utilisant intensivement les Streams et Collections
 * Java
 * 
 * Ce service démontre l'utilisation avancée de :
 * - Stream API (filter, map, collect, groupingBy, etc.)
 * - Collectors personnalisés
 * - Predicate et Function interfaces
 * - Collections (Map, Set, List)
 * 
 * @author Projet LOM - EMSI
 */
public class StatisticsService {

    private static StatisticsService instance;
    private final LomSchemaDao lomSchemaDao;

    private StatisticsService() {
        this.lomSchemaDao = new LomSchemaDao();
    }

    /**
     * Obtenir l'instance unique (Singleton Pattern)
     */
    public static StatisticsService getInstance() {
        if (instance == null) {
            instance = new StatisticsService();
        }
        return instance;
    }

    // =====================================================================
    // MÉTHODES UTILISANT LES STREAMS - GROUPEMENT ET AGRÉGATION
    // =====================================================================

    /**
     * STREAM: Compter les ressources par langue
     * 
     * Utilise:
     * - stream() pour créer un flux de données
     * - filter() pour exclure les ressources sans catégorie General
     * - collect() avec Collectors.groupingBy() pour regrouper par langue
     * - Collectors.counting() pour compter les éléments de chaque groupe
     * 
     * @return Map<String, Long> clé=langue, valeur=nombre de ressources
     */
    public Map<String, Long> countResourcesByLanguage() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // STREAM API: Regroupement par langue avec comptage
        // groupingBy() crée une Map où chaque clé est une langue
        // counting() compte le nombre d'éléments dans chaque groupe
        return resources.stream()
                .filter(r -> r.getGeneral() != null) // Filtrer les ressources avec General
                .filter(r -> r.getGeneral().getLanguage() != null) // Exclure les langues nulles
                .collect(Collectors.groupingBy(
                        r -> r.getGeneral().getLanguage(), // Fonction de regroupement
                        Collectors.counting() // Opération d'agrégation
                ));
    }

    /**
     * STREAM: Compter les ressources par niveau de difficulté
     * 
     * Utilise:
     * - stream() et filter() pour le filtrage en chaîne
     * - mapToInt() pour convertir en IntStream
     * - boxed() pour reconvertir en Stream<Integer>
     * - groupingBy() + counting() pour l'agrégation
     * 
     * @return Map<Integer, Long> clé=niveau (1-5), valeur=nombre de ressources
     */
    public Map<Integer, Long> countResourcesByDifficulty() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // STREAM API: Regroupement par difficulté avec comptage
        // Utilisation de Optional pour gérer les valeurs nulles de manière sécurisée
        return resources.stream()
                .filter(r -> r.getEducational() != null)
                .filter(r -> r.getEducational().getDifficulty() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getEducational().getDifficulty(),
                        Collectors.counting()));
    }

    /**
     * STREAM: Calculer la moyenne de difficulté des ressources
     * 
     * Utilise:
     * - mapToInt() pour convertir les objets en primitives int
     * - average() qui retourne un OptionalDouble
     * - orElse() pour fournir une valeur par défaut
     * 
     * @return double moyenne de difficulté (0.0 si aucune ressource)
     */
    public double getAverageDifficulty() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // STREAM API: Calcul de moyenne avec mapToInt() et average()
        // mapToInt() convertit le Stream en IntStream pour les opérations numériques
        return resources.stream()
                .filter(r -> r.getEducational() != null)
                .filter(r -> r.getEducational().getDifficulty() != null)
                .mapToInt(r -> r.getEducational().getDifficulty()) // Conversion en IntStream
                .average() // Calcul de la moyenne (retourne OptionalDouble)
                .orElse(0.0); // Valeur par défaut si le stream est vide
    }

    /**
     * STREAM: Obtenir les statistiques complètes de difficulté
     * 
     * Utilise:
     * - IntSummaryStatistics pour obtenir min, max, sum, average, count
     * - summaryStatistics() qui agrège plusieurs statistiques en une opération
     * 
     * @return IntSummaryStatistics contenant toutes les statistiques
     */
    public IntSummaryStatistics getDifficultyStatistics() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // STREAM API: Obtenir toutes les statistiques en une seule passe
        // IntSummaryStatistics contient: count, sum, min, max, average
        return resources.stream()
                .filter(r -> r.getEducational() != null)
                .filter(r -> r.getEducational().getDifficulty() != null)
                .mapToInt(r -> r.getEducational().getDifficulty())
                .summaryStatistics(); // Agrège toutes les stats
    }

    // =====================================================================
    // MÉTHODES UTILISANT LES STREAMS - TRI ET LIMITATION
    // =====================================================================

    /**
     * STREAM: Obtenir les N ressources les plus récentes
     * 
     * Utilise:
     * - sorted() avec Comparator.comparing() pour le tri
     * - reversed() pour l'ordre décroissant
     * - limit() pour limiter le nombre de résultats
     * - collect(Collectors.toList()) pour convertir en List
     * 
     * @param limit nombre maximum de ressources à retourner
     * @return List<LomSchema> ressources triées par date décroissante
     */
    public List<LomSchema> getMostRecentResources(int limit) {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // STREAM API: Tri et limitation
        // sorted() avec Comparator personnalisé
        // reversed() inverse l'ordre de tri
        // limit() restreint le nombre de résultats
        return resources.stream()
                .filter(r -> r.getCreatedAt() != null) // Exclure les dates nulles
                .sorted(Comparator.comparing(LomSchema::getCreatedAt).reversed()) // Tri décroissant
                .limit(limit) // Limiter à N résultats
                .collect(Collectors.toList()); // Collecter en List
    }

    /**
     * STREAM: Obtenir les ressources par type d'interactivité
     * 
     * Utilise:
     * - groupingBy() pour regrouper par type
     * - toList() collector pour collecter les ressources de chaque groupe
     * 
     * @return Map<String, List<LomSchema>> ressources groupées par type
     */
    public Map<String, List<LomSchema>> getResourcesByInteractivityType() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // STREAM API: Regroupement avec collection des éléments
        // Contrairement à counting(), toList() conserve les objets complets
        return resources.stream()
                .filter(r -> r.getEducational() != null)
                .filter(r -> r.getEducational().getInteractivityType() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getEducational().getInteractivityType(),
                        Collectors.toList() // Collecter les ressources, pas juste compter
                ));
    }

    // =====================================================================
    // MÉTHODES UTILISANT LES STREAMS - FILTRAGE AVANCÉ AVEC PREDICATES
    // =====================================================================

    /**
     * STREAM: Recherche avec Predicates combinés
     * 
     * Démontre l'utilisation de:
     * - Predicate interface fonctionnelle
     * - Combinaison de predicates avec and()
     * - Passage de predicates en paramètre
     * 
     * @param titleContains texte à rechercher dans le titre
     * @param language      langue de la ressource (null = toutes)
     * @param minDifficulty difficulté minimum (null = pas de minimum)
     * @return List<LomSchema> ressources correspondant aux critères
     */
    public List<LomSchema> searchWithPredicates(String titleContains, String language, Integer minDifficulty) {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // PREDICATE: Définition des conditions de filtrage
        // Chaque Predicate est une fonction qui retourne boolean

        // Predicate pour le titre (contient le texte recherché)
        Predicate<LomSchema> titlePredicate = r -> titleContains == null ||
                r.getResourceTitle().toLowerCase().contains(titleContains.toLowerCase());

        // Predicate pour la langue (correspond à la langue demandée)
        Predicate<LomSchema> languagePredicate = r -> language == null ||
                (r.getGeneral() != null && language.equals(r.getGeneral().getLanguage()));

        // Predicate pour la difficulté (supérieure ou égale au minimum)
        Predicate<LomSchema> difficultyPredicate = r -> minDifficulty == null ||
                (r.getEducational() != null &&
                        r.getEducational().getDifficulty() != null &&
                        r.getEducational().getDifficulty() >= minDifficulty);

        // STREAM API: Combinaison des predicates avec and()
        // Permet de construire des filtres complexes de manière lisible
        return resources.stream()
                .filter(titlePredicate.and(languagePredicate).and(difficultyPredicate))
                .collect(Collectors.toList());
    }

    // =====================================================================
    // MÉTHODES UTILISANT LES STREAMS - TRANSFORMATION ET MAPPING
    // =====================================================================

    /**
     * STREAM: Extraire tous les mots-clés uniques
     * 
     * Utilise:
     * - map() pour extraire les mots-clés
     * - flatMap() pour aplatir les tableaux en un seul stream
     * - distinct() pour éliminer les doublons
     * - sorted() pour trier alphabétiquement
     * 
     * @return Set<String> ensemble unique de mots-clés
     */
    public Set<String> getAllUniqueKeywords() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // STREAM API: Extraction et aplatissement
        // flatMap() est utilisé pour transformer chaque ressource en plusieurs
        // mots-clés
        return resources.stream()
                .filter(r -> r.getGeneral() != null)
                .filter(r -> r.getGeneral().getKeyword() != null)
                .map(r -> r.getGeneral().getKeyword()) // Extraire le champ keyword
                .flatMap(keywords -> Arrays.stream(keywords.split(",\\s*"))) // Éclater la chaîne
                .map(String::trim) // Supprimer les espaces
                .filter(k -> !k.isEmpty()) // Exclure les chaînes vides
                .collect(Collectors.toCollection(TreeSet::new)); // Collecter en TreeSet (trié)
    }

    /**
     * STREAM: Obtenir toutes les langues disponibles
     * 
     * Utilise:
     * - map() pour extraire les langues
     * - distinct() pour éliminer les doublons
     * - collect(Collectors.toList())
     * 
     * @return List<String> liste des langues uniques
     */
    public List<String> getAvailableLanguages() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // STREAM API: Extraction de valeurs uniques
        // distinct() élimine les doublons basé sur equals()
        return resources.stream()
                .filter(r -> r.getGeneral() != null)
                .map(r -> r.getGeneral().getLanguage()) // Extraire la langue
                .filter(Objects::nonNull) // Exclure les valeurs null (référence de méthode)
                .distinct() // Supprimer les doublons
                .sorted() // Trier alphabétiquement
                .collect(Collectors.toList());
    }

    // =====================================================================
    // MÉTHODES UTILISANT LES STREAMS - STATISTIQUES PAR PÉRIODE
    // =====================================================================

    /**
     * STREAM: Compter les ressources créées par mois
     * 
     * Utilise:
     * - LocalDate pour la manipulation de dates
     * - groupingBy() avec Function personnalisée
     * 
     * @return Map<String, Long> clé="YYYY-MM", valeur=nombre de ressources
     */
    public Map<String, Long> countResourcesByMonth() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // FUNCTION: Extraction année-mois depuis Date
        Function<LomSchema, String> yearMonthExtractor = r -> {
            LocalDate date = r.getCreatedAt().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            return String.format("%d-%02d", date.getYear(), date.getMonthValue());
        };

        // STREAM API: Regroupement par année-mois
        // TreeMap maintient l'ordre chronologique
        return resources.stream()
                .filter(r -> r.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        yearMonthExtractor,
                        TreeMap::new, // Factory pour TreeMap (ordre naturel)
                        Collectors.counting()));
    }

    /**
     * STREAM: Obtenir le nombre de ressources créées aujourd'hui
     * 
     * Utilise:
     * - filter() avec comparaison de dates
     * - count() terminal operation
     * 
     * @return long nombre de ressources créées aujourd'hui
     */
    public long countResourcesCreatedToday() {
        List<LomSchema> resources = lomSchemaDao.findAll();
        LocalDate today = LocalDate.now();

        // STREAM API: Filtrage et comptage
        return resources.stream()
                .filter(r -> r.getCreatedAt() != null)
                .filter(r -> {
                    LocalDate resourceDate = r.getCreatedAt().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return resourceDate.equals(today);
                })
                .count(); // Opération terminale: compte les éléments
    }

    // =====================================================================
    // MÉTHODES UTILISANT LES STREAMS - VÉRIFICATION ET VALIDATION
    // =====================================================================

    /**
     * STREAM: Vérifier si toutes les ressources ont une catégorie General
     * 
     * Utilise:
     * - allMatch() qui retourne true si tous les éléments correspondent
     * 
     * @return boolean true si toutes les ressources ont un General
     */
    public boolean allResourcesHaveGeneral() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // STREAM API: Vérification avec allMatch()
        // Retourne true si TOUS les éléments satisfont la condition
        return resources.stream()
                .allMatch(r -> r.getGeneral() != null);
    }

    /**
     * STREAM: Vérifier si au moins une ressource existe pour une langue
     * 
     * Utilise:
     * - anyMatch() qui retourne true si au moins un élément correspond
     * 
     * @param language langue à vérifier
     * @return boolean true si au moins une ressource existe
     */
    public boolean hasResourcesForLanguage(String language) {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // STREAM API: Vérification avec anyMatch()
        // Retourne true si AU MOINS UN élément satisfait la condition
        return resources.stream()
                .filter(r -> r.getGeneral() != null)
                .anyMatch(r -> language.equals(r.getGeneral().getLanguage()));
    }

    /**
     * STREAM: Trouver les ressources incomplètes (métadonnées manquantes)
     * 
     * Utilise:
     * - filter() avec conditions complexes
     * - Plusieurs predicates combinés avec ||
     * 
     * @return List<LomSchema> ressources avec métadonnées incomplètes
     */
    public List<LomSchema> findIncompleteResources() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // PREDICATE: Condition pour ressource incomplète
        // Une ressource est incomplète si une catégorie obligatoire manque
        Predicate<LomSchema> isIncomplete = r -> r.getGeneral() == null ||
                r.getLifecycle() == null ||
                r.getTechnical() == null ||
                r.getEducational() == null;

        // STREAM API: Filtrage des ressources incomplètes
        return resources.stream()
                .filter(isIncomplete)
                .collect(Collectors.toList());
    }

    // =====================================================================
    // MÉTHODES UTILISANT LES COLLECTIONS - MANIPULATION AVANCÉE
    // =====================================================================

    /**
     * COLLECTIONS: Fusionner deux listes sans doublons
     * 
     * Utilise:
     * - LinkedHashSet pour maintenir l'ordre et éliminer les doublons
     * - addAll() pour fusionner
     * - ArrayList constructor avec Collection
     * 
     * @param list1 première liste
     * @param list2 deuxième liste
     * @return List<LomSchema> liste fusionnée sans doublons
     */
    public List<LomSchema> mergeWithoutDuplicates(List<LomSchema> list1, List<LomSchema> list2) {
        // COLLECTIONS: Utilisation de LinkedHashSet
        // LinkedHashSet préserve l'ordre d'insertion tout en éliminant les doublons
        Set<LomSchema> mergedSet = new LinkedHashSet<>(list1);
        mergedSet.addAll(list2); // Ajoute seulement les éléments non présents
        return new ArrayList<>(mergedSet); // Conversion en List
    }

    /**
     * COLLECTIONS + STREAM: Partitionner les ressources par statut de complétude
     * 
     * Utilise:
     * - partitioningBy() qui crée exactement 2 groupes (true/false)
     * 
     * @return Map<Boolean, List<LomSchema>> true=complètes, false=incomplètes
     */
    public Map<Boolean, List<LomSchema>> partitionByCompleteness() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // STREAM API: partitioningBy() crée deux groupes
        // Contrairement à groupingBy(), garantit exactement 2 clés (true/false)
        Predicate<LomSchema> isComplete = r -> r.getGeneral() != null &&
                r.getLifecycle() != null &&
                r.getTechnical() != null &&
                r.getEducational() != null;

        return resources.stream()
                .collect(Collectors.partitioningBy(isComplete));
    }

    /**
     * STREAM: Créer un résumé textuel de chaque ressource
     * 
     * Utilise:
     * - map() avec Function pour transformer les objets
     * - Collectors.joining() pour concaténer avec séparateur
     * 
     * @return String résumé formaté de toutes les ressources
     */
    public String generateResourcesSummary() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // STREAM API: Transformation et jointure de chaînes
        // Collectors.joining() concatène les éléments avec un séparateur
        return resources.stream()
                .map(r -> String.format("• %s (ID: %d) - Langue: %s",
                        r.getResourceTitle(),
                        r.getId(),
                        r.getGeneral() != null ? r.getGeneral().getLanguage() : "N/A"))
                .collect(Collectors.joining("\n")); // Joindre avec retour à la ligne
    }

    /**
     * STREAM: Calculer la distribution en pourcentage par langue
     * 
     * Utilise:
     * - Calcul préalable du total
     * - toMap() avec calcul de pourcentage
     * 
     * @return Map<String, Double> clé=langue, valeur=pourcentage
     */
    public Map<String, Double> getLanguageDistributionPercentage() {
        List<LomSchema> resources = lomSchemaDao.findAll();

        // Compter d'abord les ressources avec langue valide
        long total = resources.stream()
                .filter(r -> r.getGeneral() != null)
                .filter(r -> r.getGeneral().getLanguage() != null)
                .count();

        if (total == 0)
            return Collections.emptyMap();

        // STREAM API: Calcul de pourcentage pour chaque langue
        Map<String, Long> counts = countResourcesByLanguage();
        final long finalTotal = total; // Variable finale pour lambda

        return counts.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, // Conserver la clé (langue)
                        e -> (e.getValue() * 100.0) / finalTotal // Calculer le pourcentage
                ));
    }
}
