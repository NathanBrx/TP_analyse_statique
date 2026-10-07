package hai913i.tp1.callgraph;

import hai913i.tp1.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CallGraph {
    private final Set<String> nodes = new HashSet<>();

    private final Map<String, Map<String, Integer>> outgoingEdges = new HashMap<>();

    private final Map<String, Map<String, Integer>> incomingEdges = new HashMap<>();

    private int nbArcs = 0;
    private int nbInternalSites = 0;
    private int nbExternalCalls = 0;
    private int nbUnresolvedCalls = 0;

    public CallGraph(ProjectFacts facts) {
        for (TypeFact type : facts.getTypes()) {
            for (MethodFact method : type.getMethods()) {
                nodes.add(method.getId());
                outgoingEdges.put(method.getId(), new HashMap<>());
                incomingEdges.put(method.getId(), new HashMap<>());
            }
        }

        for (TypeFact type : facts.getTypes()) {
            for (MethodFact method : type.getMethods()) {
                String callerId = method.getId();

                for (CallFact call : method.getCalls()) {
                    if (!call.isResolved()) {
                        nbUnresolvedCalls++;
                    } else if (!call.isInternal()) {
                        nbExternalCalls++;
                    } else {
                        nbInternalSites++;
                        String targetId = call.getTargetId().orElse("");

                        if (nodes.contains(targetId)) {
                            outgoingEdges.get(callerId).merge(targetId, 1, Integer::sum);

                            incomingEdges.get(targetId).merge(callerId, 1, Integer::sum);
                        }
                    }
                }
            }
        }

        for (Map<String, Integer> targets : outgoingEdges.values()) {
            nbArcs += targets.size();
        }
    }

    public void printGraphInfo() {
        System.out.println("\n=== B3 : GRAPHE D'APPEL ===");
        System.out.println(nodes.size() + " nœuds, " + nbArcs + " arcs, " + nbInternalSites + " sites d'appel internes");
        System.out.println(nbExternalCalls + " appels externes, " + nbUnresolvedCalls + " appels non résolus");

        exportToDot("callgraph.dot");
    }

    public void exportToDot(String outputPath) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph CallGraph {\n");
        dot.append("    node [shape=box, style=filled, fillcolor=lightblue, fontname=\"Helvetica\"];\n");
        dot.append("    edge [fontname=\"Helvetica\", fontsize=10];\n\n");

        for (Map.Entry<String, Map<String, Integer>> entry : outgoingEdges.entrySet()) {
            String caller = entry.getKey();
            for (Map.Entry<String, Integer> targetEntry : entry.getValue().entrySet()) {
                String target = targetEntry.getKey();
                int weight = targetEntry.getValue();

                dot.append(String.format("    \"%s\" -> \"%s\" [label=\"%d\", weight=\"%d\"];\n",
                        caller, target, weight, weight));
            }
        }
        dot.append("}\n");

        try {
            Files.writeString(Paths.get(outputPath), dot.toString(), StandardCharsets.UTF_8);
            System.out.println("Graphe exporté avec succès : " + outputPath);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier DOT : " + e.getMessage());
        }
    }
}