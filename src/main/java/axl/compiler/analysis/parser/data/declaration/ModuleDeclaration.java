package axl.compiler.analysis.parser.data.declaration;

import axl.compiler.analysis.parser.data.Declaration;
import axl.compiler.analysis.parser.data.Node;
import lombok.Data;

import java.util.List;

@Data
public class ModuleDeclaration implements Declaration {

    private String filename;

    private LocationDeclaration packageDeclaration;

    private List<LocationDeclaration> importDeclarations;

    private List<Declaration> declarations;

    private Object parent;
}
