package axl.compiler.analysis.semantic.data.tree.value;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;
import axl.compiler.linker.MethodDescriptor;
import axl.compiler.linker.type.TypeRegistry;
import axl.compiler.linker.type.data.Type;
import axl.compiler.linker.type.data.TypeKind;
import lombok.Data;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Stack;

@Data
public class InvokeStaticValue extends Value implements Elt {

    private Type source;

    private Token name;

    private List<Value> arguments;

    private TypeRegistry.ResolvedMethod method;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        MethodDescriptor descriptor = method.method();
        String owner = method.declaringType().getInternalName();
        String methodName = descriptor.getName();

        for (Value arg : arguments) {
            arg.codegen(visitor, labels);
        }

        StringBuilder descriptorBuilder = new StringBuilder();
        descriptorBuilder.append('(');
        for (String param : descriptor.getParameters()) {
            descriptorBuilder.append(SemanticAnalyzerImpl.getLinkerContext().getResolver().resolve(param).orElseThrow().getDescriptor());
        }
        descriptorBuilder.append(')');
        descriptorBuilder.append(SemanticAnalyzerImpl.getLinkerContext().getResolver().resolve(descriptor.getReturnType()).orElseThrow().getDescriptor());

        String methodDescriptor = descriptorBuilder.toString();

        visitor.visitMethodInsn(Opcodes.INVOKESTATIC, owner, methodName, methodDescriptor, method.declaringType().getKind() == TypeKind.INTERFACE);
    }
}
