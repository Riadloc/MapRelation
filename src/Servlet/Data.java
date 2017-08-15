package Servlet;

import DataHelper.ReadLines;
import DataHelper.SqlHelper;
import org.json.JSONArray;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Alien on 2016/9/27.
 */
@WebServlet("/data")
public class Data extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        JSONArray stations = SqlHelper.getPosition();
        response.getWriter().write(stations.toString());
//        System.out.println(stations);
    }
}
