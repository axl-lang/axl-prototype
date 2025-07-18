package axl.compiler.analysis.common.exception;

import axl.compiler.analysis.common.data.SourceLocation;
import lombok.Getter;

@Getter
public abstract class AnalysisSourceException extends AnalysisException {

    private final SourceLocation location;

    public AnalysisSourceException(SourceLocation location, String message) {
        super(message);

        this.location = location;
    }
}
