package hai913i.tp1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import hai913i.tp1.callgraph.CallGraph;
import hai913i.tp1.metrics.MetricsService;
import hai913i.tp1.model.*;
import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.ProjectSources;
import hai913i.tp1.visitor.*;
import org.eclipse.jdt.core.compiler.IProblem;

import hai913i.tp1.parse.JdtParser.ParsedFile;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        // vérification nb arguments ligne de commande
        if (args.length < 2) {
            afficherUsageEtQuitter("Erreur : Nombre d'arguments insuffisant.");
        }

        Path project = Path.of(args[0]);
        String action = args[1].toLowerCase();

        // vérification dossier existant
        if (!Files.exists(project) || !Files.isDirectory(project)) {
            afficherUsageEtQuitter("Erreur : Le dossier spécifié n'existe pas ou n'est pas un répertoire valide : " + project);
        }

        ProjectSources sources;

        // vérification sources valides
        try {
            sources = ProjectSources.of(project);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur lors de la lecture des sources : " + e.getMessage());
            System.exit(3);
            return;
        }

        // vérification parsing des sources possible
        List<ParsedFile> files;
        try {
            files = JdtParser.parse(sources, List.of());
        } catch (Exception e) {
            System.err.println("Erreur lors du parsing JDT : " + e.getMessage());
            System.exit(3);
            return;
        }

        // pas de fichier java présent
        if (files.isEmpty()) {
            System.err.println("Erreur : Aucun fichier source .java n'a été trouvé dans le dossier : " + project);
            System.exit(3);
        }

        // décompte des erreurs sans arrêter l'analyse
        int errors = 0;
        for (ParsedFile file : files) {
            for (IProblem problem : file.unit().getProblems()) {
                if (problem.isError()) {
                    errors++;
                    System.err.println(file.path().getFileName() + ":" + problem.getSourceLineNumber() + " " + problem.getMessage());
                }
            }
        }

        // premier bilan
        System.out.println("Racine des sources     : " + sources.sourceRoot());
        System.out.println("Unites de compilation  : " + files.size());
        System.out.println("Erreurs de compilation : " + errors);

        // affichage arbre ast
        if (action.equals("tree")) {
            executerTreeVisitor(files);
            return;
        }

        // construction modèle de faits
        ProjectFacts projectFacts = new ProjectFacts();

        for (ParsedFile file : files) {
            StructureVisitor structureVisitor = new StructureVisitor(projectFacts, file.unit());
            file.unit().accept(structureVisitor);
        }

        for (ParsedFile file : files) {
            CallVisitor callVisitor = new CallVisitor(file.unit(), projectFacts);
            file.unit().accept(callVisitor);
        }

        // traitement de l'action entrée
        switch (action) {
            case "structure":
                afficherBilanStructure(projectFacts);
                break;

            case "calls":
                afficherBilanAppels(projectFacts);
                break;

            case "metrics":
                int seuilX = 5;
                if (args.length >= 3) {
                    try {
                        seuilX = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        afficherUsageEtQuitter("Erreur : Le seuil X doit être un entier valide.");
                    }
                }

                int totalAppLines = 0;
                for (ParsedFile file : files) {
                    try {
                        totalAppLines += Files.readAllLines(file.path()).size();
                    } catch (IOException ignored) {}
                }

                MetricsService metricsService = new MetricsService(projectFacts, totalAppLines);
                metricsService.printMetrics(seuilX);
                break;

            case "graph":
                CallGraph graph = new CallGraph(projectFacts);
                graph.printGraphInfo();
                break;

            default:
                afficherUsageEtQuitter("Action non reconnue : " + action);
        }
    }

    private static void executerTreeVisitor(List<ParsedFile> files) {
        PrintTreeVisitor printer = new PrintTreeVisitor();
        for (ParsedFile file : files) {
            System.out.println("\n--- Arbre AST pour : " + file.path().getFileName() + " ---");
            file.unit().accept(printer);
        }
    }

    private static void afficherBilanStructure(ProjectFacts facts) {
        int nbClasses = 0;
        int nbInterfaces = 0;
        int nbEnums = 0;
        int nbMethodes = 0;
        int nbConstructors = 0;
        int nbAttributs = 0;

        for (TypeFact type : facts.getTypes()) {
            switch (type.getKind()) {
                case "interface": nbInterfaces++; break;
                case "énumération": nbEnums++; break;
                default: nbClasses++; break;
            }

            nbAttributs += type.getFields().size();
            nbMethodes += type.getMethods().size();

            for (MethodFact method : type.getMethods()) {
                if (method.isConstructor()) {
                    nbConstructors++;
                }
            }
        }

        int nbTotalClasses = nbClasses + nbInterfaces + nbEnums;

        System.out.println("\n--- BILAN DE STRUCTURE ---");
        System.out.println(nbTotalClasses + " classes au total : " +
                nbClasses + " classes, " +
                nbInterfaces + " interfaces, " +
                nbEnums + " énumérations");

        System.out.println(nbMethodes + " méthodes, dont " + nbConstructors + " constructeurs");
        System.out.println(nbAttributs + " attributs");
    }

    private static void afficherBilanAppels(ProjectFacts facts) {
        int totalAppels = 0;
        int methodes = 0;
        int supermethodes = 0;
        int internes = 0;
        int externes = 0;
        int nonResolus = 0;

        for (TypeFact type : facts.getTypes()) {
            for (MethodFact method : type.getMethods()) {
                for (CallFact call : method.getCalls()) {
                    totalAppels++;
                    if (call.isSuper()) {
                        supermethodes++;
                    } else {
                         methodes++;
                    }
                    if (!call.isResolved()) {
                        nonResolus++;
                    } else if (call.isInternal()) {
                        internes++;
                    } else {
                        externes++;
                    }
                }
            }
        }

        System.out.println("\n--- BILAN DES APPELS ---");
        System.out.println(totalAppels + " appels (" + methodes + " MethodInvocation et " + supermethodes + " SuperMethodInvocation)");
        System.out.println(internes + " vers des méthodes du projet (internes)");
        System.out.println(externes + " vers l'extérieur (externes)");
        System.out.println(nonResolus + " non résolus");
    }

    public static void afficherUsageEtQuitter(String errorMessage) {
        System.err.println(errorMessage + "\n");
        System.err.println("Usage : java -jar target/hai913i-tp1-analyzer.jar <DOSSIER_DU_PROJET> <ACTION>");
        System.err.println("Actions disponibles :");
        System.err.println("  tree      : Arbre AST (Étape A1)");
        System.err.println("  structure : Structure (Étape A2)");
        System.err.println("  calls     : Appels de méthodes (Étape A3)");
        System.err.println("  metrics   : Calcul des 13 métriques (Étape B2) [SEUIL_X optionnel, par défaut=5]");
        System.err.println("  graph     : Génération du graphe d'appel et export DOT (Étape B3)");
        System.exit(2);
    }
}
