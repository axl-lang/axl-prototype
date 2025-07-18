package axl.compiler.analysis.parser.impl.reference;

import axl.compiler.analysis.lexer.data.TokenType;
import axl.compiler.analysis.parser.Parser;
import axl.compiler.analysis.parser.data.reference.TypeReference;
import lombok.Getter;

import java.util.ArrayList;

public class TypeReferenceParser extends Parser<TypeReference> {

    @Getter
    public static TypeReferenceParser instance = new TypeReferenceParser();

    @Override
    public TypeReference analyze() {
        TypeReference reference = new TypeReference();

        reference.setValue(new ArrayList<>());
        reference.getValue().add(tokenStream.expect(TokenType.IDENTIFY));
        while (tokenStream.matchThen(TokenType.DOT, dot -> {
            reference.getValue().add(tokenStream.expect(TokenType.IDENTIFY));
        }));

        tokenStream.matchThen(TokenType.LESS, lessToken -> {
            reference.setGenerics(new ArrayList<>());
            tokenStream.matchThen(
                    TokenType.GREATER,
                    null,
                    ignored -> {
                        reference.getGenerics().add(TypeReferenceParser.getInstance().analyze());
                        while (tokenStream.matchThen(TokenType.COMMA, dot -> {
                            reference.getGenerics().add(TypeReferenceParser.getInstance().analyze());
                        }));
                        tokenStream.expect(TokenType.GREATER);
                    }
            );
        });

        return reference;
    }
}
