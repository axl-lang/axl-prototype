package axl.compiler.analysis.lexer.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenType {
    LEFT_PARENT(TokenGroup.DELIMITER, "("),
    RIGHT_PARENT(TokenGroup.DELIMITER, ")"),
    LEFT_BRACE(TokenGroup.DELIMITER, "{"),
    RIGHT_BRACE(TokenGroup.DELIMITER, "}"),
    LEFT_SQUARE(TokenGroup.DELIMITER, "["),
    RIGHT_SQUARE(TokenGroup.DELIMITER, "]"),
    COMMA(TokenGroup.DELIMITER, ","),
    DOT(TokenGroup.OPERATOR, "."),
    SEMI(TokenGroup.DELIMITER, ";"),
    COLON(TokenGroup.DELIMITER, ":"),
    IMPLICATION(TokenGroup.DELIMITER, "->"),

    PLUS(TokenGroup.OPERATOR, "+"),
    MINUS(TokenGroup.OPERATOR, "-"),
    MULTIPLY(TokenGroup.OPERATOR, "*"),
    DIVIDE(TokenGroup.OPERATOR, "/"),
    MODULO(TokenGroup.OPERATOR, "%"),

    AND(TokenGroup.OPERATOR, "&&"),
    OR(TokenGroup.OPERATOR, "||"),
    NOT(TokenGroup.OPERATOR, "!"),

    EQUALS(TokenGroup.OPERATOR, "=="),
    NOT_EQUALS(TokenGroup.OPERATOR, "!="),
    GREATER(TokenGroup.OPERATOR, ">"),
    LESS(TokenGroup.OPERATOR, "<"),
    GREATER_OR_EQUAL(TokenGroup.OPERATOR, ">="),
    LESS_OR_EQUAL(TokenGroup.OPERATOR, "<="),

    ASSIGN(TokenGroup.OPERATOR, "="),
    PLUS_ASSIGN(TokenGroup.OPERATOR, "+="),
    MINUS_ASSIGN(TokenGroup.OPERATOR, "-="),
    MULTIPLY_ASSIGN(TokenGroup.OPERATOR, "*="),
    DIVIDE_ASSIGN(TokenGroup.OPERATOR, "/="),
    MODULO_ASSIGN(TokenGroup.OPERATOR, "%="),

    UNARY_MINUS(TokenGroup.OPERATOR, "-"),
    POSTFIX_INCREMENT(TokenGroup.OPERATOR, "++"),
    POSTFIX_DECREMENT(TokenGroup.OPERATOR, "--"),
    PREFIX_INCREMENT(TokenGroup.OPERATOR, "++"),
    PREFIX_DECREMENT(TokenGroup.OPERATOR, "--"),

    AT_SYMBOL(TokenGroup.OPERATOR, "@"),
    QUESTION_MARK(TokenGroup.OPERATOR, "?"),
    IS(TokenGroup.OPERATOR, "is"),
    AS(TokenGroup.OPERATOR, "as"),

    PUB(TokenGroup.KEYWORD, "pub"),
    PACKAGE(TokenGroup.KEYWORD, "package"),
    IMPORT(TokenGroup.KEYWORD, "import"),

    CLASS(TokenGroup.KEYWORD, "struct"),
    FN(TokenGroup.KEYWORD, "fn"),
    RETURN(TokenGroup.KEYWORD, "return"),
    BREAK(TokenGroup.KEYWORD, "break"),
    CONTINUE(TokenGroup.KEYWORD, "continue"),
    THROW(TokenGroup.KEYWORD, "throw"),
    VAL(TokenGroup.KEYWORD, "val"),
    VAR(TokenGroup.KEYWORD, "var"),
    IF(TokenGroup.KEYWORD, "if"),
    ELSE(TokenGroup.KEYWORD, "else"),
    FOR(TokenGroup.KEYWORD, "for"),
    WHILE(TokenGroup.KEYWORD, "while"),
    THIS(TokenGroup.KEYWORD, "this"),

    TRUE(TokenGroup.LITERAL, "true"),
    FALSE(TokenGroup.LITERAL, "false"),

    IDENTIFY(TokenGroup.IDENTIFY, null),

    HEX_LONG_NUMBER(TokenGroup.LITERAL, null),
    DEC_LONG_NUMBER(TokenGroup.LITERAL, null),
    HEX_NUMBER(TokenGroup.LITERAL, null),
    DEC_NUMBER(TokenGroup.LITERAL, null),
    FLOAT_NUMBER(TokenGroup.LITERAL, null),
    DOUBLE_NUMBER(TokenGroup.LITERAL, null),
    CHAR_LITERAL(TokenGroup.LITERAL, null),
    STRING_LITERAL(TokenGroup.LITERAL, null);

    private final TokenGroup group;

    private final String representation;
}


//    BIT_AND(TokenGroup.OPERATOR, "&"),
//    BIT_OR(TokenGroup.OPERATOR, "|"),
//    BIT_NOT(TokenGroup.OPERATOR, "~"),
//    BIT_XOR(TokenGroup.OPERATOR, "^"),
//    BIT_SHIFT_LEFT(TokenGroup.OPERATOR, "<<"),
//    BIT_SHIFT_RIGHT(TokenGroup.OPERATOR, ">>"),

//    BIT_AND_ASSIGN(TokenGroup.OPERATOR, "&="),
//    BIT_OR_ASSIGN(TokenGroup.OPERATOR, "|="),
//    BIT_XOR_ASSIGN(TokenGroup.OPERATOR, "^="),
//    BIT_SHIFT_LEFT_ASSIGN(TokenGroup.OPERATOR, "<<="),
//    BIT_SHIFT_RIGHT_ASSIGN(TokenGroup.OPERATOR, ">>="),