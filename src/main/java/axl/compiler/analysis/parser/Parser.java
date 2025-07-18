package axl.compiler.analysis.parser;

import axl.compiler.analysis.lexer.util.TokenStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class Parser<T> {

    public T analyze() {
        return null;
    }

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PUBLIC)
    public static TokenStream tokenStream;
}