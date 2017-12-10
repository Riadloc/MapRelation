package Servlet;

import DataHelper.ReadLines;
import DataHelper.SqlHelper;
import net.sf.json.JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.management.relation.Relation;
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
 * Created by Alien on 2017/4/17.
 */
@WebServlet("/community")
public class Community extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String day = request.getParameter("day");
        String fileName = request.getParameter("fileName");
        String func_type = request.getParameter("func_type");
        String comm_type = request.getParameter("comm_type");
        System.out.println(comm_type);
        HashMap<String, TreeMap<String, ArrayList<String>>> vMap;
        if (comm_type.equals("louvain")) {
            String level = request.getParameter("level");
            vMap = ReadLines.getCollection(fileName, comm_type, level);
        } else {
            vMap = ReadLines.getCollection(fileName, comm_type);
        }
        TreeMap<String, ArrayList<String>> map = vMap.get("scatter");
        System.out.println(map);
        TreeMap<String, ArrayList<String>> mapSeq = vMap.get("scatter_seq");
        JSON markers = net.sf.json.JSONArray.fromObject(map);
        long a = System.currentTimeMillis();
        if (func_type.equals("scatter")) {
            JSONArray relations = ReadLines.getScatRels(mapSeq,fileName);
            long b = System.currentTimeMillis();
            System.out.println("运行时间： " + (float)(b-a)/1000.0);
            response.getWriter().write(markers.toString()+"@"+relations.toString());
        } else if (func_type.equals("cluster")) {
            JSONArray relations = new JSONArray();
            try {
                relations = ReadLines.getRelations(mapSeq, fileName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            long b = System.currentTimeMillis();
            System.out.println("运行时间： " + (float)(b-a)/1000.0);
            response.getWriter().write(markers.toString()+"@"+relations.toString());
        } else {
            String[] idArray = request.getParameter("idArray").split(",");
            TreeMap newMap = new TreeMap();
            for (int i=0;i<idArray.length;i++) {
                newMap.put(idArray[i],map.get(idArray[i]));
            }
            System.out.println(newMap);
            JSON markerss = net.sf.json.JSONArray.fromObject(newMap);
            JSONArray relations = SqlHelper.getScatRelsNew(newMap,day);
            long b = System.currentTimeMillis();
            System.out.println("运行时间： " + (float)(b-a)/1000.0);
            response.getWriter().write(markerss.toString()+"@"+relations.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
