package axl.compiler.linker.type;

import axl.compiler.analysis.lexer.data.Token;
import axl.compiler.analysis.parser.data.reference.TypeReference;

public class TypeUtils {

    public static String fromTypeReference(TypeReference reference) {
        if (reference == null) {
            return "java.lang.Void";
        }

        String type = String.join(".", reference.getValue().stream().map(Token::getValue).toList());
        if (reference.getGenerics() == null || reference.getGenerics().isEmpty())
            return type;

        return type + "<" + String.join(",", reference.getGenerics().stream().map(TypeUtils::fromTypeReference).toList()) + ">";
    }
}
