package axl.compiler.codegen;

import axl.compiler.analysis.common.util.Visitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Stack;

public interface GenerationVisitor extends Visitor {

    default Stack<ClassVisitor> classVisitors() {
        return Generator.getClassVisitors();
    }

    default Stack<MethodVisitor> methodVisitors() {
        return Generator.getMethodVisitors();
    }

    default Stack<Label> labels() {
        return Generator.getLabels();
    }
}
