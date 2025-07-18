package axl.compiler.analysis.lexer.exception.stream;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.exception.lexer.LexerException;
import lombok.Getter;

@Getter
public class UnexpectedTokenException extends LexerException {

    private final Token token;

    public UnexpectedTokenException(String message, Token token) {
        super(message + ": " + token.getSection());
        this.token = token;
    }

}