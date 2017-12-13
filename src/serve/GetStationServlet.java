package serve;

import bean.Station2Bean;
import com.google.gson.Gson;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 小型 on 2017/8/15.
 */
@WebServlet( "/GetStationServlet")
public class GetStationServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        /*String time1;
        String time2;
        String rollnum;
        String anglenum;
        time1=request.getParameter("time1");
        time2=request.getParameter("time2");
        rollnum=request.getParameter("rollnum");
        anglenum=*/
        String sql1="select  DISTINCT stationname,baidu_x,baidu_y,stationid" +
                " from B_STATIONINFO_BRIEF,S_STATIONDISANGLE_6KM " +
                " where S_STATIONDISANGLE_6KM.STATION1=B_STATIONINFO_BRIEF.STATIONID";
        Statement statement;
        ResultSet resultSet1;
        Mycon mycon=new Mycon();
        List<Station2Bean> list =new ArrayList();
        Connection con=mycon.getConnecton();
        try{

            statement=con.createStatement();
            resultSet1=statement.executeQuery(sql1);
            while (resultSet1.next()){
                Station2Bean station2Bean=new Station2Bean();
                station2Bean.setStationname(resultSet1.getString(1));
                station2Bean.setBaidux(resultSet1.getString(2));
                station2Bean.setBaiduy(resultSet1.getString(3));
                station2Bean.setStationid(resultSet1.getString(4));
                list.add(station2Bean);
            }
            statement.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        PrintWriter out=response.getWriter();
        Gson gson=new Gson();
        String json=gson.toJson(list);
        System.out.println("以获取所有站点");
        out.print(json);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
