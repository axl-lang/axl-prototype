package axl.compiler.analysis.lexer.impl;

import axl.compiler.analysis.common.data.SourceLocation;
import axl.compiler.analysis.common.data.SourceSection;
import axl.compiler.analysis.lexer.Lexer;
import axl.compiler.analysis.lexer.data.LexerInternal;
import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.lexer.impl.feature.LexerFeature;
import axl.compiler.analysis.lexer.impl.feature.LexerIdentify;
import axl.compiler.analysis.lexer.impl.feature.LexerLiteral;
import axl.compiler.analysis.lexer.impl.feature.LexerOperator;
import axl.compiler.analysis.lexer.util.TokenStream;
import lombok.Setter;

import java.util.ArrayList;

public class LexerImpl implements Lexer {

    protected LexerInternal internal;

    @Setter
    private LexerFeature identify = new LexerIdentify();

    @Setter
    private LexerFeature operator = new LexerOperator();

    @Setter
    private LexerLiteral literal = new LexerLiteral();

    @Override
    public TokenStream tokenize(String content) {
        internal = LexerInternal.builder()
                .content(content)
                .context(new ArrayList<>())
                .frame(
                        LexerInternal.LexerInternalFrame.builder()
                                .location(new SourceLocation(0, 1, 1))
                                .cursor(0).build()
                ).build();
        internal.setSafeFrame(internal.getFrame().clone());
        internal.skip();

        if (internal.isEnd())
            return new TokenStream(internal.getContext());

        while (!internal.skip()) {
            internal.setSafeFrame(internal.getFrame().clone());
            Token.TokenBuilder token;

            if (LexerIdentify.isIdentifierStart(internal.peek()))
                token = identify.tokenize(internal);
            else if (LexerLiteral.isNumber(internal.peek()) || internal.peek() == '.')
                token = literal.tokenizeNumber(internal);
            else if (internal.peek() == '"')
                token = literal.tokenizeString(internal);
            else if (internal.peek() == '\'')
                token = literal.tokenizeChar(internal);
            else
                token = operator.tokenize(internal);

            token.section(
                    new SourceSection(
                            internal.getSafeFrame().getLocation(),
                            internal.getFrame().getLocation().getOffset() - internal.getSafeFrame().getLocation().getOffset()
                    )
            );

            token.value(internal.slice());

            internal.skip();
            internal.getFrame().setCursor(internal.getFrame().getCursor() + 1);
            internal.getContext().add(token.build());
        }

        return new TokenStream(internal.getContext());
    }
}
