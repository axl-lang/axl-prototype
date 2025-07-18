package axl.compiler.linker;

import axl.compiler.linker.type.data.ParameterizedVirtualType;
import axl.compiler.linker.type.data.Type;
import axl.compiler.linker.type.TypeRegistry;
import lombok.Getter;

import java.util.*;

public class NamespaceResolver {

    @Getter
    private static NamespaceResolver instance;

    {
        instance = this;
    }

    private final TypeRegistry registry;

    private final Map<String, String> explicitImports = new HashMap<>();

    private final List<String> wildcardImports = new ArrayList<>();

    public NamespaceResolver(TypeRegistry registry) {
        this.registry = registry;
    }

    public record ParsedType(NamespaceResolver resolver, String raw, List<String> typeArguments) {

        public Optional<Type> of(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Type> type) {
            if (type.isEmpty())
                return type;

            if (typeArguments.isEmpty())
                return type;

            return Optional.of(new ParameterizedVirtualType(
                    type.get(),
                    typeArguments.stream()
                            .map(resolver::resolve)
                            .map(Optional::orElseThrow)
                            .map(Object::toString)
                            .toList()
            ));
        }
    }

    public ParsedType parseParameterized(String type) {
        int lt = type.indexOf('<');
        if (lt < 0) {
            return new ParsedType(this, type.trim(), List.of());
        }

        String raw = type.substring(0, lt).trim();
        String args = type.substring(lt + 1, type.lastIndexOf('>')).trim();
        List<String> typeArgs = new ArrayList<>();

        int depth = 0;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < args.length(); i++) {
            char c = args.charAt(i);
            if (c == '<') {
                depth++;
                current.append(c);
            } else if (c == '>') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                typeArgs.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            typeArgs.add(current.toString().trim());
        }

        return new ParsedType(this, raw, typeArgs);
    }


    public void addImport(String importString) {
        if (importString.endsWith(".*")) {
            wildcardImports.add(importString.substring(0, importString.length() - 2));
        } else {
            String simpleName = importString.substring(importString.lastIndexOf('.') + 1);
            explicitImports.put(simpleName, importString);
        }
    }

    public Optional<Type> resolve(String qualifiedAlias) {
        ParsedType parsedType = parseParameterized(qualifiedAlias);

        if (qualifiedAlias.contains(".")) {
            return parsedType.of(registry.resolve(parsedType.raw()));
        }

        if (explicitImports.containsKey(parsedType.raw())) {
            return parsedType.of(registry.resolve(explicitImports.get(parsedType.raw())));
        }

        for (String pkg : wildcardImports) {
            Optional<Type> t = registry.resolve(pkg + "." + parsedType.raw());

            if (t.isPresent())
                return parsedType.of(t);
        }

        return parsedType.of(registry.resolve(parsedType.raw()));
    }
}
