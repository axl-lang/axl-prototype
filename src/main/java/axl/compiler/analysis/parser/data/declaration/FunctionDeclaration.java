package axl.compiler.analysis.parser.data.declaration;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Declaration;
import axl.compiler.analysis.parser.data.Node;
import axl.compiler.analysis.parser.data.Reference;
import axl.compiler.analysis.parser.data.Statement;
import axl.compiler.analysis.parser.data.reference.TypeReference;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FunctionDeclaration implements Declaration {

    private Token accessFlag;

    private Token fnToken;

    private Token name;

    private List<ArgumentDeclaration> argumentDeclarations = new ArrayList<>();

    private Token implicationToken;

    private TypeReference resultReference;

    private Statement body;

    private Object parent;
}
