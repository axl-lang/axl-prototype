package axl.compiler.analysis.semantic.impl.linker;

import axl.compiler.analysis.common.util.TreeAnalyzer;
import axl.compiler.analysis.common.util.Visitor;
import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Declaration;
import axl.compiler.analysis.parser.data.declaration.LocationDeclaration;
import axl.compiler.analysis.parser.data.declaration.ModuleDeclaration;
import axl.compiler.analysis.semantic.data.context.LinkerContext;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;
import axl.compiler.linker.NamespaceResolver;
import axl.compiler.linker.type.data.TypeKind;
import axl.compiler.linker.type.TypeRegistry;
import axl.compiler.linker.type.data.VirtualType;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleContextVisitor implements Visitor {

    @Override
    @SneakyThrows
    public void enter(TreeAnalyzer treeAnalyzer, Object node) {
        if (!(node instanceof ModuleDeclaration declaration))
            return;

        LinkerContext context = SemanticAnalyzerImpl.getLinkerContext();

        TypeRegistry registry = new TypeRegistry();
        registry.loadFromJavaBase();
        context.setRegistry(registry);

        NamespaceResolver resolver = new NamespaceResolver(registry);
        context.setResolver(resolver);

        String basePackage = handleImports(declaration, resolver);
        context.setPackageLocation(basePackage);

        String fullModuleName = (basePackage.isEmpty()
                ? declaration.getFilename()
                : basePackage + "." + declaration.getFilename()) + "Module";

        context.setContext(createVirtualType(fullModuleName));

        registry.getTypes().put(fullModuleName, context.getContext());

        for (Declaration child : declaration.getDeclarations()) {
            treeAnalyzer.enqueue(child);
        }
    }

    private String handleImports(ModuleDeclaration declaration, NamespaceResolver resolver) {
        for (LocationDeclaration importDecl : declaration.getImportDeclarations()) {
            resolver.addImport(joinTokens(importDecl.getLocation()));
        }

        if (declaration.getPackageDeclaration() == null)
            return "";

        String basePackage = joinTokens(declaration.getPackageDeclaration().getLocation());
        resolver.addImport(basePackage + ".*");

        return basePackage;
    }

    private String joinTokens(List<Token> tokens) {
        return String.join(".", tokens.stream().map(Token::getValue).toList());
    }

    private VirtualType createVirtualType(String name) {
        return new VirtualType(
                name,
                "java.lang.Object",
                List.of(),
                List.of(),
                Map.of(),
                new ArrayList<>(),
                new ArrayList<>(),
                List.of(),
                TypeKind.CLASS
        );
    }
}
