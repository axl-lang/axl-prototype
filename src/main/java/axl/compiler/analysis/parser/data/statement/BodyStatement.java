package axl.compiler.analysis.parser.data.statement;

import axl.compiler.analysis.parser.data.Node;
import axl.compiler.analysis.parser.data.Statement;
import lombok.Data;

import java.util.List;

@Data
public class BodyStatement implements Statement {

    private List<Statement> statements;

    private Object parent;
}
