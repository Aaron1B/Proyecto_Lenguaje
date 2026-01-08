import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MiniLexer {

    public enum TipoToken {
        PALABRA_CLAVE,
        IDENTIFICADOR,
        LITERAL_NUMERICO,
        OPERADOR,
        DELIMITADOR,
        FIN
    }

    public static class Token {
        public TipoToken tipo;
        public String lexema;

        public Token(TipoToken tipo, String lexema) {
            this.tipo = tipo;
            this.lexema = lexema;
        }

        public String toString() {
            return "Token: <" + tipo + ", \"" + lexema + "\">";
        }
    }

    private static int currentTokenIndex = 0;
    private static Token[] tokens;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce las instrucciones:");
        String entrada = scanner.nextLine();
        scanner.close();

        entrada = entrada.replace(";", " ; ")
                         .replace("(", " ( ")
                         .replace(")", " ) ")
                         .replace("+", " + ")
                         .replace("-", " - ")
                         .replace("*", " * ")
                         .replace("/", " / ")
                         .replace("=", " = ");
        
        while (entrada.contains("  ")) {
            entrada = entrada.replace("  ", " ");
        }

        String[] lexemas = entrada.trim().split(" ");
        List<Token> tokensList = new ArrayList<>();

        for (String lexema : lexemas) {
            if (lexema.isEmpty()) continue;
            TipoToken tipo = clasificarToken(lexema);
            tokensList.add(new Token(tipo, lexema));
        }

        Token[] tokensClasificados = tokensList.toArray(new Token[0]);
        for(Token t : tokensClasificados) {
            System.out.println(t);
        }
        
        parse(tokensClasificados);
    }

    public static TipoToken clasificarToken(String lexema) {
        if (lexema.equals("print")) {
            return TipoToken.PALABRA_CLAVE;
        } else if (lexema.equals("(") || lexema.equals(")") || lexema.equals(";")) {
            return TipoToken.DELIMITADOR;
        } else if (lexema.matches("[+\\-*/=]")) {
            return TipoToken.OPERADOR;
        } else if (lexema.matches("[0-9]+")) {
            return TipoToken.LITERAL_NUMERICO;
        } else {
            return TipoToken.IDENTIFICADOR;
        }
    }

    private static void parse(Token[] tokenArray) {
        tokens = tokenArray;
        currentTokenIndex = 0;
        try {
            while (currentTokenIndex < tokens.length) {
                parseStmt();
            }
            System.out.println("La cadena es VALIDA sintácticamente.");
        } catch (Exception e) {
            System.out.println("ERROR SINTÁCTICO: " + e.getMessage());
        }
    }

    private static void parseStmt() {
        Token tokenActual = lookahead();

        if (tokenActual.tipo == TipoToken.IDENTIFICADOR) {
            match(TipoToken.IDENTIFICADOR);
            match(TipoToken.OPERADOR, "=");
            parseExpr();
            match(TipoToken.DELIMITADOR, ";");
            
        } else if (tokenActual.tipo == TipoToken.PALABRA_CLAVE && tokenActual.lexema.equals("print")) {
            match(TipoToken.PALABRA_CLAVE, "print");
            match(TipoToken.DELIMITADOR, "(");
            parseExpr();
            match(TipoToken.DELIMITADOR, ")");
            match(TipoToken.DELIMITADOR, ";");
            
        } else {
            throw new RuntimeException("Se esperaba un Identificador o 'print', pero se encontró: " + tokenActual.lexema);
        }
    }

    private static void parseExpr() {
        parseTerm();
        while (lookahead().lexema.equals("+") || lookahead().lexema.equals("-")) {
            match(TipoToken.OPERADOR);
            parseTerm();
        }
    }

    private static void parseTerm() {
        parseFactor();
        while (lookahead().lexema.equals("*") || lookahead().lexema.equals("/")) {
            match(TipoToken.OPERADOR);
            parseFactor();
        }
    }

    private static void parseFactor() {
        Token token = lookahead();
        if (token.tipo == TipoToken.IDENTIFICADOR) {
            match(TipoToken.IDENTIFICADOR);
        } else if (token.tipo == TipoToken.LITERAL_NUMERICO) {
            match(TipoToken.LITERAL_NUMERICO);
        } else if (token.lexema.equals("(")) {
            match(TipoToken.DELIMITADOR, "(");
            parseExpr();
            match(TipoToken.DELIMITADOR, ")");
        } else {
            throw new RuntimeException("Se esperaba ID, NUM o '(', pero se encontró: " + token.lexema);
        }
    }

    private static Token lookahead() {
        if (currentTokenIndex < tokens.length) {
            return tokens[currentTokenIndex];
        }
        return new Token(TipoToken.FIN, "");
    }

    private static void match(TipoToken tipoEsperado) {
        Token t = lookahead();
        if (t.tipo == tipoEsperado) {
            currentTokenIndex++;
        } else {
            throw new RuntimeException("Se esperaba " + tipoEsperado + " pero se encontró " + t.tipo);
        }
    }

    private static void match(TipoToken tipoEsperado, String lexemaEsperado) {
        Token t = lookahead();
        if (t.tipo == tipoEsperado && t.lexema.equals(lexemaEsperado)) {
            currentTokenIndex++;
        } else {
            throw new RuntimeException("Se esperaba '" + lexemaEsperado + "' pero se encontró '" + t.lexema + "'");
        }
    }
}