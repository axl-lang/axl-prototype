package axl.compiler.analysis.parser.data.declaration;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Declaration;
import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.Node;
import axl.compiler.analysis.parser.data.Reference;
import axl.compiler.analysis.parser.data.reference.TypeReference;
import lombok.Data;

@Data
public class VarDeclaration implements Declaration {

    private Token accessFlag;

    private Token declarationFlag;

    private Token name;

    private TypeReference reference;

    private Token operator;

    private Expression value;

    private Object parent;
}
