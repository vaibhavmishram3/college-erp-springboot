package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java — Central MySQL connection manager.
 * Edit the four constants below to match your setup.
 * Requires: mysql-connector-j-*.jar on the classpath.
 */
public class DBConnection {

    private static final String HOST     = "localhost";
    private static final int    PORT     = 3306;
    private static final String DB_NAME  = "college_db";   // ← your DB
    private static final String USER     = "root";         // ← your username
    private static final String PASSWORD = "123456";             // ← your password

    private static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "MySQL JDBC Driver not found. Add mysql-connector-j to classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}