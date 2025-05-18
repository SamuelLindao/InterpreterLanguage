package com.craftinginterpretes.lox;
import java.util.List;
import static com.craftinginterpretes.lox.TokenType.*;

//
//expression → equality
//equality   → comparison
//comparison → term
//term       → factor
//factor     → unary
//unary      → primary


//Esse é um parser, então precisamos ter em mente o que estamos lidando.
//Como visto acima o parser possui uma ordem de precedencia. Assim como uma linguagem de prog.
//ele primeiro chama o expression se verifica se equality (sinal de igual)
//depois ele vai verificar se tem comparações
//e verifica se tem termos
//depois verifica se é soma, multiplicação, divisão, ou unary (-1)

class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;

    }
    private Expr comparison() {
        Expr expr = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        return null;
    }

    //Match verifica os TOKENS e se são do tipo que eu quero. Interessante

//    Expr expr = term(); → lê 3
//
//    match(<, <=, >, >=) → vê < → consome <
//
//            Token operator = previous(); → <
//
//            Expr right = term(); → lê 4
//
//    expr = new Binary(3, <, 4)
//
//    match(<, <=, >, >=) → vê > → consome >
//
//    operator = previous(); → >
//
//    right = term(); → lê 2
//
//    expr = new Binary(expr, >, 2)
//    Agora expr é: ((3 < 4) > 2)
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;

    }
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }

    }
        private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }


}