package axl.compiler.analysis.parser.data.statement;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.Node;
import axl.compiler.analysis.parser.data.Statement;
import lombok.Data;

@Data
public class IfStatement implements Statement {

    private Token ifToken;

    private Expression condition;

    private Statement then;

    private Token elseToken;

    private Statement thenElse;

    private Object parent;
}
