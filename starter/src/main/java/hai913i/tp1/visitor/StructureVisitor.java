package hai913i.tp1.visitor;

import org.eclipse.jdt.core.dom.*;

public class StructureVisitor extends ASTVisitor {
    private int nbTypes = 0;
    private int nbMethodes = 0;
    private int nbAttributs = 0;

    @Override
    public boolean visit(TypeDeclaration node) {
        if (!node.isLocalTypeDeclaration()) {
            nbTypes++;
        }
        return true;
    }

     @Override
     public boolean visit(EnumDeclaration node) {
        nbTypes++;
        return true;
     }

     @Override
     public boolean visit(RecordDeclaration node) {
        nbTypes++;
        return true;
     }

     @Override
     public boolean visit(FieldDeclaration node) {
        nbAttributs += node.fragments().size();
        return true;
     }

     @Override
     public boolean visit(MethodDeclaration node) {
        nbMethodes++;
        return true;
     }

     @Override
     public boolean visit(AnonymousClassDeclaration node) {
        return false;
     }

    public int getNbTypes() {
        return nbTypes;
    }

    public int getNbMethodes() {
        return nbMethodes;
    }

    public int getNbAttributes() {
        return nbAttributs;
    }
}