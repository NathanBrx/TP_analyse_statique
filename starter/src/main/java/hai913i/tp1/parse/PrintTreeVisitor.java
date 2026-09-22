package hai913i.tp1.parse;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class PrintTreeVisitor extends ASTVisitor {
    private int depth = 0;

    @Override
    public boolean preVisit2(ASTNode node) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + node.getClass().getSimpleName());
        depth++;
        return true;
    }

    @Override
    public void postVisit(ASTNode node) {
        depth--;
    }
}