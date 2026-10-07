package hai913i.tp1.model;

import java.util.ArrayList;
import java.util.List;

public class ProjectFacts {
    private List<TypeFact> types;

    public ProjectFacts() {
        this.types = new ArrayList<>();
    }

    public List<TypeFact> getTypes() { return types; }

    public void addType(TypeFact type) {
        this.types.add(type);
    }
}