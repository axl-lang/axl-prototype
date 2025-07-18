package axl.compiler.analysis.common.util;

public interface Visitor {

    default void enter(TreeAnalyzer treeAnalyzer, Object node) {
    }

    default void exit(TreeAnalyzer treeAnalyzer, Object node) {
    }
}

