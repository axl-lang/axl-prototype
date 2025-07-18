package axl.compiler.analysis.parser.data.declaration;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Declaration;
import axl.compiler.analysis.parser.data.Node;
import lombok.Data;

import java.util.List;

@Data
public class LocationDeclaration implements Declaration {

    private List<Token> location;

    private Object parent;
}
