package hai913i.tp1.visitor;

import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class StructureVisitor extends ASTVisitor {
    private int nbClasses = 0;
    private int nbEnums = 0;
    private int nbInterfaces = 0;
    private int nbMethodes = 0;
    private int nbConstructors = 0;
    private int nbAttributs = 0;

    private void traiterType(AbstractTypeDeclaration node, String genre) {
        if (node instanceof TypeDeclaration && node.isLocalTypeDeclaration()) {
            return;
        }

        switch (genre) {
            case "interface":
                nbInterfaces++;
                break;
            case "énumération":
                nbEnums++;
                break;
            default:
                nbClasses++;
                break;
        }

        ITypeBinding binding = node.resolveBinding();
        if (binding != null) {
            System.out.println("\n--- " + genre.toUpperCase() + " : " + binding.getQualifiedName() + " ---");

            ITypeBinding superclass = binding.getSuperclass();
            while (superclass != null && !superclass.getQualifiedName().equals("java.lang.Object")) {
                System.out.println("  Hérite de : " + superclass.getQualifiedName());
                superclass = superclass.getSuperclass();
            }
        }

        List<BodyDeclaration> membres = node.bodyDeclarations();

        for (BodyDeclaration membre : membres) {

            if (membre instanceof FieldDeclaration) {
                FieldDeclaration field = (FieldDeclaration) membre;
                nbAttributs += field.fragments().size();

                int modifiers = field.getModifiers();
                String visibilite = Modifier.isPrivate(modifiers) ? "private" :
                        Modifier.isProtected(modifiers) ? "protected" :
                                Modifier.isPublic(modifiers) ? "public" : "package";

                String typeDeclare = field.getType().toString();

                for (Object obj : field.fragments()) {
                    VariableDeclarationFragment fragment = (VariableDeclarationFragment) obj;
                    System.out.println("  Attribut : " + fragment.getName() +
                            " | Type : " + typeDeclare +
                            " | Visibilité : " + visibilite);
                }
            }

            else if (membre instanceof MethodDeclaration) {
                MethodDeclaration method = (MethodDeclaration) membre;
                nbMethodes++;

                if (method.isConstructor()) {
                    nbConstructors++;
                }

                System.out.println("  Méthode : " + method.getName() +
                        " | Params : " + method.parameters().size() +
                        (method.isConstructor() ? " | Constructeur " : ""));
            }
        }
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        String genre = node.isInterface() ? "interface" : "classe";
        traiterType(node, genre);
        return true;
    }

     @Override
     public boolean visit(EnumDeclaration node) {
         traiterType(node, "énumération");
        return true;
     }

     @Override
     public boolean visit(RecordDeclaration node) {
         traiterType(node, "record");
        return true;
     }

     @Override
     public boolean visit(AnonymousClassDeclaration node) {
        return false;
     }

    public int getNbClasses() {
        return nbClasses;
    }

    public int getNbEnums() {
        return nbEnums;
    }

    public int getNbInterfaces() {
        return nbInterfaces;
    }

    public int getNbMethodes() {
        return nbMethodes;
    }

    public int getNbAttributes() {
        return nbAttributs;
    }

    public int getNbConstructors() {
        return nbConstructors;
    }
}