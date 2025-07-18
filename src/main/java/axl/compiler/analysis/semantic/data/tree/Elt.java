package axl.compiler.analysis.semantic.data.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Stack;

public interface Elt {

    default boolean isGlobalFinally() {
        return false;
    }

    default boolean isLocalFinally() {
        return false;
    }

    void codegen(MethodVisitor visitor, Stack<Label> labels);
}
