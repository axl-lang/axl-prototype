package axl.compiler.linker.type;

import axl.compiler.linker.FieldDescriptor;
import axl.compiler.linker.MethodDescriptor;
import axl.compiler.linker.type.data.Type;
import axl.compiler.linker.type.data.TypeKind;
import axl.compiler.linker.type.data.VirtualType;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.*;

public class TypeClassVisitor extends ClassVisitor {

    private final Map<String, axl.compiler.linker.type.data.Type> types;

    private String className;

    private String superClass;

    private final List<String> superClassTypeParameters = new ArrayList<>();

    private final List<String> interfaces = new ArrayList<>();

    private final List<MethodDescriptor> methods = new ArrayList<>();

    private final List<FieldDescriptor> fields = new ArrayList<>();

    private final List<String> typeParameters = new ArrayList<>();

    private final Map<String, List<String>> interfacesTypeParameters = new HashMap<>();

    private int classAccess;

    public TypeClassVisitor(Map<String, Type> types) {
        super(Opcodes.ASM9);
        this.types = types;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] ifaces) {
        this.classAccess = access;
        this.className = name.replace('/', '.');
        this.superClass = superName != null ? superName.replace('/', '.') : null;

        if (ifaces != null) {
            for (String iface : ifaces) {
                interfaces.add(iface.replace('/', '.'));
            }
        }

        if (signature != null) {
            SignatureReader sr = new SignatureReader(signature);
            sr.accept(new SignatureVisitor(Opcodes.ASM9) {
                private int ifaceIndex = 0;

                @Override
                public void visitFormalTypeParameter(String name) {
                    typeParameters.add(name);
                }

                @Override
                public SignatureVisitor visitSuperclass() {
                    return new TypeArgCollector(superClassTypeParameters);
                }

                @Override
                public SignatureVisitor visitInterface() {
                    String ifaceName = interfaces.get(ifaceIndex++);
                    List<String> params = new ArrayList<>();
                    interfacesTypeParameters.put(ifaceName, params);
                    return new TypeArgCollector(params);
                }
            });
        }
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        String type = org.objectweb.asm.Type.getType(descriptor).getClassName();

        fields.add(new FieldDescriptor(name, type, access));
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        List<String> paramTypes = Arrays.stream(org.objectweb.asm.Type.getArgumentTypes(descriptor))
                .map(org.objectweb.asm.Type::getClassName)
                .toList();
        String returnType = org.objectweb.asm.Type.getReturnType(descriptor).getClassName();

        methods.add(new MethodDescriptor(name, paramTypes, returnType, access));
        return null;
    }

    @Override
    public void visitEnd() {
        TypeKind kind;
        if ((classAccess & Opcodes.ACC_ANNOTATION) != 0) kind = TypeKind.ANNOTATION;
        else if ((classAccess & Opcodes.ACC_ENUM) != 0) kind = TypeKind.ENUM;
        else if ((classAccess & Opcodes.ACC_INTERFACE) != 0) kind = TypeKind.INTERFACE;
        else if ((classAccess & Opcodes.ACC_ABSTRACT) != 0) kind = TypeKind.ABSTRACT_CLASS;
        else kind = TypeKind.CLASS;

        types.put(className, new VirtualType(
                className,
                superClass,
                superClassTypeParameters,
                interfaces,
                interfacesTypeParameters,
                methods,
                fields,
                typeParameters,
                kind
        ));
    }

    private static class TypeArgCollector extends SignatureVisitor {

        private final List<String> params;

        public TypeArgCollector(List<String> params) {
            super(Opcodes.ASM9);
            this.params = params;
        }

        @Override
        public void visitTypeArgument() {
            params.add("?");
        }

        @Override
        public SignatureVisitor visitTypeArgument(char wildcard) {
//            String prefix = switch (wildcard) {
//                case '+' -> "? extends ";
//                case '-' -> "? super ";
//                default -> "";
//            };

            String prefix = "";

            return new SignatureVisitor(Opcodes.ASM9) {
                @Override
                public void visitTypeVariable(String name) {
                    params.add(prefix + name);
                }

                @Override
                public void visitClassType(String name) {
                    params.add(prefix + name.replace("/", "."));
                }
            };
        }
    }
}
