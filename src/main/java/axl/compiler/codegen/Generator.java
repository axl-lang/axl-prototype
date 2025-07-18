package axl.compiler.codegen;

import axl.compiler.analysis.semantic.data.scope.Scope;
import axl.compiler.analysis.semantic.data.tree.SemanticNode;
import lombok.Getter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Generator {

    @Getter
    private static Stack<ClassVisitor> classVisitors = new Stack<>();

    @Getter
    private static Stack<MethodVisitor> methodVisitors = new Stack<>();

    @Getter
    private static Stack<Label> labels = new Stack<>();

    @Getter
    private static Stack<Scope> scopes = new Stack<>();
}
