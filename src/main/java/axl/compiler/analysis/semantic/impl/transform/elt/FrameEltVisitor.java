package axl.compiler.analysis.semantic.impl.transform.elt;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.parser.data.Statement;
import axl.compiler.analysis.parser.data.statement.BodyStatement;
import axl.compiler.analysis.semantic.data.tree.elt.FrameElt;
import axl.compiler.analysis.semantic.impl.transform.TransformVisitor;

import java.util.ArrayList;

public class FrameEltVisitor implements TransformVisitor {

    @Override
    public void enter(TreeAnalyzer treeAnalyzer, Object node) {
        if (!(node instanceof BodyStatement statement))
            return;

        FrameElt elt = new FrameElt();
        accept(statement, elt);

        elt.setValue(new ArrayList<>());
        for (Statement stmt: statement.getStatements()) {
            setParent(stmt, elt);
            treeAnalyzer.enqueue(stmt);
        }
    }
}
