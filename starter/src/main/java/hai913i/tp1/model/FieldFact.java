package hai913i.tp1.model;

public class FieldFact {
    private String name;
    private String declaredType;
    private String visibility;

    public FieldFact(String name, String declaredType, String visibility) {
        this.name = name;
        this.declaredType = declaredType;
        this.visibility = visibility;
    }

    public String getName() { return name; }
    public String getDeclaredType() { return declaredType; }
    public String getVisibility() { return visibility; }
}