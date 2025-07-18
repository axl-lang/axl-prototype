package axl.compiler.codegen.impl;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.semantic.data.tree.Symbol;
import axl.compiler.analysis.semantic.data.tree.symbol.*;
import axl.compiler.analysis.semantic.data.tree.symbol.descriptor.FieldDescriptor;
import axl.compiler.analysis.semantic.data.tree.symbol.descriptor.FunctionDescriptor;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;
import axl.compiler.codegen.GenerationVisitor;
import org.objectweb.asm.*;

public class SymbolVisitor implements GenerationVisitor, Opcodes {

    @Override
    public void enter(TreeAnalyzer analyzer, Object node) {
        if (!(node instanceof Symbol symbol)) return;

        switch (symbol) {
            case ModuleSymbol moduleSymbol -> {
                String internalName = SemanticAnalyzerImpl.getLinkerContext().getContext().getInternalName();

                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                cw.visit(V1_8, ACC_PUBLIC, internalName, null, "java/lang/Object", null);

                classVisitors().push(cw);
            }
            case ClassSymbol classSymbol -> {
                String internalName = classSymbol.getType().getInternalName();

                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                cw.visit(V1_8, ACC_PUBLIC, internalName, null, "java/lang/Object", null);

                classVisitors().push(cw);
            }
            case FunctionSymbol functionSymbol -> {
                FunctionDescriptor desc = functionSymbol.getDescriptor();

                String methodDescriptor = Type.getMethodDescriptor(
                        Type.getType(desc.getReturnType().getDescriptor()),
                        desc.getParameterTypes().stream().map(t -> Type.getType(t.getDescriptor())).toArray(Type[]::new)
                );

                MethodVisitor mv = classVisitors().peek().visitMethod(
                        desc.getAccessFlags(),
                        desc.getName(),
                        methodDescriptor,
                        null,
                        null
                );

                mv.visitCode();
                methodVisitors().push(mv);
            }
            case DeclarationSymbol declarationSymbol -> {
                switch (declarationSymbol.getDeclarationType()) {
                    case VAL, VAR -> {
                        FieldDescriptor field = new FieldDescriptor();
                        field.setAccessFlags(declarationSymbol.getAccessFlag());
                        field.setName(declarationSymbol.getName().getValue());
                        field.setType(declarationSymbol.getType());

                        classVisitors().peek().visitField(
                                field.getAccessFlags(),
                                field.getName(),
                                field.getType().getName().replace('.', '/'),
                                null,
                                null
                        ).visitEnd();
                    }

                    default -> {}
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + symbol);
        }
    }

    @Override
    public void exit(TreeAnalyzer analyzer, Object node) {
        if (!(node instanceof Symbol symbol)) return;

        switch (symbol) {
            case ModuleSymbol moduleSymbol -> {
                ClassVisitor cw = classVisitors().pop();
                cw.visitEnd();
            }
            case ClassSymbol classSymbol -> {
                ClassVisitor cw = classVisitors().pop();
                cw.visitEnd();
            }
            case FunctionSymbol functionSymbol -> {
                MethodVisitor mv = methodVisitors().pop();

                mv.visitMaxs(-1, -1);
                mv.visitEnd();
            }
            case DeclarationSymbol ignored -> {
            }
            default -> throw new IllegalStateException("Unexpected value: " + symbol);
        }
    }
}
