package Servlet;

import DataHelper.ReadLines;
import net.sf.json.JSON;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Alien on 2017/4/17.
 */
@WebServlet("/hourscatter")
public class HourScatter extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String hour = request.getParameter("hour");
        JSON array = net.sf.json.JSONArray.fromObject(ReadLines.getCollection(hour));
        System.out.println(array);
        String jsonString = array.toString().replace("\\\"","");
        response.getWriter().write(jsonString);
    }
}
