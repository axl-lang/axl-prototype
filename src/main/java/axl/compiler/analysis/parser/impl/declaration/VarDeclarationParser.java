package axl.compiler.analysis.parser.impl.declaration;

import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.declaration.VarDeclaration;
import axl.compiler.analysis.parser.data.statement.VarStatement;
import axl.compiler.analysis.parser.impl.expression.ExpressionParser;
import axl.compiler.analysis.parser.impl.reference.TypeReferenceParser;
import lombok.Getter;

public class VarDeclarationParser extends Parser<VarDeclaration> {

    @Getter
    public static VarDeclarationParser instance = new VarDeclarationParser();

    @Override
    public VarDeclaration analyze() {
        VarDeclaration declaration = new VarDeclaration();

        declaration.setDeclarationFlag(tokenStream.expect(TokenType.VAR));
        declaration.setName(tokenStream.expect(TokenType.IDENTIFY));

        tokenStream.matchThen(TokenType.COLON, ignored -> declaration.setReference(TypeReferenceParser.getInstance().analyze()));
        tokenStream.matchThen(TokenType.ASSIGN, ignored -> declaration.setValue(ExpressionParser.getInstance().analyze()));

        if (declaration.getReference() == null && declaration.getValue() == null) {
            tokenStream.expect(TokenType.ASSIGN);
        }

        return declaration;
    }
}
