package axl.compiler.analysis.parser.impl.statement;

import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.statement.IfStatement;
import axl.compiler.analysis.parser.impl.common.StatementParser;
import axl.compiler.analysis.parser.impl.expression.ExpressionParser;
import lombok.Getter;

public class IfStatementParser extends Parser<IfStatement> {

    @Getter
    public static IfStatementParser instance = new IfStatementParser();

    @Override
    public IfStatement analyze() {
        IfStatement statement = new IfStatement();

        statement.setIfToken(tokenStream.expect(TokenType.IF));
        statement.setCondition(ExpressionParser.getInstance().analyze());
        statement.setThen(StatementParser.instance.analyze());
        tokenStream.matchThen(TokenType.ELSE, elseToken -> {
            statement.setElseToken(elseToken);
            statement.setThenElse(StatementParser.getInstance().analyze());
        });

        return statement;
    }
}
