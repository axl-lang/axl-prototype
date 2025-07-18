package axl.compiler.analysis.lexer.data;

import axl.compiler.analysis.common.data.SourceLocation;
import axl.compiler.analysis.lexer.exception.lexer.LexicalException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Builder
public class LexerInternal {

    @Setter(AccessLevel.PRIVATE)
    private String content;

    @Setter(AccessLevel.PRIVATE)
    private List<Token> context;

    private LexerInternalFrame frame;

    private LexerInternalFrame safeFrame;

    @Data
    @Builder
    public static class LexerInternalFrame implements Cloneable {

        private SourceLocation location;

        private int cursor;

        @Override
        public LexerInternalFrame clone() {
            try {
                LexerInternalFrame clone = (LexerInternalFrame) super.clone();
                clone.location = location.clone();
                clone.cursor = cursor;
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    public void next(int n) {
        for (int i = 0; i < n; i++)
            next();
    }

    public char next() {
        char result = peek();
        if (result == '\n') {
            getFrame().getLocation().line++;
            getFrame().getLocation().column = 1;
        } else {
            getFrame().getLocation().column++;
        }
        getFrame().getLocation().offset++;

        return result;
    }

    public char peek(int n) {
        int offset = getFrame().getLocation().offset + n;
        if (offset >= getContent().length())
            return '\0';

        return getContent().charAt(offset);
    }

    public char peek() {
        return peek(0);
    }

    public boolean isEnd() {
        return getFrame().getLocation().offset >= getContent().length();
    }

    public boolean skip() {
        for (;;) {
            if (peek(0) == '/' && peek(1) == '*')
                skipMultilineComment();
            else if (peek(0) == '/' && peek(1) == '/')
                skipSingleComment();
            else if (peek() == ' ' || peek() == '\t' || peek() == '\n' || peek() == '\r')
                next();
            else
                break;
        }

        return isEnd();
    }

    protected void skipSingleComment() {
        next(2);

        while (peek() != '\r' && peek() != '\n' && peek() != '\0')
            next();

        next();
    }

    protected void skipMultilineComment() {
        LexerInternal.LexerInternalFrame frame = getFrame().clone();
        next(2);

        while (peek(0) != '*' || peek(1) != '/') {
            if (isEnd())
                throw new LexicalException(frame.getLocation(), "Multiline comment was not closed");

            next();
        }

        next(2);
    }

    public String slice() {
        return getContent()
                .substring(
                        safeFrame.getLocation().getOffset(),
                        getFrame().getLocation().getOffset()
                );
    }
}
