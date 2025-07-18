package axl.compiler.analysis.parser.impl.common;

import axl.compiler.analysis.common.exception.AnalysisException;
import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.Statement;
import axl.compiler.analysis.parser.data.statement.BodyStatement;
import axl.compiler.analysis.parser.impl.expression.ExpressionParser;
import axl.compiler.analysis.parser.impl.statement.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class StatementParser extends Parser<Statement> {

    @Getter
    public static StatementParser instance = new StatementParser();

    @Override
    public Statement analyze() {
        return switch (tokenStream.peek().getType()) {
            case LEFT_BRACE -> {
                tokenStream.next();
                BodyStatement result = new BodyStatement();
                result.setStatements(new ArrayList<>());

                while (!tokenStream.match(TokenType.RIGHT_BRACE)) {
                    result.getStatements().add(analyze());
                }

                yield result;
            }
            case FOR -> ForStatementParser.getInstance().analyze();
            case IF -> IfStatementParser.getInstance().analyze();
            case WHILE -> WhileStatementParser.getInstance().analyze();
            case BREAK -> OperationStatementParser.getBreakParser().analyze();
            case RETURN -> OperationStatementParser.getReturnParser().analyze();
            case CONTINUE -> OperationStatementParser.getContinueParser().analyze();
            case THROW -> OperationStatementParser.getThrowParser().analyze();
            case VAL -> ValStatementParser.getInstance().analyze();
            case VAR -> VarStatementParser.getInstance().analyze();
            default -> ExpressionParser.getInstance().analyze();
        };
    }

    @SafeVarargs
    public final Statement analyze(Parser<? extends Statement>... statements) {
        if (tokenStream.match(TokenType.LEFT_BRACE)) {

        }

        return analyzePart(statements);
    }

    @SafeVarargs
    protected final Statement analyzePart(Parser<? extends Statement>... statements) {
        //noinspection MismatchedQueryAndUpdateOfCollection
        List<Exception> exceptions = new ArrayList<>();
        Statement statement = null;

        int start = tokenStream.getPosition();
        for (Parser<? extends Statement> parser: statements) {
            if (statement != null) {
                continue;
            }
            try {
                statement = parser.analyze();
            } catch (AnalysisException e) {
                exceptions.add(e);
                tokenStream.rewind(start);
            }
        }

        if (statement == null) {
            throw new RuntimeException();
        }

        return statement;
    }
}
