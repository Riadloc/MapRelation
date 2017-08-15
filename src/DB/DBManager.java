package DB;

import java.sql.*;

/**
 * Created by Yoyoth on 2016/5/16.
 */
public class DBManager {

    /**
     * 获取与oracle的连接
     * @return
     */
    public static Connection getConnection() {

        Connection con = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            String url = "jdbc:oracle:thin:@localhost:1521:oracle";
            String user = "scott";// 用户名,系统默认的账户名
            String password = "scott";// 你安装时选设置的密码
            con = DriverManager.getConnection(url, user, password);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("请检查用户名，密码是否正确！或oracle相关服务是否开启");
        }
        return con;
    }


    /**
     * 关闭Connection，PreparedStatement，ResultSet
     *
     * @param conn
     * @param pstmt
     * @param rs
     */
    public static void close(Connection conn, PreparedStatement pstmt,
                             ResultSet rs) {
        try {
            conn.close();
            pstmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeAll(PreparedStatement pstmt,
                             ResultSet rs) {
        try {
            pstmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void closeCon(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

