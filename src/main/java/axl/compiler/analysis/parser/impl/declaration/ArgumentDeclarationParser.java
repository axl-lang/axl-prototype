package axl.compiler.analysis.parser.impl.declaration;

import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.declaration.ArgumentDeclaration;
import axl.compiler.analysis.parser.impl.reference.TypeReferenceParser;
import lombok.Getter;

public class ArgumentDeclarationParser extends Parser<ArgumentDeclaration> {

    @Getter
    public static ArgumentDeclarationParser instance = new ArgumentDeclarationParser();

    @Override
    public ArgumentDeclaration analyze() {
        ArgumentDeclaration declaration = new ArgumentDeclaration();
        declaration.setName(tokenStream.expect(TokenType.IDENTIFY));
        declaration.setColon(tokenStream.expect(TokenType.COLON));
        declaration.setReference(TypeReferenceParser.getInstance().analyze());
        return declaration;
    }
}
