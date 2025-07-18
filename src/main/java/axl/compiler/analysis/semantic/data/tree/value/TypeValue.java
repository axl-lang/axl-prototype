package axl.compiler.analysis.semantic.data.tree.value;

import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.linker.type.data.Type;
import lombok.Data;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Stack;

@Data
public class TypeValue extends Value implements Elt {

    private Type value;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        throw new UnsupportedOperationException();
    }
}
