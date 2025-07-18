package axl.compiler.analysis.parser.exception;

import axl.compiler.analysis.common.data.SourceLocation;
import axl.compiler.analysis.common.exception.AnalysisSourceException;

public class ParserException extends AnalysisSourceException {

    public ParserException(SourceLocation location, String message) {
        super(location, message + ": " + location);
    }
}
