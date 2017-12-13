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
 * Created by 小型 on 2017/7/7.
 */
@WebServlet("/RangServlet")
public class RangServlet extends HttpServlet {
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
        String mincount=null;
        String maxcount=null;
        mintime=request.getParameter("mintime");
        maxtime=request.getParameter("maxtime");
        interval1=request.getParameter("interval1");
        interval2=request.getParameter("interval2");
        stadistance1=request.getParameter("stadistance1");
        stadistance2=request.getParameter("stadistance2");
        lrhour1=request.getParameter("lrhour1");
        lrhour2=request.getParameter("lrhour2");
        String part1="";
        String part2="";
        String part3="";
        String part4="";
        String part5="";
        String part6="";
        if(interval1!="")
        {
            part1=" and duration>="+interval1;
        }
        if(interval2!=""){
            part2=" and duration<="+interval2;
        }
        if(stadistance1!="")
        {
            part3=" and dis>="+stadistance1;
        }
        if(stadistance2!="")
        {
            part4=" and dis<="+stadistance2;
        }
        if(lrhour1!="")
        {
            part5=" and leasehour>="+lrhour1;
        }
        if(lrhour2!="")
        {
            part6=" and returnhour<="+lrhour2;
        }
        String sql1="select max(count(*)),min(count(*)) from B_LEASEINFOHIS_DISANGLE  where LEASEDAY>='"+mintime+"' and LEASEDAY<='"+maxtime
                +"'"+part1+part2+part3+part4+part5+part6+" group by LEASESTATION,RETURNSTATION";
        Statement statement;
        ResultSet resultSet1;
        Mycon mycon=new Mycon();
        Connection con=mycon.getConnecton();
        try{

            statement=con.createStatement();
            resultSet1=statement.executeQuery(sql1);
            while (resultSet1.next()){
                maxcount=resultSet1.getString(1);
                mincount=resultSet1.getString(2);
            }
            statement.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        PrintWriter out=response.getWriter();
        System.out.println("已查询到范围");
        out.print("{\"maxcount\":"+maxcount+",\"mincount\":"+mincount+"}");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
