package axl.compiler.linker.type;

import axl.compiler.linker.MethodDescriptor;
import axl.compiler.linker.type.data.ParameterizedVirtualType;
import axl.compiler.linker.type.data.PrimitiveType;
import axl.compiler.linker.type.data.Type;
import axl.compiler.linker.type.data.VirtualType;
import lombok.Getter;
import org.objectweb.asm.*;

import java.io.*;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public class TypeRegistry {

    private static final Map<String, List<String>> WIDENING_MAP = Map.ofEntries(
            Map.entry("byte", List.of("short", "int", "long", "float", "double")),
            Map.entry("short", List.of("int", "long", "float", "double")),
            Map.entry("char", List.of("int", "long", "float", "double")),
            Map.entry("int", List.of("long", "float", "double")),
            Map.entry("long", List.of("float", "double")),
            Map.entry("float", List.of("double"))
    );

    private static final Map<String, String> BOXING = Map.ofEntries(
//            Map.entry("int", "java.lang.Integer"),
//            Map.entry("long", "java.lang.Long"),
//            Map.entry("double", "java.lang.Double"),
//            Map.entry("float", "java.lang.Float"),
//            Map.entry("boolean", "java.lang.Boolean"),
//            Map.entry("char", "java.lang.Character"),
//            Map.entry("byte", "java.lang.Byte"),
//            Map.entry("short", "java.lang.Short")
    );

    @Getter
    private final Map<String, axl.compiler.linker.type.data.Type> types = new HashMap<>();

    private boolean javaBaseLoaded = false;

    public Optional<axl.compiler.linker.type.data.Type> resolve(String fqcn) {
        if (fqcn.contains("<")) fqcn = fqcn.substring(0, fqcn.indexOf('<'));
        return Optional.ofNullable(types.get(fqcn));
    }

    public void loadFromJavaBase() throws IOException {
        if (this.javaBaseLoaded)
            return;
        this.javaBaseLoaded = true;

        this.types.put("boolean", new PrimitiveType("boolean"));
        this.types.put("byte", new PrimitiveType("byte"));
        this.types.put("short", new PrimitiveType("short"));
        this.types.put("char", new PrimitiveType("char"));
        this.types.put("int", new PrimitiveType("int"));
        this.types.put("long", new PrimitiveType("long"));
        this.types.put("float", new PrimitiveType("float"));
        this.types.put("double", new PrimitiveType("double"));
        this.types.put("void", new PrimitiveType("void"));

        FileSystem jrt;
        try {
            jrt = FileSystems.getFileSystem(URI.create("jrt:/"));
        } catch (FileSystemNotFoundException e) {
            jrt = FileSystems.newFileSystem(URI.create("jrt:/"), Collections.emptyMap());
        }

        Path javaBase = jrt.getPath("/modules/java.base");
        if (!Files.exists(javaBase))
            return;

        Files.walk(javaBase)
                .filter(p -> p.toString().endsWith(".class"))
                .forEach(path -> {
                    try (InputStream is = Files.newInputStream(path)) {
                        loadClassFromStream(is);
                    } catch (IOException ignored) {
                    }
                });
    }

    public void loadFromJars(List<Path> jarPaths) throws IOException {
        for (Path path : jarPaths) {
            try (JarFile jar = new JarFile(path.toFile())) {
                jar.stream()
                        .filter(e -> e.getName().endsWith(".class"))
                        .forEach(e -> {
                            try (InputStream is = jar.getInputStream(e)) {
                                loadClassFromStream(is);
                            } catch (IOException ignored) {
                            }
                        });
            }
        }
    }

    public boolean isAssignable(String subType, String superType) {
        if (subType.contains("[") || superType.contains("]"))
            return false; // FIXME

        return getAllSupertypes(resolve(subType).orElseThrow())
                .contains(resolve(superType).orElseThrow());
    }

    public boolean isAssignable(Type subType, Type superType) {
        return getAllSupertypes(subType)
                .contains(superType);
    }

    public Optional<ResolvedMethod> resolveMethodDeep(VirtualType current, String name, List<String> args,
                                                      boolean isStaticCall, VirtualType caller) {
        record MethodMatch(MethodDescriptor method, VirtualType declaringType, int score) {}

        List<MethodMatch> matches = new ArrayList<>();

        //noinspection unchecked
        for (VirtualType type : (List<? extends VirtualType>) getAllSupertypes(current)) {
            Map<String, String> map = Map.of();
            if (type instanceof ParameterizedVirtualType parameterizedVirtualType) {
                map = buildGenericMapping(type.getTypeParameters(), parameterizedVirtualType.getTypeArguments());
            }

            for (MethodDescriptor method : type.getMethods()) {
                if (!method.getName().equals(name)) continue;
                if (Modifier.isStatic(method.getAccess()) != isStaticCall) continue;
                if (!isAccessible(method, caller, type)) continue;

                Map<String, String> finalMap = map;
                List<String> resolved = method.getParameters().stream()
                        .map(p -> substituteTypeVars(p, finalMap)).toList();

                int score = matchArguments(args, resolved);
                if (score >= 0) matches.add(new MethodMatch(method, type, score));
            }
        }

        return matches.stream()
                .min(Comparator.comparingInt(MethodMatch::score))
                .map(m -> new ResolvedMethod(m.method(), m.declaringType()));
    }

    public List<? extends axl.compiler.linker.type.data.Type> getAllSupertypes(Type base) {
        if (base instanceof PrimitiveType)
            return List.of(base);
        
        List<VirtualType> result = new ArrayList<>();
        Set<VirtualType> visited = new HashSet<>();
        collectSupertypes((VirtualType) base, result, visited);
        return result;
    }

    private void collectSupertypes(VirtualType current, List<VirtualType> result, Set<VirtualType> visited) {
        if (!visited.add(current))
            return;
        result.add(current);

        if (!(current instanceof ParameterizedVirtualType))
            current = new ParameterizedVirtualType(current, List.of());

        Map<String, String> map = buildGenericMapping(current.getTypeParameters(), current.getTypeArguments());

        if (current.getSuperClass() != null) {
            var mapped = current.getSuperClassTypeParameters().stream()
                    .map(s -> substituteTypeVars(s, map)).toList();
            var superDesc = new ParameterizedVirtualType(resolve(current.getSuperClass()).orElseThrow(), mapped);
            collectSupertypes(superDesc, result, visited);
        }

        for (String iface : current.getInterfaces()) {
            List<String> ifaceArgs = current.getInterfacesTypeParameters().getOrDefault(iface, List.of());
            List<String> mapped = ifaceArgs.stream()
                    .map(s -> substituteTypeVars(s, map)).toList();
            var ifaceDesc = new ParameterizedVirtualType(resolve(iface).orElseThrow(), mapped);
            collectSupertypes(ifaceDesc, result, visited);
        }
    }

    private static String substituteTypeVars(String type, Map<String, String> map) {
        int lt = type.indexOf('<');
        if (lt == -1) {
            return map.getOrDefault(type, type);
        }

        String raw = type.substring(0, lt);
        String argsPart = type.substring(lt + 1, type.length() - 1);
        List<String> args = splitGenericArguments(argsPart);

        List<String> mappedArgs = args.stream()
                .map(arg -> substituteTypeVars(arg.trim(), map))
                .toList();

        return raw + "<" + String.join(", ", mappedArgs) + ">";
    }

    private static List<String> splitGenericArguments(String input) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c == '<') depth++;
            else if (c == '>') depth--;
            if (c == ',' && depth == 0) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) result.add(current.toString());
        return result;
    }

    public void loadClassFromStream(InputStream is) throws IOException {
        ClassReader reader = new ClassReader(is);
        reader.accept(new TypeClassVisitor(types), 0);
    }

    private Map<String, String> buildGenericMapping(List<String> formals, List<String> actuals) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < Math.min(formals.size(), actuals.size()); i++) {
            String formal = formals.get(i).split(" ")[0];
            map.put(formal, actuals.get(i));
        }
        return map;
    }

    private boolean isAccessible(MethodDescriptor method, VirtualType caller, VirtualType declaring) {
        int acc = method.getAccess();
        if (Modifier.isPublic(acc)) return true;
        if (Modifier.isPrivate(acc)) return caller.getName().equals(declaring.getName());
        if (Modifier.isProtected(acc)) return isSubclass(caller, declaring);
        return isSamePackage(caller, declaring);
    }

    private boolean isSamePackage(VirtualType fqcn1, VirtualType fqcn2) {
        return getPackage(fqcn1.getName()).equals(getPackage(fqcn2.getName()));
    }

    private String getPackage(String fqcn) {
        int lastDot = fqcn.lastIndexOf('.');
        return lastDot < 0 ? "" : fqcn.substring(0, lastDot);
    }

    public boolean isSubclass(VirtualType sub, VirtualType sup) {
        if (sub.equals(sup)) return true;

        Set<VirtualType> visited = new HashSet<>();
        Deque<VirtualType> queue = new ArrayDeque<>();
        queue.add(sub);

        while (!queue.isEmpty()) {
            VirtualType current = queue.poll();
            if (!visited.add(current)) continue;

            if (sup.equals(current.getSuperClass())) return true;
            if (sup.equals(current)) return true;

            if (current.getSuperClass() != null)
                queue.add((VirtualType) resolve(current.getSuperClass()).orElseThrow());

            queue.addAll(current.getInterfaces().stream().map(this::resolve).map(o -> (VirtualType) o.orElseThrow()).toList());
        }

        return false;
    }

    private int matchArguments(List<String> actuals, List<String> formals) {
        if (actuals.size() != formals.size()) return -1;
        int score = 0;
        for (int i = 0; i < actuals.size(); i++) {
            int s = matchScore(actuals.get(i), formals.get(i));
            if (s < 0) return -1;
            score += s;
        }
        return score;
    }

    private int matchScore(String actual, String formal) {
        if (actual.equals(formal)) return 0;
        if (isBoxingMatch(actual, formal)) return 1;
        if (isWideningPrimitive(actual, formal)) return 2;
        if (isAssignable(actual, formal)) return 3;
        return -1;
    }

    private boolean isWideningPrimitive(String actual, String formal) {
        List<String> widenings = WIDENING_MAP.get(actual);
        return widenings != null && widenings.contains(formal);
    }

    private boolean isBoxingMatch(String a, String b) {
        return BOXING.getOrDefault(a, "").equals(b) || BOXING.getOrDefault(b, "").equals(a);
    }

    public record ResolvedMethod(MethodDescriptor method, VirtualType declaringType) {}
}
