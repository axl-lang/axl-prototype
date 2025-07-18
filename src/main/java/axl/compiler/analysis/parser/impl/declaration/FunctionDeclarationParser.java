package axl.compiler.analysis.parser.impl.declaration;

import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.declaration.FunctionDeclaration;
import axl.compiler.analysis.parser.impl.common.CollectionParser;
import axl.compiler.analysis.parser.impl.common.StatementParser;
import axl.compiler.analysis.parser.impl.reference.TypeReferenceParser;
import lombok.Getter;

public class FunctionDeclarationParser extends Parser<FunctionDeclaration> {

    @Getter
    public static FunctionDeclarationParser instance = new FunctionDeclarationParser();

    @Override
    public FunctionDeclaration analyze() {
        FunctionDeclaration declaration = new FunctionDeclaration();
        tokenStream.matchThen(TokenType.PUB, declaration::setAccessFlag);
        declaration.setFnToken(tokenStream.expect(TokenType.FN));
        declaration.setName(tokenStream.expect(TokenType.IDENTIFY));
        tokenStream.expect(TokenType.LEFT_PARENT);

        if (!tokenStream.match(TokenType.RIGHT_PARENT)) {
            declaration.setArgumentDeclarations(CollectionParser.analyze(ArgumentDeclarationParser.getInstance(), TokenType.COMMA));
            tokenStream.expect(TokenType.RIGHT_PARENT);
        }

        if (tokenStream.match(TokenType.IMPLICATION)) {
            declaration.setResultReference(TypeReferenceParser.getInstance().analyze());
        }


        declaration.setBody(StatementParser.getInstance().analyze());

        return declaration;
    }
}
