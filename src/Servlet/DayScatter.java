package Servlet;

import DataHelper.ReadLines;
import DataHelper.SqlHelper;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by Alien on 2017/4/17.
 */
@WebServlet("/dayscatter")
public class DayScatter extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String day = request.getParameter("day");
        TreeMap map = ReadLines.getCollection(day);
        JSON markers = net.sf.json.JSONArray.fromObject(map);
        System.out.println(markers);
        JSONArray relations = SqlHelper.getScatRels(map,day);
        response.getWriter().write(markers.toString()+"@"+relations.toString());
    }
}
