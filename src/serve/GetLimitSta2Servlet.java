package serve;

import bean.LimiteStaBean;
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
 * Created by 小型 on 2017/9/12.
 */
@WebServlet("/GetLimitSta2Servlet")
public class GetLimitSta2Servlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        String stationId=null;
        String angle1=null;
        String angle2=null;
        String dis1=null;
        String dis2=null;
        String time1=null;
        String time2=null;
        stationId=request.getParameter("stationId");
        angle1=request.getParameter("angle1");
        angle2=request.getParameter("angle2");
        dis1=request.getParameter("dis1");
        dis2=request.getParameter("dis2");
        time1=request.getParameter("time1");
        time2=request.getParameter("time2");
        String sql="select  stationname,baidu_x,baidu_y,stationid,count(*) " +
                "from B_LEASEINFOHIS_BRIEF,S_STATIONDISANGLE_6KM,B_STATIONINFO_BRIEF  " +
                "where LEASEDAY<='"+time2+"'  and LEASEDAY>='"+time1+"' " +
                "and S_STATIONDISANGLE_6KM.STATION1=returnstation " +
                "and distance<="+dis2+" and distance>="+dis1+" and angle<="+angle2+" and angle>="+angle1+
                " and leasestation=B_STATIONINFO_BRIEF.STATIONID " +
                "and station2=leasestation  and returnstation=(select stationid from B_STATIONINFO_BRIEF where stationname='"+stationId+"')"+
                "group by stationname,baidu_x,baidu_y,stationid";
        Statement statement;
        ResultSet resultSet1;
        Mycon mycon=new Mycon();
        List<LimiteStaBean> list =new ArrayList();
        Connection con=mycon.getConnecton();
        int max=0;
        try{

            statement=con.createStatement();
            resultSet1=statement.executeQuery(sql);
            while (resultSet1.next()){
                LimiteStaBean station2Bean=new LimiteStaBean();
                station2Bean.setStationname(resultSet1.getString(1));
                station2Bean.setBaidux(resultSet1.getString(2));
                station2Bean.setBaiduy(resultSet1.getString(3));
                station2Bean.setStationid(resultSet1.getString(4));
                station2Bean.setCount(resultSet1.getInt(5));
                if(max<resultSet1.getInt(5)){
                    max=resultSet1.getInt(5);
                }
                list.add(station2Bean);
            }
            statement.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        PrintWriter out=response.getWriter();
        Gson gson=new Gson();
        String json=gson.toJson(list);
        System.out.println("以获取附近站点");
        out.print(json+"|"+max);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
