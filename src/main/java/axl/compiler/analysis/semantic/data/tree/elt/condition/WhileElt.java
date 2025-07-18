package axl.compiler.analysis.semantic.data.tree.elt.condition;

import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.SemanticNode;
import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.analysis.semantic.data.tree.elt.FrameElt;
import lombok.Data;
import lombok.Getter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Stack;

@Data
public class WhileElt extends SemanticNode implements Elt {

    private Value condition;

    private FrameElt then;

    @Getter
    private boolean localFinally = false;

    @Getter
    private boolean globalFinally = false;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        Label loopStart = new Label();
        Label loopEnd = new Label();

        labels.push(loopEnd);

        visitor.visitLabel(loopStart);

        condition.codegen(visitor, labels);
        visitor.visitJumpInsn(Opcodes.IFEQ, loopEnd);

        if (then != null) {
            then.codegen(visitor, labels);
        }

        visitor.visitJumpInsn(Opcodes.GOTO, loopStart);
        visitor.visitLabel(loopEnd);
        labels.pop();
    }

}
