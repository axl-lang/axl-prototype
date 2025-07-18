package axl.compiler.analysis.parser.impl.declaration;

import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.lexer.exception.stream.TokenStreamException;
import axl.compiler.analysis.lexer.exception.stream.UnexpectedTokenException;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.Declaration;
import axl.compiler.analysis.parser.data.declaration.LocationDeclaration;
import axl.compiler.analysis.parser.data.declaration.ModuleDeclaration;
import axl.compiler.analysis.parser.impl.common.CollectionParser;
import axl.compiler.analysis.parser.exception.ParserException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class ModuleDeclarationParser extends Parser<ModuleDeclaration> {

    @Override
    public ModuleDeclaration analyze() {
        ModuleDeclaration declaration = new ModuleDeclaration();

        tokenStream.matchThen(TokenType.PACKAGE, ignored -> {
            LocationDeclaration locationDeclaration = new LocationDeclaration();
            locationDeclaration.setLocation(CollectionParser.analyze(TokenType.IDENTIFY, TokenType.DOT));
            declaration.setPackageDeclaration(locationDeclaration);
        });

        declaration.setImportDeclarations(new ArrayList<>());
        while (tokenStream.matchThen(TokenType.IMPORT, ignored -> {
            LocationDeclaration locationDeclaration = new LocationDeclaration();
            locationDeclaration.setLocation(new ArrayList<>());
            declaration.getImportDeclarations().add(locationDeclaration);
            locationDeclaration.getLocation().add(tokenStream.expect(TokenType.IDENTIFY));

            while (tokenStream.matchThen(TokenType.DOT, dot -> {
                try {
                    locationDeclaration.getLocation().add(tokenStream.expect(TokenType.IDENTIFY));
                } catch (UnexpectedTokenException e) {
                    locationDeclaration.getLocation().add(tokenStream.expect(TokenType.MULTIPLY));
                }
            })) if (locationDeclaration.getLocation().getLast().getType() == TokenType.MULTIPLY) break;
        }));

        declaration.setDeclarations(new ArrayList<>());
        while (tokenStream.hasNext()) {
            int position = tokenStream.getPosition();
            tokenStream.match(TokenType.PUB);
            declaration.getDeclarations().add(analyzeDeclaration(position));
        }

        if (tokenStream.hasNext()) {
            System.err.println("Warning: token stream not end");
        }

        return declaration;
    }

    protected Declaration analyzeDeclaration(int position) {
        AtomicReference<Declaration> declaration = new AtomicReference<>();

        if (!(tokenStream.matchThen(TokenType.CLASS, classToken -> {
            tokenStream.rewind(position);
            declaration.set(ClassDeclarationParser.getInstance().analyze());
        }) || tokenStream.matchThen(TokenType.FN, fnToken -> {
            tokenStream.rewind(position);
            declaration.set(FunctionDeclarationParser.getInstance().analyze());
        }) || tokenStream.matchThen(TokenType.VAR, varToken -> {
            tokenStream.rewind(position);
            declaration.set(VarDeclarationParser.getInstance().analyze());
        }) || tokenStream.matchThen(TokenType.VAL, valToken -> {
            tokenStream.rewind(position);
            declaration.set(ValDeclarationParser.getInstance().analyze());
        }))) throw new ParserException(tokenStream.peek().getSection(), "Unknown token");

        return declaration.get();
    }
}
