package serve;

import bean.StationBean;
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
 * Created by 小型 on 2017/7/7.
 */
@WebServlet("/SearchLineServlet")
public class SearchLineServlet extends HttpServlet {
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
        String part1="";
        String part2="";
        String part3="";
        String part4="";
        String part5="";
        String part6="";
        String part7="";
        String part8="";
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
        if(mincount!="")
        {
            part7=" count(*)>="+mincount;
        }
        if(maxcount!="")
        {
            part8=" count(*)<="+maxcount;
        }
        //System.out.print("("+mintime+" "+maxtime+" "+interval1+" "+interval2+" "+stadistance1+" "+stadistance2+" "+lrhour1+" "+lrhour2);
        //String sql1="select  x.baidu_x,x.baidu_y,x.stationname,y.baidu_x,y.baidu_y,y.stationname,count(*) from B_LEASEINFOHIS_DISANGLE,B_STATIONINFO_BRIEF x,B_STATIONINFO_BRIEF y where x.STATIONID=B_LEASEINFOHIS_DISANGLE.LEASESTATION and y.STATIONID=B_LEASEINFOHIS_DISANGLE.RETURNSTATION and LEASEDAY>='"+mintime+"' and LEASEDAY<='"+maxtime+"' and DURATION<"+interval2+" and DURATION>"+interval1+" and dis>"+stadistance1+" and dis<"+stadistance2+" and LEASEHOUR>"+lrhour1+" and LEASEHOUR<"+lrhour2+" group by  x.baidu_x,x.baidu_y,x.stationname,y.baidu_x,y.baidu_y,y.stationname HAVING  count(*)<"+maxcount+" and count(*)>"+mincount;
        String sql1="select x.baidu_x,x.baidu_y,x.stationname,y.baidu_x,y.baidu_y,y.stationname,leasestation,returnstation,count(*),COUNT(DISTINCT cardno) \n" +
                "from B_LEASEINFOHIS_DISANGLE,B_STATIONINFO_BRIEF x,B_STATIONINFO_BRIEF y \n" +
                "where x.STATIONID=B_LEASEINFOHIS_DISANGLE.LEASESTATION and y.STATIONID=B_LEASEINFOHIS_DISANGLE.RETURNSTATION and leaseday<='"+maxtime+"' and leaseday>='"+mintime+"'\n" +
                part1+part2+part3+part4+part5+part6+
                "group by x.baidu_x,x.baidu_y,x.stationname,y.baidu_x,y.baidu_y,y.stationname,leasestation,returnstation \n" +
                "HAVING  "+part7+" and "+part8;
        System.out.print(sql1);
        Statement statement;
        ResultSet resultSet1;
        Mycon mycon=new Mycon();
        Connection con=mycon.getConnecton();
        List<StationBean> list=new ArrayList();
        StationBean stationBean;
        try{

            statement=con.createStatement();
            resultSet1=statement.executeQuery(sql1);
            while(resultSet1.next()){
                stationBean=new StationBean();
               stationBean.setLeasestationx(resultSet1.getString(1));
               stationBean.setLeasestationy(resultSet1.getString(2));
               stationBean.setLeasestationname(resultSet1.getString(3));
               stationBean.setReturnstationx(resultSet1.getString(4));
               stationBean.setReturnstationy(resultSet1.getString(5));
               stationBean.setReturnstationname(resultSet1.getString(6));
               stationBean.setLeasestation(resultSet1.getString(7));
               stationBean.setReturnstation(resultSet1.getString(8));
               stationBean.setCount(resultSet1.getInt(9));
               stationBean.setUsercount(resultSet1.getInt(10));
               list.add(stationBean);
            }
            statement.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        Gson gson=new Gson();
        PrintWriter out=response.getWriter();
        System.out.println("查询借还数据");
        out.print(gson.toJson(list));

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}

