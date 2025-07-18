package axl.compiler.analysis.parser.impl.common;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;

import java.util.ArrayList;
import java.util.List;

public abstract class CollectionParser extends Parser<List<?>> {

    public static List<Token> analyze(TokenType target, TokenType delimiter) {
        List<Token> result = new ArrayList<>();
        result.add(tokenStream.expect(target));

        while (tokenStream.matchThen(delimiter, dot -> {
            result.add(tokenStream.expect(target));
        }));

        return result;
    }

    public static <T> List<T> analyze(Parser<T> target, TokenType delimiter) {
        List<T> result = new ArrayList<>();
        result.add(target.analyze());

        while (tokenStream.matchThen(delimiter, dot -> {
            result.add(target.analyze());
        }));

        return result;
    }
}
