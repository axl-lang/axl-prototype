package axl.compiler.analysis.parser.data.statement;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Node;
import axl.compiler.analysis.parser.data.Statement;
import lombok.Data;

@Data
public class OperationStatement<T> implements Statement {

    private Token operator;

    private T operand;

    private Object parent;
}
