package serve;

import util.Mycon;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by 小型 on 2017/10/18.
 */
@WebServlet("/ServletOneStation")
public class ServletOneStation extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        String stationname=request.getParameter("stationId");
        String sql="select baidu_x,baidu_y from B_STATIONINFO_BRIEF where stationname='"+stationname+"'";
        Statement statement;
        ResultSet resultSet1;
        Mycon mycon=new Mycon();
        Connection con=mycon.getConnecton();
        String baidu_x=null;
        String baidu_y=null;
        try{

            statement=con.createStatement();
            resultSet1=statement.executeQuery(sql);
            while (resultSet1.next()){
                 baidu_x=resultSet1.getString(1);
                 baidu_y=resultSet1.getString(2);
            }
            statement.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        PrintWriter out=response.getWriter();
        System.out.print("已获得坐标");
        out.print("{\"baidu_x\":\""+baidu_x+"\",\"baidu_y\":\""+baidu_y+"\"}");

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
