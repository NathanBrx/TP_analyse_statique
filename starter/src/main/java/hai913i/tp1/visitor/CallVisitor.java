package hai913i.tp1.visitor;

import org.eclipse.jdt.core.dom.*;

public class CallVisitor extends ASTVisitor {

    private CompilationUnit unit;
    private String fileName;
    private String currentCaller = "Hors méthode";

    private int nbMethodInvocations = 0;
    private int nbSuperMethodInvocations = 0;
    private int nbInternes = 0;
    private int nbExternes = 0;
    private int nbNonResolus = 0;

    public CallVisitor(CompilationUnit unit, String filename) {
        this.unit = unit;
        this.fileName = filename;
    }

    private void analyserAppel(Expression receveurExpr, IMethodBinding methodBinding, int startPosition, String nomAppel) {
        int ligne = unit.getLineNumber(startPosition);

        // appel de méthode non résolu
        if (methodBinding == null) {
            nbNonResolus++;
            System.out.println("Ligne " + ligne + " : " + nomAppel + " -> NON RÉSOLU");
            return;
        }

        IMethodBinding cible = methodBinding.getMethodDeclaration();
        String cibleQualifiee = cible.getDeclaringClass().getQualifiedName() + "#" + cible.getName();

        // type statique du receveur
        String typeReceveur = "Inconnu";
        if (receveurExpr != null) {
            ITypeBinding typeBinding = receveurExpr.resolveTypeBinding();
            if (typeBinding != null) {
                typeReceveur = typeBinding.getErasure().getQualifiedName();
            }
        } else {
            // pas de receveur explicite (this ou appel statique)
            if (Modifier.isStatic(cible.getModifiers())) {
                typeReceveur = cible.getDeclaringClass().getQualifiedName();
            } else {
                typeReceveur = cible.getDeclaringClass().getQualifiedName();
            }
        }

        // appel interne (dans le projet) ou externe (JDK, bibliothèques)
        boolean isInterne = cible.getDeclaringClass().isFromSource();
        if (isInterne) {
            nbInternes++;
        } else {
            nbExternes++;
        }

        System.out.println("[" + fileName + " | Ligne " + ligne + "] " + currentCaller + " ---> appelle ---> " + cibleQualifiee +
                "\n    (Receveur : " + typeReceveur + " | Nature : " + (isInterne ? "Interne" : "Externe") + ")\n");
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        IMethodBinding binding = node.resolveBinding();
        if (binding != null) {
            currentCaller = binding.getDeclaringClass().getName() + "#" + binding.getName();
        } else {
            currentCaller = node.getName().getIdentifier();
        }
        return true;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        nbMethodInvocations++;
        Expression receveur = node.getExpression();
        IMethodBinding binding = node.resolveMethodBinding();

        analyserAppel(receveur, binding, node.getStartPosition(), node.getName().getIdentifier());
        return true;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        nbSuperMethodInvocations++;
        IMethodBinding binding = node.resolveMethodBinding();

        analyserAppel(null, binding, node.getStartPosition(), "super." + node.getName().getIdentifier());
        return true;
    }

    public int getNbTotalAppels() { return nbMethodInvocations + nbSuperMethodInvocations; }
    public int getNbMethodInvocations() { return nbMethodInvocations; }
    public int getNbSuperMethodInvocations() { return nbSuperMethodInvocations; }
    public int getNbInternes() { return nbInternes; }
    public int getNbExternes() { return nbExternes; }
    public int getNbNonResolus() { return nbNonResolus; }
}