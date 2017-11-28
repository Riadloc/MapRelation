package Servlet;

import DataHelper.ReadLines;
import DataHelper.SqlHelper;
import net.sf.json.JSON;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by Alien on 2017/11/20.
 */
@WebServlet("/louvain")
public class Louvain extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String day = request.getParameter("day");
        String fileName = request.getParameter("fileName");
        TreeMap map = ReadLines.getCollectionFromFile(fileName);
        System.out.println(map);
        JSON markers = net.sf.json.JSONArray.fromObject(map);
        long a = System.currentTimeMillis();
        JSONArray relations = SqlHelper.getRelations(map,day);
        long b = System.currentTimeMillis();
        System.out.println("运行时间： " + (float)(b-a)/1000.0);
        String markerss = markers.toString();
        String relationss = relations.toString();
        response.getWriter().write(markerss+"@"+relationss);
    }
}
