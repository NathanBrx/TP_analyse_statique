package hai913i.tp1.model;

import java.util.ArrayList;
import java.util.List;

public class TypeFact {
    private String qualifiedName;
    private String packageName;
    private String kind;
    private List<String> superclasses;
    private List<String> interfaces;
    private List<FieldFact> fields;
    private List<MethodFact> methods;

    public TypeFact(String qualifiedName, String packageName, String kind) {
        this.qualifiedName = qualifiedName;
        this.packageName = packageName;
        this.kind = kind;
        this.superclasses = new ArrayList<>();
        this.interfaces = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
    }

    public String getQualifiedName() { return qualifiedName; }
    public String getPackageName() { return packageName; }
    public String getKind() { return kind; }
    public List<String> getSuperclasses() { return superclasses; }
    public List<String> getInterfaces() { return interfaces; }
    public List<FieldFact> getFields() { return fields; }
    public List<MethodFact> getMethods() { return methods; }

    public void addField(FieldFact field) { this.fields.add(field); }
    public void addMethod(MethodFact method) { this.methods.add(method); }
    public void addSuperclass(String superclass) { this.superclasses.add(superclass); }
    public void addInterface(String iface) { this.interfaces.add(iface); }
}