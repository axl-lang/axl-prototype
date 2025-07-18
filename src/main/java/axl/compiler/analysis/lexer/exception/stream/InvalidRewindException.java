package axl.compiler.analysis.lexer.exception.stream;

import axl.compiler.analysis.lexer.exception.lexer.LexerException;

public class InvalidRewindException extends LexerException {

    public InvalidRewindException(String message) {
        super(message);
    }
}