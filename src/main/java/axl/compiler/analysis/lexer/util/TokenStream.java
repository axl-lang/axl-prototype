package axl.compiler.analysis.lexer.util;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.lexer.exception.stream.InvalidRewindException;
import axl.compiler.analysis.lexer.exception.stream.TokenStreamException;
import axl.compiler.analysis.lexer.exception.stream.UnexpectedEndOfTokenStream;
import axl.compiler.analysis.lexer.exception.stream.UnexpectedTokenException;

import java.util.List;
import java.util.function.Consumer;

public class TokenStream {

    private final List<Token> tokens;

    private int index = 0;

    public TokenStream(List<Token> tokens) {
        this.tokens = tokens;
    }

    public boolean hasNext() {
        return index < tokens.size();
    }

    public Token peek() throws TokenStreamException {
        if (!hasNext()) {
            throw new UnexpectedEndOfTokenStream("Unexpected end of token stream at position " + index);
        }

        return tokens.get(index);
    }

    public Token next() throws TokenStreamException {
        if (!hasNext()) {
            throw new UnexpectedEndOfTokenStream("Unexpected end of token stream at position " + index);
        }

        return tokens.get(index++);
    }

    public Token expect(TokenType expectedType) throws TokenStreamException {
        Token token = peek();
        if (token.getType() != expectedType) {
            throw new UnexpectedTokenException("Expected token of type " + expectedType + ", but found " + token.getType(), token);
        }

        return next();
    }

    public boolean match(TokenType expectedType) {
        if (hasNext() && tokens.get(index).getType() == expectedType) {
            index++;
            return true;
        }

        return false;
    }

    public boolean matchThen(TokenType expectedType, Consumer<Token> then, Consumer<Token> thenElse) {
        if (!hasNext()) {
            if (thenElse != null) {
                thenElse.accept(null);
            }

            return false;
        }

        Token value = tokens.get(index);
        if (value.getType() == expectedType) {
            index++;
            if (then != null) {
                then.accept(value);
            }

            return true;
        } else {
            if (thenElse != null) {
                thenElse.accept(value);
            }

            return false;
        }
    }

    public boolean matchThen(TokenType expectedType, Consumer<Token> then)  {
        return matchThen(expectedType, then, null);
    }

    public void rewind(int newIndex) throws TokenStreamException {
        if (newIndex < 0 || newIndex > tokens.size()) {
            throw new InvalidRewindException("Cannot rewind to index " + newIndex);
        }

        index = newIndex;
    }

    public int getPosition() {
        return index;
    }

    public List<Token> toList() {
        return tokens;
    }
}
