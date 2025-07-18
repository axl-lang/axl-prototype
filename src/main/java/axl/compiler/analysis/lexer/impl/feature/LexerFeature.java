package axl.compiler.analysis.lexer.impl.feature;

import axl.compiler.analysis.lexer.data.LexerInternal;
import axl.compiler.analysis.lexer.data.Token;

public interface LexerFeature {

    Token.TokenBuilder tokenize(LexerInternal lexer);
}
