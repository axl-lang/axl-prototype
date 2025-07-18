package axl.compiler.analysis.parser.impl.statement;

import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.statement.ForStatement;
import axl.compiler.analysis.parser.impl.common.StatementParser;
import axl.compiler.analysis.parser.impl.expression.ExpressionParser;
import lombok.Getter;

import java.util.List;

public class ForStatementParser extends Parser<ForStatement> {

    @Getter
    public static ForStatementParser instance = new ForStatementParser();

    @Override
    public ForStatement analyze() {
        ForStatement statement = new ForStatement();
        statement.setForToken(tokenStream.expect(TokenType.FOR));

        tokenStream.expect(TokenType.LEFT_PARENT);
        statement.setVarToken(tokenStream.expect(TokenType.VAR));
        statement.setName(tokenStream.expect(TokenType.IDENTIFY));
        statement.setColonToken(tokenStream.expect(TokenType.COLON));
        statement.setIterator(ExpressionParser.instance.analyze());
        tokenStream.expect(TokenType.RIGHT_PARENT);

        statement.setBody(StatementParser.instance.analyze());

        return statement;
    }
}
