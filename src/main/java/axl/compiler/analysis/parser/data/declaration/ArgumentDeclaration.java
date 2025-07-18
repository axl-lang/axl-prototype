package axl.compiler.analysis.parser.data.declaration;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Declaration;
import axl.compiler.analysis.parser.data.Node;
import axl.compiler.analysis.parser.data.reference.TypeReference;
import lombok.Data;

@Data
public class ArgumentDeclaration implements Declaration {

    private Token name;

    private Token colon;

    private TypeReference reference;

    private Object parent;
}
