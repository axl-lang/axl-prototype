package axl.compiler.analysis.semantic.impl;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.parser.data.declaration.ModuleDeclaration;
import axl.compiler.analysis.semantic.SemanticAnalyzer;
import axl.compiler.analysis.semantic.data.context.LinkerContext;
import axl.compiler.analysis.semantic.data.scope.Scope;
import axl.compiler.analysis.semantic.data.tree.symbol.ModuleSymbol;
import axl.compiler.analysis.semantic.impl.linker.*;
import axl.compiler.analysis.semantic.impl.semantic.type.impl.TypeVisitorImpl;
import axl.compiler.analysis.semantic.impl.transform.elt.EltVisitor;
import axl.compiler.analysis.semantic.impl.transform.elt.FrameEltVisitor;
import axl.compiler.analysis.semantic.impl.transform.symbol.ClassSymbolVisitor;
import axl.compiler.analysis.semantic.impl.transform.symbol.DeclarationSymbolVisitor;
import axl.compiler.analysis.semantic.impl.transform.symbol.FunctionSymbolVisitor;
import axl.compiler.analysis.semantic.impl.transform.symbol.ModuleSymbolVisitor;
import axl.compiler.linker.FieldDescriptor;
import axl.compiler.linker.MethodDescriptor;
import axl.compiler.linker.type.data.ParameterizedVirtualType;
import axl.compiler.linker.type.data.Type;
import axl.compiler.linker.type.data.VirtualType;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

public class SemanticAnalyzerImpl implements SemanticAnalyzer {

    @Getter
    private static LinkerContext linkerContext = new LinkerContext();

    @Getter
    private static Stack<Scope> scopes = new Stack<>();

    @Getter
    private static Stack<Type> context = new Stack<>();

    @SneakyThrows
    public LinkerContext analyze(ModuleDeclaration declaration) {
        new TreeAnalyzer(List.of(
                new ModuleContextVisitor(),
                new ClassContextVisitor(),
                new FunctionContextVisitor(),
                new ValContextVisitor(),
                new VarContextVisitor()
        )).analyze(declaration);

        linkerContext.getRegistry().loadFromJavaBase();

        getTypeConsumer(linkerContext).accept(linkerContext.getContext());
        linkerContext.getProcessedClassContexts().forEach(getTypeConsumer(linkerContext));
        return linkerContext;
    }

    @Override
    public ModuleSymbol transform(ModuleDeclaration declaration) {
        ModuleSymbol moduleSymbol = new ModuleSymbol();

        new TreeAnalyzer(List.of(
                new ModuleSymbolVisitor(moduleSymbol),
                new ClassSymbolVisitor(),
                new DeclarationSymbolVisitor(),
                new FunctionSymbolVisitor(),
                new EltVisitor(),
                new FrameEltVisitor()
        )).analyze(declaration);

        return moduleSymbol;
    }

    public ModuleSymbol typed(ModuleSymbol symbol) {
        new TreeAnalyzer(List.of(
                new TypeVisitorImpl()
        )).analyze(symbol);

        return symbol;
    }

    private static Consumer<Type> getTypeConsumer(LinkerContext linkerContext) {
        Function<String, String> resolveType = t ->
                linkerContext.getResolver().resolve(t).orElseThrow(() -> new IllegalStateException("Cannot resolve: " + t)).toString();

        Function<List<String>, List<String>> resolveList = list ->
                list != null ? list.stream().map(resolveType).toList() : null;

        Function<Map<String, List<String>>, Map<String, List<String>>> resolveMap = map -> {
            if (map == null) return null;
            Map<String, List<String>> result = new HashMap<>();
            for (var entry : map.entrySet()) {
                result.put(resolveType.apply(entry.getKey()), resolveList.apply(entry.getValue()));
            }
            return result;
        };

        Function<FieldDescriptor, FieldDescriptor> processField = field -> {
            field.setType(resolveType.apply(field.getType()));
            return field;
        };

        Function<MethodDescriptor, MethodDescriptor> processMethod = method -> {
            method.setReturnType(resolveType.apply(method.getReturnType()));
            method.setParameters(resolveList.apply(method.getParameters()));
            return method;
        };

        return (Type type) -> {
            if (type instanceof VirtualType t) {
                t.setSuperClass(resolveType.apply(t.getSuperClass()));
                t.setSuperClassTypeParameters(resolveList.apply(t.getSuperClassTypeParameters()));
                t.setInterfaces(resolveList.apply(t.getInterfaces()));
                t.setInterfacesTypeParameters(resolveMap.apply(t.getInterfacesTypeParameters()));
                t.setFields(t.getFields().stream().map(processField).toList());
                t.setMethods(t.getMethods().stream().map(processMethod).toList());
                t.setTypeParameters(resolveList.apply(t.getTypeParameters()));

                if (t instanceof ParameterizedVirtualType pt) {
                    pt.setTypeArguments(resolveList.apply(pt.getTypeArguments()));
                }
            }
        };
    }
}
