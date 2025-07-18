package axl.compiler.analysis.semantic.data.tree.symbol;

import axl.compiler.analysis.semantic.data.tree.SemanticNode;
import axl.compiler.analysis.semantic.data.tree.Symbol;
import axl.compiler.analysis.semantic.data.tree.symbol.descriptor.FieldDescriptor;
import axl.compiler.analysis.semantic.data.tree.symbol.descriptor.FunctionDescriptor;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;
import lombok.Data;
import lombok.Getter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.objectweb.asm.Opcodes.*;

@Data
public class ModuleSymbol extends SemanticNode implements Symbol {

    @Getter
    private static MethodVisitor methodVisitor;

    private List<Symbol> symbols;

    public List<GeneratedClass> codegen() {
        List<GeneratedClass> result = new ArrayList<>();

        String internalName = SemanticAnalyzerImpl.getLinkerContext().getContext().getInternalName();

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V17, ACC_PUBLIC, internalName, null, "java/lang/Object", null);

        generateDefaultConstructor(cw);

        for (Symbol symbol : symbols) {
            if (symbol instanceof ClassSymbol classSymbol) {
                result.add(codegen(classSymbol));
                continue;
            }

            switch (symbol) {
                case DeclarationSymbol declarationSymbol -> {
                    FieldDescriptor field = new FieldDescriptor();
                    field.setAccessFlags(declarationSymbol.getAccessFlag());
                    field.setName(declarationSymbol.getName().getValue());
                    field.setType(declarationSymbol.getType());

                    cw.visitField(
                            field.getAccessFlags(),
                            field.getName(),
                            field.getType().getName().replace('.', '/'),
                            null,
                            null
                    ).visitEnd();
                }
                case FunctionSymbol functionSymbol -> {
                    FunctionDescriptor desc = functionSymbol.getDescriptor();

                    String methodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(
                            org.objectweb.asm.Type.getType(desc.getReturnType().getDescriptor()),
                            desc.getParameterTypes().stream().map(t -> org.objectweb.asm.Type.getType(t.getDescriptor())).toArray(org.objectweb.asm.Type[]::new)
                    );

                    MethodVisitor mv = cw.visitMethod(
                            ACC_PUBLIC + ACC_STATIC,
                            desc.getName(),
                            methodDescriptor,
                            null,
                            null
                    );

                    methodVisitor = mv;

                    mv.visitCode();
                    functionSymbol.getBody().codegen(mv, new Stack<>());
                    mv.visitInsn(RETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                }
                default -> throw new IllegalStateException("Unexpected value: " + symbol);
            }
        }

        cw.visitEnd();
        result.add(new GeneratedClass(internalName, cw));

        return result;
    }

    private GeneratedClass codegen(ClassSymbol classSymbol) {
        String internalName = classSymbol.getType().getInternalName();

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V17, ACC_PUBLIC, internalName, null, "java/lang/Object", null);

        generateDefaultConstructor(cw);

        for (Symbol symbol : classSymbol.getSymbols()) {
            switch (symbol) {
                case DeclarationSymbol declarationSymbol -> {
                    FieldDescriptor field = new FieldDescriptor();
                    field.setAccessFlags(declarationSymbol.getAccessFlag());
                    field.setName(declarationSymbol.getName().getValue());
                    field.setType(declarationSymbol.getType());

                    cw.visitField(
                            field.getAccessFlags(),
                            field.getName(),
                            field.getType().getName().replace('.', '/'),
                            null,
                            null
                    ).visitEnd();
                }
                case FunctionSymbol functionSymbol -> {
                    FunctionDescriptor desc = functionSymbol.getDescriptor();

                    String methodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(
                            org.objectweb.asm.Type.getType(desc.getReturnType().getDescriptor()),
                            desc.getParameterTypes().stream().map(t -> org.objectweb.asm.Type.getType(t.getDescriptor())).toArray(org.objectweb.asm.Type[]::new)
                    );

                    MethodVisitor mv = cw.visitMethod(
                            desc.getAccessFlags(),
                            desc.getName(),
                            methodDescriptor,
                            null,
                            null
                    );
                    methodVisitor = mv;

                    mv.visitCode();
                    functionSymbol.getBody().codegen(mv, new Stack<>());
                    mv.visitMaxs(-1, -1);
                    mv.visitEnd();
                }
                default -> throw new IllegalStateException("Unexpected value: " + symbol);
            }
        }

        cw.visitEnd();

        return new GeneratedClass(internalName, cw);
    }

    private void generateDefaultConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    public record GeneratedClass(String internalName, ClassWriter cw) {}
}
