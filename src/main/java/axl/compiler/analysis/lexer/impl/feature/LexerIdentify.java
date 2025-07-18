package axl.compiler.analysis.lexer.impl.feature;

import axl.compiler.analysis.lexer.data.LexerInternal;
import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.data.TokenGroup;
import axl.compiler.analysis.lexer.data.TokenType;

public class LexerIdentify implements LexerFeature {

    @Override
    public Token.TokenBuilder tokenize(LexerInternal lexer) {
        do {
            lexer.next();
        } while (isIdentifierPart(lexer.peek()));

        TokenType type = getByRepresentation(lexer.slice());

        if (type == null) {
            type = TokenType.IDENTIFY;
        }

        return Token.builder().type(type);
    }

    private TokenType getByRepresentation(String representation) {
        for (TokenType type: TokenType.values()) {
            if (type.getGroup() != TokenGroup.KEYWORD && type.getGroup() != TokenGroup.OPERATOR)
                continue;

            if (type.getRepresentation().equals(representation))
                return type;
        }

        return null;
    }

    public static boolean isIdentifierStart(char current) {
        return Character.isLetter(current) || current == '_';
    }

    public static boolean isIdentifierPart(char current) {
        return isIdentifierStart(current) || LexerLiteral.isNumber(current);
    }
}
