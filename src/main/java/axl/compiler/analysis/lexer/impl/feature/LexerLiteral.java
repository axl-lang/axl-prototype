package axl.compiler.analysis.lexer.impl.feature;

import axl.compiler.analysis.lexer.data.LexerInternal;
import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.lexer.exception.lexer.LexerException;
import axl.compiler.analysis.lexer.exception.lexer.LexicalException;

public class LexerLiteral {

    public Token.TokenBuilder tokenizeNumber(LexerInternal lexer) {
        if (lexer.peek(0) == '0') {
            if (lexer.peek(1) == 'x' || lexer.peek(1) == 'X')
                return this.tokenizeHexNumber(lexer);
            else if (lexer.peek(1) == 'b' || lexer.peek(1) == 'B')
                throw new LexerException("Binary numbers not supported");
            else if (lexer.peek(1) == '.')
                return this.tokenizeFloatingPointNumber(lexer);
            else if (lexer.peek(1) == '_' || isNumber(lexer.peek(1)))
                return this.tokenizeDecNumber(lexer);

            lexer.next();
            return Token.builder().type(TokenType.DEC_NUMBER);
        }

        if (lexer.peek(0) == '.') {
            if (!isNumber(lexer.peek(1))) {
                lexer.next();
                return Token.builder().type(TokenType.DOT);
            }

            return this.tokenizeFloatingPointNumber(lexer);
        }

        return this.tokenizeDecNumber(lexer);
    }

    private Token.TokenBuilder tokenizeHexNumber(LexerInternal lexer) {
        lexer.next(2);
        int cnt = 0;
        boolean zeroStart = true;

        if (lexer.peek() != '0') {
            zeroStart = false;
            cnt++;
        }

        if (!isHexNumber(lexer.next()))
            throw new LexicalException(lexer.getFrame().getLocation(), "Numeric literal must start with a digit");

        boolean lastUnderscore = false;

        for (; ; ) {
            if (isHexNumber(lexer.peek())) {
                lastUnderscore = false;
                if (lexer.peek() != '0' && zeroStart) {
                    zeroStart = false;
                    cnt++;
                }
                lexer.next();
            } else if (lexer.peek() == '_') {
                lastUnderscore = true;
                lexer.next();
            } else {
                break;
            }
        }

        if (lastUnderscore)
            throw new LexicalException(lexer.getFrame().getLocation(), "Numeric literal cannot have an underscore as its last character");

        if (lexer.peek() == 'L' || lexer.peek() == 'l') {
            if (cnt > 16)
                throw new LexicalException(lexer.getFrame().getLocation(), "Value of numeric literal is too large");
            lexer.next();
            return Token.builder().type(TokenType.HEX_LONG_NUMBER);
        }

        if (cnt > 8)
            throw new LexicalException(lexer.getFrame().getLocation(), "Value of numeric literal is too large");

        return Token.builder().type(TokenType.HEX_NUMBER);
    }

    private Token.TokenBuilder tokenizeDecNumber(LexerInternal lexer) {
        tokenizeDecPart(lexer, true);

        if (lexer.peek() == '.' || lexer.peek() == 'E' || lexer.peek() == 'e')
            return tokenizeFloatingPointNumber(lexer);

        if (lexer.peek() == 'L' || lexer.peek() == 'l') {
            lexer.next();

            return Token.builder().type(TokenType.DEC_LONG_NUMBER);
        }

        if (lexer.peek() == 'F' || lexer.peek() == 'f') {
            lexer.next();

            return Token.builder().type(TokenType.FLOAT_NUMBER);
        }

        if (lexer.peek() == 'D' || lexer.peek() == 'd') {
            lexer.next();

            return Token.builder().type(TokenType.DOUBLE_NUMBER);
        }

        return Token.builder().type(TokenType.DEC_NUMBER);
    }

    private Token.TokenBuilder tokenizeFloatingPointNumber(LexerInternal lexer) {
        lexer.setFrame(lexer.getSafeFrame());

        tokenizeDecPart(lexer, false);

        if (lexer.peek() == '.') {
            lexer.next();
            tokenizeDecPart(lexer, true);
        }

        if (lexer.peek() == 'F' || lexer.peek() == 'f') {
            lexer.next();

            return Token.builder().type(TokenType.FLOAT_NUMBER);
        }

        if (lexer.peek() == 'D' || lexer.peek() == 'd')
            lexer.next();

        return Token.builder().type(TokenType.DOUBLE_NUMBER);
    }

    private void tokenizeDecPart(LexerInternal lexer, boolean req) {
        boolean firstUnderscore = true;
        boolean lastUnderscore = false;
        boolean hasNumber = false;

        for (; ; ) {
            if (isNumber(lexer.peek())) {
                lastUnderscore = false;
                firstUnderscore = false;
                hasNumber = true;
                lexer.next();
            } else if (lexer.peek() == '_') {
                if (firstUnderscore)
                    throw new LexicalException(lexer.getFrame().getLocation(), "Numeric literal cannot have an underscore as it's first or last character");

                lastUnderscore = true;
                lexer.next();
            } else {
                break;
            }
        }

        if (!hasNumber && req)
            throw new LexicalException(lexer.getFrame().getLocation(), "Numeric literal cannot terminate with a dot");

        if (lastUnderscore)
            throw new LexicalException(lexer.getFrame().getLocation(), "Numeric literal cannot have an underscore as it's last character");
    }

    public Token.TokenBuilder tokenizeString(LexerInternal lexer) {
        lexer.next();
        while (lexer.peek() != '"') {
            if (lexer.isEnd())
                throw new LexicalException(lexer.getFrame().getLocation(), "String literal is not completed");

            if (lexer.peek() == '\\')
                this.readEscape(lexer, '"');
            else
                lexer.next();
        }
        lexer.next();

        return Token.builder().type(TokenType.STRING_LITERAL);
    }

    public Token.TokenBuilder tokenizeChar(LexerInternal lexer) {
        lexer.next();

        if (lexer.peek() == '\\')
            readEscape(lexer, '\'');
        else
            lexer.next();

        if (lexer.next() != '\'')
            throw new LexicalException(lexer.getFrame().getLocation(), "Symbol literal is not completed");

        return Token.builder().type(TokenType.CHAR_LITERAL);
    }

    private void readEscape(LexerInternal lexer, char allowed) {
        LexerInternal.LexerInternalFrame frame = lexer.getFrame().clone();

        lexer.next();
        switch (lexer.peek()) {
            case '\'':
            case '"':
                if (lexer.peek() != allowed)
                    break;
            case 'n':
            case 't':
            case 'r':
            case '0':
            case '\\':
                lexer.next();
                return;
            case 'u':
                lexer.next();
                for (int i = 0; i < 4; i++) {
                    if (isHexNumber(lexer.next()))
                        continue;

                    throw new LexicalException(frame.getLocation(), "Invalid unicode");
                }
                return;
        }

        throw new LexicalException(frame.getLocation(), "Invalid escape sequence");
    }

    public static boolean isNumber(char current) {
        return ('0' <= current && current <= '9');
    }

    public static boolean isHexNumber(char current) {
        return ('0' <= current && current <= '9')
                || ('a' <= current && current <= 'f')
                || ('A' <= current && current <= 'F');
    }
}
