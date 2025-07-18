package axl.compiler.analysis.semantic.data.tree.elt.condition;

import axl.compiler.analysis.semantic.data.tree.DeclarationType;
import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.SemanticNode;
import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.analysis.semantic.data.tree.elt.DeclarationElt;
import axl.compiler.analysis.semantic.data.tree.elt.FrameElt;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;
import axl.compiler.codegen.Generator;
import lombok.Data;
import lombok.Getter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Iterator;
import java.util.Random;
import java.util.Stack;

@Data
public class ForeachElt extends SemanticNode implements Elt {

    private DeclarationElt declaration;

    private Value iterator;

    private FrameElt then;

    @Getter
    private boolean localFinally = false;

    @Getter
    private boolean globalFinally = false;

    private static int k = 0;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        Label loopStart = new Label();
        Label loopCheck = new Label();
        Label loopEnd = new Label();

        iterator.codegen(visitor, labels);

        String name = "foreachIterator" + k++;
        Generator.getScopes().peek().declare(
                name,
                SemanticAnalyzerImpl.getLinkerContext().getResolver().resolve("java.util.Iterator").orElseThrow(),
                DeclarationType.VAR
        );
        int iteratorIndex = Generator.getScopes().peek().getStackIndex(name);

        visitor.visitVarInsn(Opcodes.ASTORE, iteratorIndex);

        visitor.visitLabel(loopCheck);

        visitor.visitVarInsn(Opcodes.ALOAD, iteratorIndex);

        visitor.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "java/util/Iterator",
                "hasNext",
                "()Z",
                true
        );

        visitor.visitJumpInsn(Opcodes.IFEQ, loopEnd);

        visitor.visitLabel(loopStart);

        visitor.visitVarInsn(Opcodes.ALOAD, iteratorIndex);

        visitor.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "java/util/Iterator",
                "next",
                "()Ljava/lang/Object;",
                true
        );

        Generator.getScopes().peek().declare(declaration.getName().getValue(), declaration.getType(), declaration.getDeclarationType());
        int varIndex = Generator.getScopes().peek().getStackIndex(declaration.getName().getValue());

        visitor.visitVarInsn(Opcodes.ASTORE, varIndex);

        then.codegen(visitor, labels);

        visitor.visitJumpInsn(Opcodes.GOTO, loopCheck);

        visitor.visitLabel(loopEnd);
    }

}
