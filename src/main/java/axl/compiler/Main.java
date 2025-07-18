package axl.compiler;

import axl.compiler.analysis.lexer.Lexer;
import axl.compiler.analysis.lexer.impl.LexerImpl;
import axl.compiler.analysis.lexer.util.TokenStream;
import axl.compiler.analysis.parser.data.declaration.ModuleDeclaration;
import axl.compiler.analysis.parser.impl.declaration.ModuleDeclarationParser;
import axl.compiler.analysis.common.util.PrettyPrinter;
import axl.compiler.analysis.semantic.data.tree.symbol.ModuleSymbol;
import axl.compiler.analysis.semantic.impl.SemanticAnalyzerImpl;
import axl.compiler.cli.CliException;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new CliException("Usage: axl <input-file>");
        }

        String content = Files.readString(new File(args[0]).toPath());
        Lexer lexer = new LexerImpl();

        TokenStream stream = lexer.tokenize(content);

        ModuleDeclarationParser.setTokenStream(stream);

        ModuleDeclaration moduleDeclaration = new ModuleDeclarationParser().analyze();
        moduleDeclaration.setFilename(args[0].substring(args[0].lastIndexOf('/') + 1, args[0].lastIndexOf('.')));

        SemanticAnalyzerImpl semanticAnalyzer = new SemanticAnalyzerImpl();

        semanticAnalyzer.analyze(moduleDeclaration);

        ModuleSymbol symbol = semanticAnalyzer.transform(moduleDeclaration);

        symbol = semanticAnalyzer.typed(symbol);

        List<ModuleSymbol.GeneratedClass> writers = symbol.codegen();

        File inputFile = new File(args[0]);
        Path outputDir = inputFile.toPath().getParent();
        for (ModuleSymbol.GeneratedClass gc : writers) {
            byte[] bytes = gc.cw().toByteArray();
            String internalName = gc.internalName();
            String fileName = internalName.replace('/', File.separatorChar) + ".class";
            Path outFile = outputDir.resolve(fileName);
            Files.createDirectories(outFile.getParent());
            try (FileOutputStream fos = new FileOutputStream(outFile.toFile())) {
                fos.write(bytes);
            }
        }

        URL[] urls = new URL[] { outputDir.toUri().toURL() };
        try (URLClassLoader classLoader = new URLClassLoader(urls, Main.class.getClassLoader())) {
            String mainClassName = SemanticAnalyzerImpl.getLinkerContext().getContext().getName();
            Class<?> mainClass = classLoader.loadClass(mainClassName);
            Method mainMethod = mainClass.getMethod("main");

            mainMethod.invoke(null);
        }
    }

}