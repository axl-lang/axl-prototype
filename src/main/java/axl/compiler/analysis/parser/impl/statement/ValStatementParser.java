package axl.compiler.analysis.parser.impl.statement;

import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.statement.ValStatement;
import axl.compiler.analysis.parser.data.statement.VarStatement;
import axl.compiler.analysis.parser.impl.expression.ExpressionParser;
import axl.compiler.analysis.parser.impl.reference.TypeReferenceParser;
import lombok.Getter;

public class ValStatementParser extends Parser<ValStatement> {

    @Getter
    public static ValStatementParser instance = new ValStatementParser();

    @Override
    public ValStatement analyze() {
        ValStatement statement = new ValStatement();

        statement.setDeclarationFlag(tokenStream.expect(TokenType.VAL));
        statement.setName(tokenStream.expect(TokenType.IDENTIFY));

        tokenStream.matchThen(TokenType.COLON, ignored -> statement.setReference(TypeReferenceParser.getInstance().analyze()));

        tokenStream.matchThen(TokenType.ASSIGN, ignored -> statement.setValue(ExpressionParser.getInstance().analyze()));

        return statement;
    }
}
