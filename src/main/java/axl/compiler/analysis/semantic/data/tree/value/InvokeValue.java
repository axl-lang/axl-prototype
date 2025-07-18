package axl.compiler.analysis.semantic.data.tree.value;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.semantic.data.tree.Elt;
import axl.compiler.analysis.semantic.data.tree.Value;
import axl.compiler.linker.MethodDescriptor;
import axl.compiler.linker.NamespaceResolver;
import axl.compiler.linker.type.TypeRegistry;
import axl.compiler.linker.type.data.TypeKind;
import axl.compiler.linker.type.data.VirtualType;
import lombok.Data;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Stack;

@Data
public class InvokeValue extends Value implements Elt {

    private Value source;

    private Token name;

    private List<Value> arguments;

    private TypeRegistry.ResolvedMethod method;

    @Override
    public void codegen(MethodVisitor visitor, Stack<Label> labels) {
        MethodDescriptor descriptor = method.method();
        VirtualType owner = method.declaringType();

        String ownerInternalName = owner.getInternalName();
        String methodName = descriptor.getName();

        if ((descriptor.getAccess() & Opcodes.ACC_STATIC) != Opcodes.ACC_STATIC)
            source.codegen(visitor, labels);

        for (Value arg : arguments) {
            arg.codegen(visitor, labels);
        }

        StringBuilder descriptorBuilder = new StringBuilder();
        descriptorBuilder.append('(');
        for (String param : descriptor.getParameters()) {
            descriptorBuilder.append(NamespaceResolver.getInstance().resolve(param).orElseThrow().getDescriptor());
        }
        descriptorBuilder.append(')');
        descriptorBuilder.append(NamespaceResolver.getInstance().resolve(descriptor.getReturnType()).orElseThrow().getDescriptor());
        String methodDescriptor = descriptorBuilder.toString();

        boolean isInterface = owner.getKind() == TypeKind.INTERFACE;
        visitor.visitMethodInsn(
                isInterface ? Opcodes.INVOKEINTERFACE : ((descriptor.getAccess() & Opcodes.ACC_STATIC) != Opcodes.ACC_STATIC ? Opcodes.INVOKEVIRTUAL : Opcodes.INVOKESTATIC),
                ownerInternalName,
                methodName,
                methodDescriptor,
                isInterface
        );
    }

}
