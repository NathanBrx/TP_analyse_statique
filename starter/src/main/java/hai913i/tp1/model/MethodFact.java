package hai913i.tp1.model;

import java.util.ArrayList;
import java.util.List;

public class MethodFact {
    private String id;
    private String name;
    private List<String> parameterTypes;
    private boolean constructor;
    private int bodyLoc;
    private List<CallFact> calls;

    public MethodFact(String id, String name, List<String> parameterTypes, boolean constructor, int bodyLoc) {
        this.id = id;
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.constructor = constructor;
        this.bodyLoc = bodyLoc;
        this.calls = new ArrayList<>(); // Initialisé vide, à remplir par le CallVisitor
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<String> getParameterTypes() { return parameterTypes; }
    public boolean isConstructor() { return constructor; }
    public int getBodyLoc() { return bodyLoc; }
    public List<CallFact> getCalls() { return calls; }

    public void addCall(CallFact call) {
        this.calls.add(call);
    }
}