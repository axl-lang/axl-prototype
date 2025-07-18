package axl.compiler.analysis.parser.impl.declaration;

import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.Declaration;
import axl.compiler.analysis.parser.data.declaration.ClassDeclaration;
import axl.compiler.analysis.parser.exception.ParserException;
import axl.compiler.analysis.parser.impl.reference.TypeReferenceParser;
import lombok.Getter;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class ClassDeclarationParser extends Parser<ClassDeclaration> {

    @Getter
    public static ClassDeclarationParser instance = new ClassDeclarationParser();

    @Override
    public ClassDeclaration analyze() {
        ClassDeclaration declaration = new ClassDeclaration();
        tokenStream.matchThen(TokenType.PUB, declaration::setAccessFlag);
        declaration.setClassToken(tokenStream.expect(TokenType.CLASS));
        declaration.setClassReference(TypeReferenceParser.getInstance().analyze());
        tokenStream.matchThen(TokenType.COLON, colonToken -> {
            declaration.setSuperClassReference(TypeReferenceParser.getInstance().analyze());
        });

        tokenStream.expect(TokenType.LEFT_BRACE);

        declaration.setDeclarations(new ArrayList<>());
        while (!tokenStream.match(TokenType.RIGHT_BRACE)) {
            if (!tokenStream.hasNext()) {
                tokenStream.expect(TokenType.RIGHT_BRACE); // FIXME throws error
            }

            int position = tokenStream.getPosition();
            tokenStream.match(TokenType.PUB);
            declaration.getDeclarations().add(analyzeDeclaration(position));
        }

        return declaration;
    }

    protected Declaration analyzeDeclaration(int position) {
        AtomicReference<Declaration> declaration = new AtomicReference<>();

        if (!(tokenStream.matchThen(TokenType.FN, fnToken -> {
            tokenStream.rewind(position);
            declaration.set(FunctionDeclarationParser.getInstance().analyze());
        }) || tokenStream.matchThen(TokenType.VAR, varToken -> {
            tokenStream.rewind(position);
            declaration.set(VarDeclarationParser.getInstance().analyze());
        }) || tokenStream.matchThen(TokenType.VAL, valToken -> {
            tokenStream.rewind(position);
            declaration.set(ValDeclarationParser.getInstance().analyze());
        }))) {
            throw new ParserException(tokenStream.peek().getSection(), "Unknown token: " + tokenStream.peek().getType());
        }

        return declaration.get();
    }
}
