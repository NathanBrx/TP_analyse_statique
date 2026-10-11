package hai913i.tp1.visitor;

import org.eclipse.jdt.core.dom.*;
import hai913i.tp1.model.*;
import java.util.Optional;

public class CallVisitor extends ASTVisitor {

    private CompilationUnit unit;
    private ProjectFacts facts;
    private String currentMethodId = "Hors méthode";

    public CallVisitor(CompilationUnit unit, ProjectFacts facts) {
        this.unit = unit;
        this.facts = facts;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (currentMethodId.equals("Hors méthode")) {
            IMethodBinding binding = node.resolveBinding();
            if (binding != null) {
                StringBuilder idBuilder = new StringBuilder(binding.getDeclaringClass().getQualifiedName() + "#" + binding.getName() + "(");
                ITypeBinding[] pt = binding.getParameterTypes();
                for (int i = 0; i < pt.length; i++) {
                    idBuilder.append(pt[i].getErasure().getQualifiedName());
                    if (i < pt.length - 1) idBuilder.append(",");
                }
                idBuilder.append(")");
                currentMethodId = idBuilder.toString();
            } else {
                currentMethodId = node.getName().getIdentifier() + "()";
            }
        }
        return true;
    }

    @Override
    public void endVisit(MethodDeclaration node) {
        if (node.getParent() instanceof TypeDeclaration || node.getParent() instanceof EnumDeclaration) {
            currentMethodId = "Hors méthode";
        }
    }

    private void analyserAppel(Expression receveurExpr, IMethodBinding methodBinding, int startPosition, boolean isSuper) {
        int ligne = unit.getLineNumber(startPosition);

        boolean resolved = (methodBinding != null);
        String targetId = null;
        String typeReceveur = "Inconnu";
        boolean isInterne = false;

        if (resolved) {
            IMethodBinding cible = methodBinding.getMethodDeclaration();
            isInterne = cible.getDeclaringClass().isFromSource();

            StringBuilder targetIdBuilder = new StringBuilder(cible.getDeclaringClass().getQualifiedName() + "#" + cible.getName() + "(");
            ITypeBinding[] pt = cible.getParameterTypes();
            for (int i = 0; i < pt.length; i++) {
                targetIdBuilder.append(pt[i].getErasure().getQualifiedName());
                if (i < pt.length - 1) targetIdBuilder.append(",");
            }
            targetIdBuilder.append(")");
            targetId = targetIdBuilder.toString();

            if (receveurExpr != null && receveurExpr.resolveTypeBinding() != null) {
                typeReceveur = receveurExpr.resolveTypeBinding().getErasure().getQualifiedName();
            } else {
                if (Modifier.isStatic(cible.getModifiers())) {
                    typeReceveur = cible.getDeclaringClass().getQualifiedName();
                } else {
                    typeReceveur = cible.getDeclaringClass().getQualifiedName();
                }
            }
        }

        CallFact call = new CallFact(
                currentMethodId,
                Optional.ofNullable(targetId),
                typeReceveur,
                ligne,
                resolved,
                isInterne,
                isSuper
        );

        for (TypeFact type : facts.getTypes()) {
            for (MethodFact method : type.getMethods()) {
                if (method.getId().equals(currentMethodId)) {
                    method.addCall(call);
                    return;
                }
            }
        }
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (currentMethodId.equals("Hors méthode")) return false;

        Expression receveur = node.getExpression();
        IMethodBinding binding = node.resolveMethodBinding();

        analyserAppel(receveur, binding, node.getStartPosition(), false);
        return true;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        if (currentMethodId.equals("Hors méthode")) return false;

        IMethodBinding binding = node.resolveMethodBinding();

        analyserAppel(null, binding, node.getStartPosition(), true);
        return true;
    }
}