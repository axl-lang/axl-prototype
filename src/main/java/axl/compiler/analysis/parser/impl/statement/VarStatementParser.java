package axl.compiler.analysis.parser.impl.statement;

import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.statement.ForStatement;
import axl.compiler.analysis.parser.data.statement.VarStatement;
import axl.compiler.analysis.parser.impl.common.StatementParser;
import axl.compiler.analysis.parser.impl.expression.ExpressionParser;
import axl.compiler.analysis.parser.impl.reference.TypeReferenceParser;
import lombok.Getter;

public class VarStatementParser extends Parser<VarStatement> {

    @Getter
    public static VarStatementParser instance = new VarStatementParser();

    @Override
    public VarStatement analyze() {
        VarStatement statement = new VarStatement();

        statement.setDeclarationFlag(tokenStream.expect(TokenType.VAR));
        statement.setName(tokenStream.expect(TokenType.IDENTIFY));

        tokenStream.matchThen(TokenType.COLON, ignored -> statement.setReference(TypeReferenceParser.getInstance().analyze()));
        tokenStream.matchThen(TokenType.ASSIGN, ignored -> statement.setValue(ExpressionParser.getInstance().analyze()));

        if (statement.getReference() == null && statement.getValue() == null) {
            tokenStream.expect(TokenType.ASSIGN);
        }

        return statement;
    }
}
