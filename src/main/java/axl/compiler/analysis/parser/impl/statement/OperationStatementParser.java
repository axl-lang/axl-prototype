package axl.compiler.analysis.parser.impl.statement;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.statement.OperationStatement;
import axl.compiler.analysis.parser.impl.common.TokenParser;
import axl.compiler.analysis.parser.impl.expression.ExpressionParser;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class OperationStatementParser<T> extends Parser<OperationStatement<T>> {

    private final TokenType type;

    private final Parser<T> parser;

    @Getter
    public static final OperationStatementParser<Expression> returnParser = new OperationStatementParser<>(TokenType.RETURN, ExpressionParser.getInstance());

    @Getter
    public static final OperationStatementParser<Expression> throwParser = new OperationStatementParser<>(TokenType.THROW, ExpressionParser.getInstance());

    @Getter
    public static final OperationStatementParser<Token> breakParser = new OperationStatementParser<>(TokenType.BREAK, new TokenParser(TokenType.IDENTIFY));

    @Getter
    public static final OperationStatementParser<Token> continueParser = new OperationStatementParser<>(TokenType.CONTINUE, new TokenParser(TokenType.IDENTIFY));

    @Override
    public OperationStatement<T> analyze() {
        OperationStatement<T> statement = new OperationStatement<>();
        statement.setOperator(tokenStream.expect(type));
        if (tokenStream.hasNext() && tokenStream.peek().getType() != TokenType.RIGHT_BRACE) {
            statement.setOperand(parser.analyze());
        }

        return statement;
    }
}
