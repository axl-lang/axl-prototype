package axl.compiler.analysis.parser.data.reference;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.Node;
import axl.compiler.analysis.parser.data.Reference;
import lombok.Data;

import java.util.List;

@Data
public class TypeReference implements Reference, Expression {

    private List<Token> value;

    private List<TypeReference> generics;

    private Object parent;
}
