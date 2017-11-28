package Servlet;

import DataHelper.ReadLines;
import net.sf.json.JSON;
import org.json.JSONArray;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.TreeMap;

/**
 * Created by Alien on 2017/4/17.
 */
@WebServlet("/hourcore")
public class HourCore extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String fileName = request.getParameter("fileName");
        TreeMap map = ReadLines.getCollection(fileName);
        System.out.println(map);
        JSON markers = net.sf.json.JSONArray.fromObject(map);
        JSONArray relations = ReadLines.getRelations(map,day,hour);
        System.out.println(relations);
        String markerss = markers.toString().replace("\\\"","");
        String relationss = relations.toString();
        response.getWriter().write(markerss+"@"+relationss);
    }
}
