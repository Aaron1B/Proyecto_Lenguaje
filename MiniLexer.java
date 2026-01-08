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

    private static int indiceTokenActual = 0;
    private static Token[] tokens;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce las instrucciones:");
        String entrada = scanner.nextLine();
        scanner.close();

        entrada = entrada.replaceAll("([;()+-*/=])", " $1 ").replaceAll("\\s+", " ").trim();

        String[] lexemas = entrada.split("\\s+");
        List<Token> listaTokens = new ArrayList<>();

        for (String lexema : lexemas) {
            if (lexema.isEmpty()) continue;
            TipoToken tipo = clasificarToken(lexema);
            listaTokens.add(new Token(tipo, lexema));
        }

        Token[] tokensClasificados = listaTokens.toArray(new Token[0]);
        for(Token t : tokensClasificados) {
            System.out.println(t);
        }
        
        analizar(tokensClasificados);
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

    private static void analizar(Token[] arregloTokens) {
        tokens = arregloTokens;
        indiceTokenActual = 0;
        try {
            while (indiceTokenActual < tokens.length) {
                analizarSentencia();
            }
            System.out.println("La cadena es VALIDA sintácticamente.");
        } catch (Exception e) {
            System.out.println("ERROR SINTÁCTICO: " + e.getMessage());
        }
    }

    private static void analizarSentencia() {
        Token tokenActual = anticipar();

        if (tokenActual.tipo == TipoToken.IDENTIFICADOR) {
            coincidir(TipoToken.IDENTIFICADOR);
            coincidir(TipoToken.OPERADOR, "=");
            analizarExpresion();
            coincidir(TipoToken.DELIMITADOR, ";");
            
        } else if (tokenActual.tipo == TipoToken.PALABRA_CLAVE && tokenActual.lexema.equals("print")) {
            coincidir(TipoToken.PALABRA_CLAVE, "print");
            coincidir(TipoToken.DELIMITADOR, "(");
            analizarExpresion();
            coincidir(TipoToken.DELIMITADOR, ")");
            coincidir(TipoToken.DELIMITADOR, ";");
            
        } else {
            throw new RuntimeException("Se esperaba un Identificador o 'print', pero se encontró: " + tokenActual.lexema);
        }
    }

    private static void analizarExpresion() {
        analizarTermino();
        while (anticipar().lexema.equals("+") || anticipar().lexema.equals("-")) {
            coincidir(TipoToken.OPERADOR);
            analizarTermino();
        }
    }

    private static void analizarTermino() {
        analizarFactor();
        while (anticipar().lexema.equals("*") || anticipar().lexema.equals("/")) {
            coincidir(TipoToken.OPERADOR);
            analizarFactor();
        }
    }

    private static void analizarFactor() {
        Token token = anticipar();
        if (token.tipo == TipoToken.IDENTIFICADOR) {
            coincidir(TipoToken.IDENTIFICADOR);
        } else if (token.tipo == TipoToken.LITERAL_NUMERICO) {
            coincidir(TipoToken.LITERAL_NUMERICO);
        } else if (token.lexema.equals("(")) {
            coincidir(TipoToken.DELIMITADOR, "(");
            analizarExpresion();
            coincidir(TipoToken.DELIMITADOR, ")");
        } else {
            throw new RuntimeException("Se esperaba ID, NUM o '(', pero se encontró: " + token.lexema);
        }
    }

    private static Token anticipar() {
        if (indiceTokenActual < tokens.length) {
            return tokens[indiceTokenActual];
        }
        return new Token(TipoToken.FIN, "");
    }

    private static void coincidir(TipoToken tipoEsperado) {
        Token t = anticipar();
        if (t.tipo == tipoEsperado) {
            indiceTokenActual++;
        } else {
            throw new RuntimeException("Se esperaba " + tipoEsperado + " pero se encontró " + t.tipo);
        }
    }

    private static void coincidir(TipoToken tipoEsperado, String lexemaEsperado) {
        Token t = anticipar();
        if (t.tipo == tipoEsperado && t.lexema.equals(lexemaEsperado)) {
            indiceTokenActual++;
        } else {
            throw new RuntimeException("Se esperaba '" + lexemaEsperado + "' pero se encontró '" + t.lexema + "'");
        }
    }
}