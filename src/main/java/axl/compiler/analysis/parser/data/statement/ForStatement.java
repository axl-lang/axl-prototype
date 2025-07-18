package axl.compiler.analysis.parser.data.statement;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Expression;
import axl.compiler.analysis.parser.data.Node;
import axl.compiler.analysis.parser.data.Statement;
import lombok.Data;

@Data
public class ForStatement implements Statement {

    private Token forToken;

    private Token varToken;

    private Token name;

    private Token colonToken;

    private Expression iterator;

    private Statement body;

    private Object parent;
}
