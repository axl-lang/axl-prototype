package axl.compiler.analysis.lexer.data;

import axl.compiler.analysis.common.data.SourceSection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Token {

    private String value;

    private SourceSection section;

    private TokenType type;
}
