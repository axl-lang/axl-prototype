package axl.compiler.analysis.parser.impl.statement;

import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.statement.IfStatement;
import axl.compiler.analysis.parser.data.statement.WhileStatement;
import axl.compiler.analysis.parser.impl.common.StatementParser;
import axl.compiler.analysis.parser.impl.expression.ExpressionParser;
import lombok.Getter;

public class WhileStatementParser extends Parser<WhileStatement> {

    @Getter
    public static WhileStatementParser instance = new WhileStatementParser();

    @Override
    public WhileStatement analyze() {
        WhileStatement statement = new WhileStatement();

        statement.setWhileToken(tokenStream.expect(TokenType.WHILE));
        statement.setCondition(ExpressionParser.getInstance().analyze());
        statement.setThen(StatementParser.instance.analyze());

        return statement;
    }
}
