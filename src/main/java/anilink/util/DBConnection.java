package anilink.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    public static final String HOST = "localhost";
    public static final int PORT = 3306;
    public static final String DATABASE = "anilink";
    public static final String USER = "root";
    public static final String PASSWORD = "";

    private static Connection connection;

    private DBConnection() {
    }

    public static synchronized Connection get() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL driver not found", e);
            }
            // connectionTimeZone=LOCAL: MySQL stores created_at/updated_at as
            // local wall-clock time (its NOW() matches the Windows clock), so
            // the driver must not translate between zones. The previous
            // serverTimezone=UTC made it treat those values as UTC and shift
            // every timestamp forward by the local UTC offset.
            String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                    + "?useSSL=false&allowPublicKeyRetrieval=true"
                    + "&connectionTimeZone=LOCAL&forceConnectionTimeZoneToSession=false"
                    + "&characterEncoding=utf8";
            connection = DriverManager.getConnection(url, USER, PASSWORD);
        }
        return connection;
    }

    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
            connection = null;
        }
    }
}
