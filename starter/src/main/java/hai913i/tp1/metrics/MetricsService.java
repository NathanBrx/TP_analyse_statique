package hai913i.tp1.metrics;

import hai913i.tp1.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class MetricsService {
    private final ProjectFacts facts;
    private final int totalAppLines;

    public MetricsService(ProjectFacts facts, int totalAppLines) {
        this.facts = facts;
        this.totalAppLines = totalAppLines;
    }

    private <T> List<T> getTop10Percent(List<T> items, java.util.function.ToIntFunction<T> valueExtractor) {
        if (items.isEmpty()) return Collections.emptyList();
        
        int n = items.size();
        int k = (n + 9) / 10;
        if (k == 0) return Collections.emptyList();

        List<T> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> Integer.compare(valueExtractor.applyAsInt(b), valueExtractor.applyAsInt(a)));
        
        int threshold = valueExtractor.applyAsInt(sorted.get(k - 1));
        
        return sorted.stream()
                .filter(item -> {
                    int val = valueExtractor.applyAsInt(item);
                    return val >= threshold && val > 0;
                })
                .collect(Collectors.toList());
    }

    public void printMetrics(int x) {
        System.out.println("\n--- MÉTRIQUES ---");

        int nbClasses = facts.getTypes().size();
        System.out.println("1. Nombre de classes : " + nbClasses);

        System.out.println("2. Lignes de code de l'application : " + totalAppLines);

        int totalMethods = facts.getTypes().stream().mapToInt(t -> t.getMethods().size()).sum();
        System.out.println("3. Nombre total de méthodes : " + totalMethods);

        long nbPackages = facts.getTypes().stream().map(TypeFact::getPackageName).distinct().count();
        System.out.println("4. Nombre total de paquetages : " + nbPackages);

        double avgMethods = nbClasses > 0 ? (double) totalMethods / nbClasses : 0;
        System.out.printf(Locale.US, "5. Moyenne de méthodes par classe : %.2f\n", avgMethods);

        int totalLinesInMethods = 0;
        int methodsWithBody = 0;
        for (TypeFact type : facts.getTypes()) {
            for (MethodFact method : type.getMethods()) {
                if (method.getBodyLoc() > 0) {
                    totalLinesInMethods += method.getBodyLoc();
                    methodsWithBody++;
                }
            }
        }
        double avgLinesPerMethod = methodsWithBody > 0 ? (double) totalLinesInMethods / methodsWithBody : 0;
        System.out.printf(Locale.US, "6. Moyenne de lignes par méthode : %.2f\n", avgLinesPerMethod);

        int totalFields = facts.getTypes().stream().mapToInt(t -> t.getFields().size()).sum();
        double avgFields = nbClasses > 0 ? (double) totalFields / nbClasses : 0;
        System.out.printf(Locale.US, "7. Moyenne d'attributs par classe : %.2f\n", avgFields);

        List<TypeFact> top10Methods = getTop10Percent(facts.getTypes(), t -> t.getMethods().size());
        System.out.println("8. Les 10% des classes avec le plus de méthodes : " + 
            top10Methods.stream().map(TypeFact::getQualifiedName).collect(Collectors.joining(", ")));

        List<TypeFact> top10Fields = getTop10Percent(facts.getTypes(), t -> t.getFields().size());
        System.out.println("9. Les 10% des classes avec le plus d'attributs : " + 
            top10Fields.stream().map(TypeFact::getQualifiedName).collect(Collectors.joining(", ")));

        List<TypeFact> intersection = new ArrayList<>(top10Methods);
        intersection.retainAll(top10Fields);
        System.out.println("10. Classes dans les deux catégories précédentes : " + 
            intersection.stream().map(TypeFact::getQualifiedName).collect(Collectors.joining(", ")));

        List<TypeFact> classesMoreThanX = facts.getTypes().stream()
                .filter(t -> t.getMethods().size() > x)
                .collect(Collectors.toList());
        System.out.println("11. Classes avec plus de X=" + x + " méthodes : " + 
            classesMoreThanX.stream().map(TypeFact::getQualifiedName).collect(Collectors.joining(", ")));

        System.out.println("12. Les 10% des méthodes avec le plus de lignes par classe :");
        for (TypeFact type : facts.getTypes()) {
            List<MethodFact> top10ClassMethods = getTop10Percent(type.getMethods(), MethodFact::getBodyLoc);
            if (!top10ClassMethods.isEmpty()) {
                System.out.println("    - " + type.getQualifiedName() + " : " + 
                    top10ClassMethods.stream().map(MethodFact::getName).collect(Collectors.joining(", ")));
            }
        }

        int maxParams = 0;
        List<MethodFact> maxParamMethods = new ArrayList<>();
        for (TypeFact type : facts.getTypes()) {
            for (MethodFact method : type.getMethods()) {
                int paramCount = method.getParameterTypes().size();
                if (paramCount > maxParams) {
                    maxParams = paramCount;
                    maxParamMethods.clear();
                    maxParamMethods.add(method);
                } else if (paramCount == maxParams && paramCount > 0) {
                    maxParamMethods.add(method);
                }
            }
        }
        System.out.println("13. Nombre maximal de paramètres (" + maxParams + ") : " + 
            maxParamMethods.stream().map(MethodFact::getId).collect(Collectors.joining(", ")));
    }
}