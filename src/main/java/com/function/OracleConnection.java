package com.function;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class OracleConnection {

    private static String walletPath = null;

    static {
        try {
            // 1. Intentar wallet local (Mac/desarrollo)
            String tnsAdmin = System.getenv("TNS_ADMIN");
            if (tnsAdmin != null && !tnsAdmin.isEmpty() && new java.io.File(tnsAdmin).exists()) {
                walletPath = tnsAdmin;
                System.setProperty("oracle.net.tns_admin", walletPath);
                System.out.println("✅ Wallet local: " + walletPath);
            } else {
                // 2. Extraer del JAR (Azure)
                walletPath = extractWallet();
                System.setProperty("oracle.net.tns_admin", walletPath);
                System.out.println("✅ Wallet extraído del JAR: " + walletPath);
            }
        } catch (Exception e) {
            System.err.println("❌ Error inicializando wallet: " + e.getMessage());
        }
    }

    private static String extractWallet() throws IOException {
        Path tempDir = Files.createTempDirectory("oracle_wallet_");

        String[] archivos = {"cwallet.sso", "ewallet.p12", "tnsnames.ora", "sqlnet.ora"};

        for (String archivo : archivos) {
            InputStream is = OracleConnection.class.getResourceAsStream("/wallet/" + archivo);

            if (is == null)
                is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("wallet/" + archivo);

            if (is != null) {
                Files.copy(is, tempDir.resolve(archivo), StandardCopyOption.REPLACE_EXISTING);
                is.close();
                System.out.println("📄 Copiado: " + archivo);
            } else {
                System.out.println("❌ No encontrado: " + archivo);
            }
        }

        // Sobreescribir sqlnet.ora con path temporal real
        String sqlnetContent =
            "WALLET_LOCATION = (SOURCE = (METHOD = file)(METHOD_DATA = (DIRECTORY=\""
            + tempDir.toString() + "\")))\n"
            + "SSL_SERVER_DN_MATCH=yes\n";

        Files.write(tempDir.resolve("sqlnet.ora"),
            sqlnetContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("📝 sqlnet.ora → " + tempDir);
        return tempDir.toString();
    }

    public static Connection getConnection() throws SQLException {
        String url      = System.getenv("ORACLE_URL");
        String user     = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");

        if (url == null || user == null || password == null) {
            throw new SQLException("Variables de entorno Oracle no configuradas");
        }

        if (walletPath == null) {
            throw new SQLException("Wallet no pudo ser inicializado");
        }

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("oracle.net.tns_admin", walletPath);
        props.setProperty("oracle.net.wallet_location",
            "(SOURCE=(METHOD=file)(METHOD_DATA=(DIRECTORY=" + walletPath + ")))");

        // Limpiar TNS_ADMIN de la URL si viene con él
        String cleanUrl = url.contains("?TNS_ADMIN=") 
            ? url.substring(0, url.indexOf("?TNS_ADMIN=")) 
            : url;

        return DriverManager.getConnection(cleanUrl, props);
    }
}