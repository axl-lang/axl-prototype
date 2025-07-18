package axl.compiler.analysis.lexer;

import axl.compiler.analysis.lexer.util.TokenStream;

public interface Lexer {

    TokenStream tokenize(String content);
}
