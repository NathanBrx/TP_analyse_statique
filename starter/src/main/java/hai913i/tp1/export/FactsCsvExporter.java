package hai913i.tp1.export;

import hai913i.tp1.model.CallFact;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.model.TypeFact;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class FactsCsvExporter {

    private record ArcKey(String caller, String callerClass, String callee, String calleeClass) {}

    public static void export(ProjectFacts facts, Path outputDir, String prefix) throws IOException {
        Files.createDirectories(outputDir);

        Path classesCsv = outputDir.resolve(prefix + "-classes.csv");
        Path methodsCsv = outputDir.resolve(prefix + "-methods.csv");
        Path callsCsv = outputDir.resolve(prefix + "-calls.csv");

        Set<String> projectClasses = new HashSet<>();

        try (BufferedWriter writer = Files.newBufferedWriter(classesCsv, StandardCharsets.UTF_8)) {
            writer.write("class;simpleName;package\n");
            for (TypeFact type : facts.getTypes()) {
                String qName = type.getQualifiedName();
                projectClasses.add(qName);
                writer.write(String.format("%s;%s;%s\n", qName, extractSimpleName(qName), extractPackageName(qName)));
            }
        }

        Map<ArcKey, Integer> sitesCountMap = new LinkedHashMap<>();

        try (BufferedWriter writer = Files.newBufferedWriter(methodsCsv, StandardCharsets.UTF_8)) {
            writer.write("method;owner\n");
            for (TypeFact type : facts.getTypes()) {
                String callerClass = type.getQualifiedName();

                for (MethodFact method : type.getMethods()) {
                    String methodId = method.getId();
                    writer.write(String.format("%s;%s\n", methodId, callerClass));

                    for (CallFact call : method.getCalls()) {
                        if (call.isResolved()) {
                            String calleeClass = call.getStaticReceiverType();

                            if (projectClasses.contains(calleeClass)) {
                                String caller = call.getCallerId();
                                String callee = call.getTargetId().orElse("Inconnu");

                                ArcKey key = new ArcKey(caller, callerClass, callee, calleeClass);
                                sitesCountMap.put(key, sitesCountMap.getOrDefault(key, 0) + 1);
                            }
                        }
                    }
                }
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(callsCsv, StandardCharsets.UTF_8)) {
            writer.write("caller;callerClass;callee;calleeClass;sites\n");
            for (Map.Entry<ArcKey, Integer> entry : sitesCountMap.entrySet()) {
                ArcKey k = entry.getKey();
                if (projectClasses.contains(k.calleeClass) && !k.calleeClass.endsWith("LoanException")) {
                    writer.write(String.format("%s;%s;%s;%s;%d\n",
                            k.caller(), k.callerClass(), k.callee(), k.calleeClass(), entry.getValue()));
                }
            }
        }
    }

    private static String extractSimpleName(String qName) {
        int dot = qName.lastIndexOf('.');
        return (dot == -1) ? qName : qName.substring(dot + 1);
    }

    private static String extractPackageName(String qName) {
        int dot = qName.lastIndexOf('.');
        return (dot == -1) ? "" : qName.substring(0, dot);
    }
}