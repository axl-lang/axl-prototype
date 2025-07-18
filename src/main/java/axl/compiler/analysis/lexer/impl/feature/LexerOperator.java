package axl.compiler.analysis.lexer.impl.feature;

import axl.compiler.analysis.lexer.data.LexerInternal;
import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.data.TokenGroup;
import axl.compiler.analysis.lexer.data.TokenType;

public class LexerOperator implements LexerFeature {

    @Override
    public Token.TokenBuilder tokenize(LexerInternal lexer) {
        int currentLength = 0;
        TokenType current = null;

        for (TokenType type : TokenType.values()) {
            if (type.getGroup() != TokenGroup.OPERATOR && type.getGroup() != TokenGroup.DELIMITER)
                continue;

            String representation = type.getRepresentation();
            if (!isRepresentation(lexer, representation))
                continue;

            if (currentLength < representation.length()) {
                currentLength = representation.length();
                current = type;
            }
        }

        if (current == null)
            throw new IllegalArgumentException();
//            throw new IllegalLexicalException("Unknown symbol", this, frame);

        boolean isPrefix = lexer.getContext().isEmpty() ||
                lexer.getContext().getLast().getType().getGroup() == TokenGroup.OPERATOR ||
                lexer.getContext().getLast().getType() == TokenType.COMMA ||
                lexer.getContext().getLast().getType() == TokenType.LEFT_PARENT ||
                lexer.getContext().getLast().getType() == TokenType.LEFT_SQUARE ||
                lexer.getContext().getLast().getType() == TokenType.RETURN ||
                lexer.getContext().getLast().getType() == TokenType.THIS;
        if (isPrefix) {
            if (current == TokenType.MINUS) {
                current = TokenType.UNARY_MINUS;
            } else if (current == TokenType.POSTFIX_DECREMENT) {
                current = TokenType.PREFIX_DECREMENT;
            } else if (current == TokenType.POSTFIX_INCREMENT) {
                current = TokenType.PREFIX_INCREMENT;
            }
        }

        lexer.next(currentLength);
        return Token.builder().type(current);
    }

    private boolean isRepresentation(LexerInternal lexer, String representation) {
        for (int i = 0; i < representation.length(); i++)
            if (lexer.peek(i) != representation.charAt(i))
                return false;

        return true;
    }
}
