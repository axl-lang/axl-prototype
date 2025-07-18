package axl.compiler.analysis.parser.data.declaration;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.Declaration;
import axl.compiler.analysis.parser.data.Node;
import axl.compiler.analysis.parser.data.reference.TypeReference;
import lombok.Data;

import java.util.List;

@Data
public class ClassDeclaration implements Declaration {

    private Token accessFlag;

    private Token classToken;

    private TypeReference classReference;

    private Token implication;

    private TypeReference superClassReference;

    private List<Declaration> declarations;

    private Object parent;
}
