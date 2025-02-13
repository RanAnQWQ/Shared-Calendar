package com.app.login.util;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * function： 数据库工具类，连接数据库用
 */
public class JDBCUtils {
    private static final String TAG = "mysql-party-JDBCUtils";

    private static String driver = "com.mysql.jdbc.Driver";// MySql驱动

    private static String dbName = "user";// 数据库名称

    private static String user = "root";// 用户名

    private static String password = "12345";// 密码
    public static ThreadLocal<Connection> tl = new ThreadLocal<>();

    public static Connection getConn() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://rm-bp1388x4titmrebh7go.mysql.rds.aliyuncs.com:3306/user?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&failOverReadOnly=false&maxReconnects=10";
            connection = DriverManager.getConnection(url, "anran", "ar20039!108yY");
            tl.set(connection);
        } catch (SQLException e) {
            Log.e(TAG, "fail to connect " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "fail driver: " + e.getMessage());
        }
        return connection;
    }

}
