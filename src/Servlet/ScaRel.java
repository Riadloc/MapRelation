package Servlet;

import DataHelper.ReadLines;
import DataHelper.SqlHelper;
import net.sf.json.JSON;
import org.json.JSONArray;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by Alien on 2017/5/25.
 */
@WebServlet("/scaRel")
public class ScaRel extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String day = request.getParameter("day");
        String[] idArray = request.getParameter("idArray").split(",");
        String fileName = request.getParameter("fileName");
        String comm_type = request.getParameter("comm_type");
        HashMap<String, TreeMap<String, ArrayList<String>>> vMap = ReadLines.getCollection(fileName, comm_type);
        TreeMap map = vMap.get("scatter");
        TreeMap newMap = new TreeMap();
        for (int i=0;i<idArray.length;i++) {
            newMap.put(idArray[i],map.get(idArray[i]));
        }
        System.out.println(newMap);
        JSON markers = net.sf.json.JSONArray.fromObject(newMap);
        long a = System.currentTimeMillis();
        JSONArray relations = SqlHelper.getScatRelsNew(newMap,day);
        long b = System.currentTimeMillis();
        System.out.println("运行时间： " + (float)(b-a)/1000.0);
        String markerss = markers.toString();
        String relationss = relations.toString();
        response.getWriter().write(markerss+"@"+relationss);
    }
}
