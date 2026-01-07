import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MiniLexer {

    public enum TipoToken {
        PALABRA_CLAVE,
        IDENTIFICADOR,
        LITERAL_NUMERICO,
        OPERADOR,
        DELIMITADOR
    }

    public static class Token {
        public TipoToken tipo;
        public String lexema;

        public Token(TipoToken tipo, String lexema) {
            this.tipo = tipo;
            this.lexema = lexema;
        }

        @Override
        public String toString() {
            return "Token: <" + tipo + ", \"" + lexema + "\">";
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce las instrucciones (separadas por espacios):");
        String entrada = scanner.nextLine();
        scanner.close();

        String[] tokens = entrada.split(" ");
        List<Token> tokensList = new ArrayList<>();

        for (String lexema : tokens) {
            if (lexema.isEmpty()) continue;
            
            TipoToken tipo = clasificarToken(lexema);
            Token token = new Token(tipo, lexema);
            tokensList.add(token);
            
            System.out.println(token);
        }

        Token[] tokensClasificados = tokensList.toArray(new Token[0]);
        parse(tokensClasificados);
    }

    private static int currentTokenIndex = 0;
    private static Token[] tokens;

    private static void parse(Token[] tokenArray) {
        tokens = tokenArray;
        currentTokenIndex = 0;
        try {
            parseStmtList();
            if (currentTokenIndex < tokens.length) {
                String found = tokens[currentTokenIndex].lexema;
                throw new RuntimeException("Token inesperado '" + found + "' en la posición " + currentTokenIndex + ", se esperaba fin de entrada");
            }
            System.out.println("Análisis sintáctico exitoso");
        } catch (Exception e) {
            System.out.println("Error de sintaxis: " + e.getMessage());
        }
    }

    private static void parseStmtList() {
        while (currentTokenIndex < tokens.length && (match(TipoToken.IDENTIFICADOR) || match(TipoToken.PALABRA_CLAVE, "print") || match(TipoToken.PALABRA_CLAVE, "int"))) {
            parseStmt();
        }
    }

    private static void parseStmt() {
        if (match(TipoToken.IDENTIFICADOR)) {
            expect(TipoToken.IDENTIFICADOR);
            expect(TipoToken.OPERADOR, "=");
            parseExpr();
            expect(TipoToken.DELIMITADOR, ";");
        } else if (match(TipoToken.PALABRA_CLAVE, "print")) {
            expect(TipoToken.PALABRA_CLAVE, "print");
            expect(TipoToken.DELIMITADOR, "(");
            parseExpr();
            expect(TipoToken.DELIMITADOR, ")");
            expect(TipoToken.DELIMITADOR, ";");
        } else if (match(TipoToken.PALABRA_CLAVE, "int")) {
            expect(TipoToken.PALABRA_CLAVE, "int");
            expect(TipoToken.IDENTIFICADOR);
            expect(TipoToken.OPERADOR, "=");
            parseExpr();
            expect(TipoToken.DELIMITADOR, ";");
        } else {
            String found = currentTokenIndex < tokens.length ? tokens[currentTokenIndex].lexema : "fin de entrada";
            throw new RuntimeException("Se esperaba ID, 'print' o 'int' pero se encontró '" + found + "' en la posición " + currentTokenIndex);
        }
    }

    private static void parseExpr() {
        parseTerm();
        while (match(TipoToken.OPERADOR, "+") || match(TipoToken.OPERADOR, "-")) {
            expect(tokens[currentTokenIndex].tipo, tokens[currentTokenIndex].lexema);
            parseTerm();
        }
    }

    private static void parseTerm() {
        parseFactor();
        while (match(TipoToken.OPERADOR, "*") || match(TipoToken.OPERADOR, "/")) {
            expect(tokens[currentTokenIndex].tipo, tokens[currentTokenIndex].lexema);
            parseFactor();
        }
    }

    private static void parseFactor() {
        if (match(TipoToken.IDENTIFICADOR)) {
            expect(TipoToken.IDENTIFICADOR);
        } else if (match(TipoToken.LITERAL_NUMERICO)) {
            expect(TipoToken.LITERAL_NUMERICO);
        } else if (match(TipoToken.DELIMITADOR, "(")) {
            expect(TipoToken.DELIMITADOR, "(");
            parseExpr();
            expect(TipoToken.DELIMITADOR, ")");
        } else {
            String found = currentTokenIndex < tokens.length ? tokens[currentTokenIndex].lexema : "fin de entrada";
            throw new RuntimeException("Se esperaba ID, NUM o '(' pero se encontró '" + found + "' en la posición " + currentTokenIndex);
        }
    }

    private static boolean match(TipoToken tipo) {
        return currentTokenIndex < tokens.length && tokens[currentTokenIndex].tipo == tipo;
    }

    private static boolean match(TipoToken tipo, String lexema) {
        return currentTokenIndex < tokens.length && tokens[currentTokenIndex].tipo == tipo && tokens[currentTokenIndex].lexema.equals(lexema);
    }

    private static void expect(TipoToken tipo) {
        if (!match(tipo)) {
            String found = currentTokenIndex < tokens.length ? tokens[currentTokenIndex].lexema : "fin de entrada";
            throw new RuntimeException("Se esperaba " + tipo + " pero se encontró '" + found + "' en la posición " + currentTokenIndex);
        }
        currentTokenIndex++;
    }

    private static void expect(TipoToken tipo, String lexema) {
        if (!match(tipo, lexema)) {
            String found = currentTokenIndex < tokens.length ? tokens[currentTokenIndex].lexema : "fin de entrada";
            throw new RuntimeException("Se esperaba '" + lexema + "' pero se encontró '" + found + "' en la posición " + currentTokenIndex);
        }
        currentTokenIndex++;
    }

    public static TipoToken clasificarToken(String lexema) {
        if (lexema.equals("if") || lexema.equals("int") || lexema.equals("print")) {
            return TipoToken.PALABRA_CLAVE;
        } else if (lexema.equals("(") || lexema.equals(")") || lexema.equals(";")) {
            return TipoToken.DELIMITADOR;
        } else if (lexema.equals("+") || lexema.equals("=") || lexema.equals("==") || lexema.equals("-") || lexema.equals("*") || lexema.equals("/")) {
            return TipoToken.OPERADOR;
        } else if (lexema.matches("[0-9]+")) {
            return TipoToken.LITERAL_NUMERICO;
        } else {
            return TipoToken.IDENTIFICADOR;
        }
    }
}