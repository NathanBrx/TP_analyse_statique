# Java Static Analyzer

## Description
Cet outil en ligne de commande permet d'effectuer une analyse statique de projets Java. Basé sur Eclipse JDT, il extrait la structure du code, analyse les appels de méthodes, calcule des métriques logicielles et génère un graphe d'appel exportable au format DOT.

## Prérequis
- Java Development Kit (JDK) 11 ou supérieur.
- Apache Maven.

## Compilation
Pour construire le projet depuis un état propre, exécutez la commande suivante à la racine du dépôt :

```bash
mvn clean package
```
Cela générera une archive exécutable dans le dossier `target/` (par exemple : `target/hai913i-tp1-analyzer.jar`).

## Utilisation
La syntaxe générale de l'outil est la suivante :

```bash
java -jar target/hai913i-tp1-analyzer.jar <CHEMIN_DU_PROJET_A_ANALYSER> <ACTION> [SEUIL_X]
```

### Actions disponibles
- `tree` : Affiche l'arbre syntaxique abstrait (AST) des fichiers sources.
- `structure` : Affiche le bilan structurel (nombre de classes, interfaces, énumérations, méthodes, attributs).
- `calls` : Affiche le bilan des appels de méthodes (internes, externes, non résolus, super-méthodes).
- `metrics` : Calcule et affiche les 13 métriques logicielles. Le paramètre optionnel `[SEUIL_X]` (entier, par défaut 5) est utilisé pour identifier les classes ayant plus de X méthodes.
- `graph` : Génère le graphe d'appel en console et l'exporte dans un fichier `callgraph.dot` exploitable avec Graphviz.

### Exemple
```bash
java -jar target/hai913i-tp1-analyzer.jar /chemin/vers/mon/projet metrics 10
```

## Traitement des erreurs
L'outil est conçu pour être robuste :
- **Dossier inexistant ou sans sources :** L'outil affiche un message explicite sur la sortie d'erreur (`System.err`) et s'arrête avec un code de sortie non nul.
- **Erreurs de syntaxe dans le projet analysé :** Si des fichiers `.java` contiennent des erreurs de syntaxe, l'outil les signale mais continue l'analyse sur les parties valides du code (best-effort).
- **Arguments malformés :** Si le seuil X n'est pas un entier valide, un refus explicite est affiché et le programme s'arrête proprement.