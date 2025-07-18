package axl.compiler.analysis.lexer.exception.stream;

import axl.compiler.analysis.lexer.exception.lexer.LexerException;

public class UnexpectedEndOfTokenStream extends LexerException {

    public UnexpectedEndOfTokenStream(String message) {
        super(message);
    }
}