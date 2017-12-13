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
 * Created by 小型 on 2017/10/31.
 */
@WebServlet("/ServletShowTopTen")
public class ServletShowTopTen extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        String stationname=null;
        String time1=null;
        String time2=null;
        stationname=request.getParameter("stationname");
        time1=request.getParameter("time1");
        time2=request.getParameter("time2");
        String sql="select * from(\n" +
                "select  x.baidu_x as xx,x.baidu_y as xy,x.stationname as xname,y.baidu_x as yx,y.baidu_y as yy,y.stationname as yname,leasestation,returnstation,count(*),0\n" +
                "from B_LEASEINFOHIS_DISANGLE,B_STATIONINFO_BRIEF x,B_STATIONINFO_BRIEF y \n" +
                "where x.stationid=B_LEASEINFOHIS_DISANGLE.LEASESTATION\n" +
                "and y.STATIONID=B_LEASEINFOHIS_DISANGLE.RETURNSTATION \n" +
                "and leaseday<='"+time2+"' and leaseday>='"+time1+"'\n" +
                "and (x.stationname='"+stationname+"' or y.stationname='"+stationname+"')\n" +
                " and B_LEASEINFOHIS_DISANGLE.LEASESTATION!=B_LEASEINFOHIS_DISANGLE.RETURNSTATION "+
                "group by x.baidu_x,x.baidu_y,x.stationname,y.baidu_x,y.baidu_y,y.stationname,leasestation,returnstation \n" +
                "order by count(*) DESC ) where rownum<=10";
        System.out.print(sql);
        Statement statement;
        ResultSet resultSet1;
        Mycon mycon=new Mycon();
        List<StationBean> list=new ArrayList();
        StationBean stationBean;
        Connection con=mycon.getConnecton();
        int max=0;
        try{

            statement=con.createStatement();
            resultSet1=statement.executeQuery(sql);
            while (resultSet1.next()){
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
                if(resultSet1.getInt(9)>max){
                    max=resultSet1.getInt(9);
                }
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
        out.print("{\"max\":"+max+",\"json\":"+gson.toJson(list)+"}");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
