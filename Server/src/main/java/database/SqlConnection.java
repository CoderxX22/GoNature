package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlConnection {
    private static String dburl;
    private static String dbuser;
    private static String dbpass;

    public static void setConnectionFields(String url, String user, String pass) {
        dburl = url;
        dbuser = user;
        dbpass = pass;
    }
    public static void setUrlBack()
    {
        dburl = "jdbc:mysql://localhost/gonature?serverTimezone=UTC";
    }
    public static void setUrlWithoutGonature()
    {
        dburl = "jdbc:mysql://localhost?serverTimezone=UTC";
    }

    /**
     * Establishes new connection to the database
     * @return connection instance
     */
    public static Connection getConnection() {
        try {
            Connection con;
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(dburl,dbuser,dbpass);  //return new Connection to db
            return con;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
