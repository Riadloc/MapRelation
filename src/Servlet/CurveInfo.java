package Servlet;

import DataHelper.SqlHelper;
import org.json.JSONArray;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Alien on 2017/6/8.
 */
@WebServlet("/curveInfo")
public class CurveInfo extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String leaseStation = request.getParameter("leaseStation");
        String returnStation = request.getParameter("returnStation");
        String time = request.getParameter("time");
        JSONArray curveInfos = SqlHelper.getCurveInfo(leaseStation, returnStation, time);
        response.getWriter().write(curveInfos.toString());
    }
}
