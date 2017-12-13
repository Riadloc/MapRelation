package serve;

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
 * Created by 小型 on 2017/7/1.
 */
@WebServlet("/Servlet1")
public class Servlet1 extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        String mintime;
        String maxtime;
        String interval1;
        String interval2;
        String stadistance1;
        String stadistance2;
        String lrhour1;
        String lrhour2;
        String mincount;
        String maxcount;

        mintime=request.getParameter("mintime");
        maxtime=request.getParameter("maxtime");
        interval1=request.getParameter("interval1");
        interval2=request.getParameter("interval2");
        stadistance1=request.getParameter("stadistance1");
        stadistance2=request.getParameter("stadistance2");
        lrhour1=request.getParameter("lrhour1");
        lrhour2=request.getParameter("lrhour2");
        mincount=request.getParameter("mincount");
        maxcount=request.getParameter("maxcount");

        //查询chart1
        String sql1="select leasedate,sum(bikenum) from B_LEASEINFOHIS_SUM_PART where LEASEDATE>='"+mintime+"' and LEASEDATE<='"+maxtime+"' group by LEASEDATE";
        Statement statement;
        ResultSet resultSet1;
        Mycon mycon=new Mycon();
        Connection con=mycon.getConnecton();
        Gson gson =new Gson();

        /*
        * 数据chart1
        * */
        List<String> listChart1Date=new ArrayList();
        List<String> listChart1Sum=new ArrayList();
        try{

            statement=con.createStatement();
            resultSet1=statement.executeQuery(sql1);
            while (resultSet1.next()){
                listChart1Date.add(resultSet1.getString(1).substring(8,10));
                listChart1Sum.add(resultSet1.getString(2));
            }
            statement.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        //查询chart2
        String sql2="select cast(dis/0.1 as int)/10,COUNT(*) from B_LEASEINFOHIS_DISANGLE where dis<12 and dis>0 and LEASEDAY>='"+mintime+"' and LEASEDAY<='"+maxtime+"' group by cast(dis/0.1 as int) order by cast(dis/0.1 as int)";
        ResultSet resultSet2=null;
        List<String> listChart2Distance=new ArrayList();
        List<String> listChart2Sum=new ArrayList();
        try{
            statement=con.createStatement();
            resultSet2=statement.executeQuery(sql2);
            while(resultSet2.next()){
                    listChart2Distance.add(resultSet2.getString(1));
                listChart2Sum.add(resultSet2.getString(2));
            }
            statement.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        listChart2Distance.remove(listChart2Distance.size()-1);
        listChart2Sum.remove(listChart2Sum.size()-1);
        //查询chart3
        ResultSet resultSet3=null;
        String sql3="select cast(duration/0.1 as int)/10,COUNT(*)from B_LEASEINFOHIS_DISANGLE where LEASEDAY>='"+mintime+"' and duration<100 and LEASEDAY<='"+maxtime+"'  GROUP by cast(duration/0.1 as int) ORDER BY cast(duration/0.1 as int)";
        List<String> listChart3Duration=new ArrayList();
        List<String> listChart3Sum=new ArrayList();
        try{
            statement=con.createStatement();
            resultSet3=statement.executeQuery(sql3);
            while (resultSet3.next()){
                listChart3Duration.add(resultSet3.getString(1));
                listChart3Sum.add(resultSet3.getString(2));
            }
            statement.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        //查询chart4
        ResultSet resultSet4=null;
        String sql4="select leasehour,count(*) from B_LEASEINFOHIS_DISANGLE where LEASEDAY>='"+mintime+"' and LEASEDAY<='"+maxtime+"' GROUP BY leasehour order by LEASEHOUR";
        List<String> listChart4hour=new ArrayList();
        List<String> listChart4Sum=new ArrayList();
        try{
            statement=con.createStatement();
            resultSet4=statement.executeQuery(sql4);
            while(resultSet4.next()){
                listChart4hour.add(resultSet4.getString(1));
                listChart4Sum.add(resultSet4.getString(2));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        PrintWriter out=response.getWriter();
        out.print("{\"chart1date\":"+gson.toJson(listChart1Date)+",\"chart1data\":"+gson.toJson(listChart1Sum)
                +",\"chart2distance\":"+gson.toJson(listChart2Distance)+",\"chart2sum\":"+gson.toJson(listChart2Sum)
                +",\"chart3duration\":"+gson.toJson(listChart3Duration)+",\"chart3sum\":"+gson.toJson(listChart3Sum)
                +",\"chart4hour\":"+gson.toJson(listChart4hour)+",\"chart4sum\":"+gson.toJson(listChart4Sum)+"}");
        System.out.println("直方图数据已获取");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
