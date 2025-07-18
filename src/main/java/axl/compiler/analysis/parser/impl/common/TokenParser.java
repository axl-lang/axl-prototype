package axl.compiler.analysis.parser.impl.common;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TokenParser extends Parser<Token> {

    private TokenType type;

    @Override
    public Token analyze() {
        return tokenStream.expect(type);
    }
}
