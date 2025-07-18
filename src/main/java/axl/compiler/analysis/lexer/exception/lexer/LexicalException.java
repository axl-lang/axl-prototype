package axl.compiler.analysis.lexer.exception.lexer;

import axl.compiler.analysis.common.data.SourceLocation;
import axl.compiler.analysis.common.exception.AnalysisSourceException;

public class LexicalException extends AnalysisSourceException {

    public LexicalException(SourceLocation location, String message) {
        super(location, message);
    }
}
