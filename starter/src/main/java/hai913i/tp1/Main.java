package hai913i.tp1;

import java.nio.file.Path;
import java.util.List;

import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.ProjectSources;
import hai913i.tp1.visitor.*;
import org.eclipse.jdt.core.compiler.IProblem;

import hai913i.tp1.parse.JdtParser.ParsedFile;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage : java -jar target/hai913i-tp1-analyzer.jar <DOSSIER_DU_PROJET> <ACTION>");
            System.err.println("Actions disponibles :");
            System.err.println("  tree      : Arbre AST (Étape A1)");
            System.err.println("  structure : Structure (Étape A2)");
            System.err.println("  calls     : Appels de méthodes (Étape A3)");
            System.exit(2);
        }

        Path project = Path.of(args[0]);
        String action = args[1].toLowerCase();
        ProjectSources sources;

        try {
            sources = ProjectSources.of(project);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur : " + e.getMessage());
            System.exit(3);
            return;
        }

        List<ParsedFile> files = JdtParser.parse(sources, List.of());
        int errors = 0;
        for (ParsedFile file : files) {
            for (IProblem problem : file.unit().getProblems()) {
                if (problem.isError()) {
                    errors++;
                    System.err.println(file.path().getFileName() + ":" + problem.getSourceLineNumber() + " "
                            + problem.getMessage());
                }
            }
        }
        System.out.println("Racine des sources     : " + sources.sourceRoot());
        System.out.println("Unites de compilation  : " + files.size());
        System.out.println("Erreurs de compilation : " + errors);
        switch (action) {
            case "tree":
                executerTreeVisitor(files);
                break;

            case "structure":
                executerStructureVisitor(files);
                break;

            case "calls":
                executerCallVisitor(files);
                break;

            default:
                System.err.println("Action non reconnue : '" + action + "'. Utilisez tree, structure, ou calls.");
                System.exit(2);
        }
    }

    private static void executerTreeVisitor(List<ParsedFile> files) {
        PrintTreeVisitor printer = new PrintTreeVisitor();
        for (ParsedFile file : files) {
            System.out.println("\n--- Arbre AST pour : " + file.path().getFileName() + " ---");
            file.unit().accept(printer);
        }
    }

    private static void executerStructureVisitor(List<ParsedFile> files) {
        StructureVisitor structureVisitor = new StructureVisitor();
        for (ParsedFile file : files) {
            file.unit().accept(structureVisitor);
        }

        int nbTotalClasses = structureVisitor.getNbClasses() + structureVisitor.getNbEnums() + structureVisitor.getNbInterfaces();
        System.out.println("\n--- BILAN DE STRUCTURE ---");
        System.out.println(nbTotalClasses + " classes : " +
                structureVisitor.getNbClasses() + " classes, " +
                structureVisitor.getNbInterfaces() + " interfaces et " +
                structureVisitor.getNbEnums() + " énumérations");

        System.out.println(structureVisitor.getNbMethodes() + " méthodes, dont " +
                structureVisitor.getNbConstructors() + " constructeurs");

        System.out.println(structureVisitor.getNbAttributes() + " attributs");
    }

    private static void executerCallVisitor(List<ParsedFile> files) {;
        int totalAppels = 0;
        int methodes = 0;
        int supermethodes = 0;
        int internes = 0;
        int externes = 0;
        int nonResolus = 0;

        for (ParsedFile file : files) {
            CallVisitor callVisitor = new CallVisitor(file.unit());
            file.unit().accept(callVisitor);

            totalAppels += callVisitor.getNbTotalAppels();
            methodes += callVisitor.getNbMethodInvocations();
            supermethodes += callVisitor.getNbSuperMethodInvocations();
            internes += callVisitor.getNbInternes();
            externes += callVisitor.getNbExternes();
            nonResolus += callVisitor.getNbNonResolus();
        }

        System.out.println("\n--- BILAN DES APPELS ---");
        System.out.println(totalAppels + " appels (" + methodes + " MethodInvocation et " + supermethodes + " SuperMethodInvocation)");
        System.out.println(internes + " vers des méthodes du projet (internes)");
        System.out.println(externes + " vers l'extérieur (externes)");
        System.out.println(nonResolus + " non résolus");
    }
}
