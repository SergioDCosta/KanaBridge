package com.kanabridge;
/** Exposes the production query to SQLite integration tests on the host. */
public final class LexiconQueryProbe {
    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        LexiconQueries query = new LexiconQueries(new String(java.util.Base64.getDecoder().decode(args[0]), java.nio.charset.StandardCharsets.UTF_8), Boolean.parseBoolean(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        System.out.println(query.sql); for (String value : query.args) System.out.println(value);
    }
}
