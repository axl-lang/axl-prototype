package axl.compiler.analysis.parser.impl.declaration;

import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.declaration.ValDeclaration;
import axl.compiler.analysis.parser.data.statement.ValStatement;
import axl.compiler.analysis.parser.impl.expression.ExpressionParser;
import axl.compiler.analysis.parser.impl.reference.TypeReferenceParser;
import lombok.Getter;

public class ValDeclarationParser extends Parser<ValDeclaration> {

    @Getter
    public static ValDeclarationParser instance = new ValDeclarationParser();

    @Override
    public ValDeclaration analyze() {
        ValDeclaration declaration = new ValDeclaration();

        declaration.setDeclarationFlag(tokenStream.expect(TokenType.VAL));
        declaration.setName(tokenStream.expect(TokenType.IDENTIFY));

        tokenStream.matchThen(TokenType.COLON, ignored -> declaration.setReference(TypeReferenceParser.getInstance().analyze()));

        // FIXME
        tokenStream.matchThen(TokenType.ASSIGN, ignored -> declaration.setValue(ExpressionParser.getInstance().analyze()));
        ;

        return declaration;
    }
}
