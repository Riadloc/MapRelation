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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 小型 on 2017/10/24.
 */
@WebServlet("/GetHeatServletFor11")
public class GetHeatServletFor11 extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        String time1;
        String time2;
        String lrhour3;
        String lrhour4;
        String stationId;
        int rollnum;
        int anglenum;
        stationId=request.getParameter("stationId");
        time1=request.getParameter("time1");
        time2=request.getParameter("time2");
        rollnum=Integer.valueOf(request.getParameter("rollnum"));
        anglenum=Integer.valueOf(request.getParameter("anglenum"));
        lrhour3=request.getParameter("lrhour3");
        lrhour4=request.getParameter("lrhour4");
        String part5="";
        String part6="";
        if(lrhour3!="")
        {
            part5=" and leasehour>="+lrhour3;
        }
        if(lrhour4!="")
        {
            part6=" and returnhour<="+lrhour4;
        }
        String sql1="select count(*),angle,distance  " +
                " from B_LEASEINFOHIS_BRIEF,S_STATIONDISANGLE_11KM " +
                " where LEASEDAY<='"+time2+"' "+
                " and LEASEDAY>='"+time1+"' "+
                "and returnstation!=leasestation"+
                " and S_STATIONDISANGLE_11KM.STATION1=LEASESTATION " +
                " and station2=RETURNSTATION " +part5+part6+
                " and LEASESTATION=(select stationid from B_STATIONINFO_BRIEF where stationname='"+stationId+"')"+
                " GROUP BY angle,distance ";
        Statement statement;
        ResultSet resultSet1;
        Mycon mycon=new Mycon();
        List<Integer> list =new ArrayList();
        for(int i=1;i<=rollnum*anglenum;i++){
            list.add(0);
        }
        Connection con=mycon.getConnecton();
        int max=0;
        try{

            statement=con.createStatement();
            int i;//angle
            int j;//roll

            resultSet1=statement.executeQuery(sql1);
            while (resultSet1.next()){
                i=(int)Math.floor(resultSet1.getDouble(2)/(360/anglenum));
                j=(int)Math.floor(resultSet1.getDouble(3)/11*rollnum);
                if(j==11){
                    j=10;
                }
                System.out.println("i:"+i+"ang:"+resultSet1.getDouble(2)+"j:"+j+"dis:"+resultSet1.getDouble(3));
                list.set(anglenum*j+i,list.get(anglenum*j+i)+resultSet1.getInt(1));
            }
            statement.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        String json="";
        String json1="";
        for (int a=0;a<rollnum;a++){
            for(int b=0;b<anglenum;b++){
                json=json+"["+1+","+list.get(anglenum*a+b)+"],";
            }
            json=json.substring(0,json.length()-1);
            //System.out.println(json);
            json1=json1+"["+json+"],";
            json="";
            //System.out.println(json1);
        }
        json1=json1.substring(0,json1.length()-1);
        PrintWriter out=response.getWriter();
        System.out.println("已获取热力图数据");
        out.print("{\"max\":"+ Collections.max(list)+",\"json\":["+json1+"]}");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
