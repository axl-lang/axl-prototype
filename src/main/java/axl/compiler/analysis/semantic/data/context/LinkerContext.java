package axl.compiler.analysis.semantic.data.context;

import axl.compiler.linker.NamespaceResolver;
import axl.compiler.linker.type.data.Type;
import axl.compiler.linker.type.TypeRegistry;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Data
public class LinkerContext {

    private String packageLocation;

    private TypeRegistry registry;

    private NamespaceResolver resolver;

    private Type context;

    private Stack<Type> classContexts = new Stack<>();

    private List<Type> processedClassContexts = new ArrayList<>();
}
