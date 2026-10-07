package hai913i.tp1.visitor;

import hai913i.tp1.model.*;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class StructureVisitor extends ASTVisitor {
    private ProjectFacts facts;
    private CompilationUnit unit;

    public StructureVisitor(ProjectFacts facts, CompilationUnit unit) {
        this.facts = facts;
        this.unit = unit;
    }

    private void traiterType(AbstractTypeDeclaration node, String genre) {
        if (node instanceof TypeDeclaration && node.isLocalTypeDeclaration()) return;

        ITypeBinding binding = node.resolveBinding();
        if (binding == null) return;

        String packageName = binding.getPackage() != null ? binding.getPackage().getName() : "(défaut)";
        TypeFact typeFact = new TypeFact(binding.getQualifiedName(), packageName, genre);

        List<BodyDeclaration> membres = node.bodyDeclarations();
        for (BodyDeclaration membre : membres) {

            if (membre instanceof FieldDeclaration) {
                FieldDeclaration field = (FieldDeclaration) membre;
                String typeDeclare = field.getType().toString();

                int modifiers = field.getModifiers();
                String visibilite = Modifier.isPrivate(modifiers) ? "private" : Modifier.isProtected(modifiers) ? "protected" : Modifier.isPublic(modifiers) ? "public" : "package";

                for (Object obj : field.fragments()) {
                    VariableDeclarationFragment fragment = (VariableDeclarationFragment) obj;
                    typeFact.addField(new FieldFact(fragment.getName().getIdentifier(), typeDeclare, visibilite));
                }
            }

            else if (membre instanceof MethodDeclaration) {
                MethodDeclaration method = (MethodDeclaration) membre;
                IMethodBinding methodBinding = method.resolveBinding();

                List<String> paramTypes = new ArrayList<>();
                StringBuilder methodIdBuilder = new StringBuilder(binding.getQualifiedName() + "#" + method.getName().getIdentifier() + "(");

                if (methodBinding != null) {
                    ITypeBinding[] pt = methodBinding.getParameterTypes();
                    for (int i = 0; i < pt.length; i++) {
                        String paramName = pt[i].getErasure().getQualifiedName();
                        paramTypes.add(paramName);
                        methodIdBuilder.append(paramName);
                        if (i < pt.length - 1) methodIdBuilder.append(",");
                    }
                }
                methodIdBuilder.append(")");
                String methodId = methodIdBuilder.toString();

                int bodyLoc = 0;
                if (method.getBody() != null) {
                    int startPosition = method.getBody().getStartPosition();
                    int length = method.getBody().getLength();
                    int startLine = unit.getLineNumber(startPosition);
                    int endLine = unit.getLineNumber(startPosition + length - 1);
                    bodyLoc = endLine - startLine + 1;
                }

                typeFact.addMethod(new MethodFact(
                        methodId, method.getName().getIdentifier(), paramTypes, method.isConstructor(), bodyLoc
                ));
            }
        }

        facts.addType(typeFact);
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
     public boolean visit(AnonymousClassDeclaration node) {
        return false;
     }
}