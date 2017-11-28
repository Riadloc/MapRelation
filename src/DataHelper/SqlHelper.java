package DataHelper;

import DB.DBManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SqlHelper {
    static Connection con=null;
    public static JSONArray getPosition(){
        PreparedStatement ps=null;
        ResultSet rs=null;
        JSONArray stations=new JSONArray();
        try {
            con= DBManager.getConnection();
            String sql="select * from B_STATIONINFO_BRIEF ORDER BY stationid";
            ps=con.prepareStatement(sql);
            rs=ps.executeQuery();
            while(rs.next()){
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("stationid",rs.getString("stationid"));
                jsonObject.put("stationname",rs.getString("stationname"));
                jsonObject.put("lng",rs.getString("BAIDU_X"));
                jsonObject.put("lat",rs.getString("BAIDU_Y"));
                stations.put(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(con,ps,rs);
        }
        return stations;
    }
    public static TreeMap<String, String[]> getStations(){
        Connection con= DBManager.getConnection();
        PreparedStatement ps=null;
        ResultSet rs=null;
        TreeMap<String, String[]> stations = new TreeMap();
        try {
            String sql="select * from B_STATIONINFO_BRIEF ORDER BY stationid";
            ps=con.prepareStatement(sql);
            rs=ps.executeQuery();
            while(rs.next()){
                String key = rs.getString("stationid");
                String[] temp = new String[]{rs.getString("BAIDU_X"), rs.getString("BAIDU_Y")};
                stations.put(key, temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(con,ps,rs);
        }
        return stations;
    }
    public static JSONArray getRelations(String day) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        JSONArray relations=new JSONArray();
        String date = day.replace("-","");
        try {
            con= DBManager.getConnection();
            String sql="select LEASESTATION,RETURNSTATION,count(bikenum) as nums from B_LEASEINFOHIS_SUM_PART partition(D"+date+") WHERE LEASESTATION != RETURNSTATION group by LEASESTATION,RETURNSTATION order by LEASESTATION";
            ps=con.prepareStatement(sql);
            rs=ps.executeQuery();
            while(rs.next()){
                JSONObject obj=new JSONObject();
                obj.put("lease",rs.getString("leasestation"));
                obj.put("return",rs.getString("returnstation"));
                obj.put("nums",rs.getString("nums"));
                relations.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(con,ps,rs);
        }
        return relations;
    }
    public static JSONArray getRelations(String day,String hour) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        JSONArray relations=new JSONArray();
        String date = day.replace("-","");
        try {
            con= DBManager.getConnection();
            String sql="select LEASESTATION,RETURNSTATION,count(bikenum) as nums from B_LEASEINFOHIS_SUM_PART partition(D"+date+") WHERE LEASETIME =? group by LEASESTATION,RETURNSTATION order by LEASESTATION";
            ps=con.prepareStatement(sql);
            ps.setString(1,hour);
            rs=ps.executeQuery();
            while(rs.next()){
                JSONObject obj=new JSONObject();
                obj.put("lease",rs.getString("leasestation"));
                obj.put("return",rs.getString("returnstation"));
                obj.put("nums",rs.getString("nums"));
                relations.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(con,ps,rs);
        }
        return relations;
    }
    public static JSONArray getRelationsBlock(String from,String to) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        JSONArray relations=new JSONArray();
        try {
            con= DBManager.getConnection();
            String sql="select LEASESTATION,RETURNSTATION,count(bikenum) as nums from B_LEASEINFOHIS_SUM_PART WHERE leasedate BETWEEN ? AND ? group by LEASESTATION,RETURNSTATION order by LEASESTATION,RETURNSTATION";
            ps=con.prepareStatement(sql);
            ps.setString(1,from);
            ps.setString(2,to);
            rs=ps.executeQuery();
            while(rs.next()){
                JSONObject obj=new JSONObject();
                obj.put("lease",rs.getString("leasestation"));
                obj.put("return",rs.getString("returnstation"));
                obj.put("nums",rs.getString("nums"));
                relations.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(con,ps,rs);
        }
        return relations;
    }

    public static String getRange(String val, ArrayList<String> list) {
        String range = "";
        for (int i = 0; i < list.size(); i++) {
            range += val + " in " + "(" + list.get(i) + ")" + " or ";
        }
        range = range.substring(0, range.length()-4);
        return range;
    }

    public static JSONArray getRelationsBlock(String from,String to,String[] points) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        JSONArray relations=new JSONArray();
        ArrayList<String> list = new ArrayList<>();
        String bfrom = "2014-" + from.substring(0,2) +"-"+ from.substring(2,4);
        String bto = "2014-" + to.substring(0,2) +"-"+ to.substring(2,4);
        int len = points.length / 800 + 1;
        for (int i = 0; i < len; i++) {
            String range = "";
            for (int j = i * 800; j < Math.min(800*(i+1), points.length); j++) {
                range += "'"+points[j]+"',";
            }
            range = range.substring(0, range.length()-1);
            list.add(range);
        }
        System.out.println(getRange("LEASESTATION", list));
        try {
            con= DBManager.getConnection();
            String sql="select LEASESTATION,RETURNSTATION,count(bikenum) as nums from B_LEASEINFOHIS_SUM_PART " +
                    "WHERE (leasedate BETWEEN ? AND ?) AND ("+getRange("LEASESTATION", list)+") AND ("+getRange("RETURNSTATION", list)+
                    ") group by LEASESTATION,RETURNSTATION order by LEASESTATION";
            ps=con.prepareStatement(sql);
            ps.setString(1,bfrom);
            ps.setString(2,bto);
            rs=ps.executeQuery();
            while(rs.next()){
                JSONObject obj=new JSONObject();
                obj.put("lease",rs.getString("leasestation"));
                obj.put("return",rs.getString("returnstation"));
                obj.put("nums",rs.getString("nums"));
                relations.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(con,ps,rs);
        }
        return relations;
    }

    public static ArrayList<String> getStationsArray(String day) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        ArrayList<String> stations = new ArrayList<>();
        try {
            con= DBManager.getConnection();
            String sql="select * from (select leasestation,leasedate from B_LEASEINFOHIS_SUM_PART union select RETURNSTATION,leasedate from B_LEASEINFOHIS_SUM_PART) where leasedate=?";
            ps=con.prepareStatement(sql);
            ps.setString(1,day);
            rs=ps.executeQuery();
            while(rs.next()){
                stations.add(rs.getString("leasestation"));
//                stations += rs.getString("leasestation");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(con,ps,rs);
        }
        return stations;
    }

    public static ArrayList<String> getStationsArray(String day,String hour) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        ArrayList<String> stations = new ArrayList<>();
        try {
            con= DBManager.getConnection();
            String sql="select * from (select leasestation,leasedate,leasetime from B_LEASEINFOHIS_SUM_PART union select RETURNSTATION,leasedate,leasetime from B_LEASEINFOHIS_SUM_PART) where leasedate=? AND leasetime =?";
            ps=con.prepareStatement(sql);
            ps.setString(1,day);
            ps.setString(2,hour);
            rs=ps.executeQuery();
            while(rs.next()){
                stations.add(rs.getString("leasestation"));
//                stations += rs.getString("leasestation");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(con,ps,rs);
        }
        return stations;
    }

    public static ArrayList<String> getStationsArrayBlock(String from,String to) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        ArrayList<String> stations = new ArrayList<>();
        try {
            con= DBManager.getConnection();
            String sql="select DISTINCT leasestation from (select leasestation,leasedate,leasetime from B_LEASEINFOHIS_SUM_PART union select RETURNSTATION,leasedate,leasetime from B_LEASEINFOHIS_SUM_PART) where leasedate BETWEEN ? AND ?";
            ps=con.prepareStatement(sql);
            ps.setString(1,from);
            ps.setString(2,to);
            rs=ps.executeQuery();
            while(rs.next()){
                stations.add(rs.getString("leasestation"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(con,ps,rs);
        }
        return stations;
    }

    public static JSONArray getScatRels(TreeMap map,String date){
        Iterator it = map.keySet().iterator();
        JSONArray array =new JSONArray();
        JSONObject object = new JSONObject();
        con = DBManager.getConnection();
        while(it.hasNext()) {
            Object key = it.next();
            JSONArray jsonArray = getScatterRels(date, (ArrayList) map.get(key));
            try {
                object.put(key.toString(),jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        array.put(object);
        DBManager.closeCon(con);
        return array;
    }
    public static JSONArray getScatRelsNew(TreeMap map,String date){
        Iterator it = map.keySet().iterator();
        Object[] keys = new Object[map.keySet().size()];
        for(int i=0;i<keys.length;i++) {
            keys[i] = it.next();
        }
        JSONArray array =new JSONArray();
        JSONObject object = new JSONObject();
        con = DBManager.getConnection();
        for (int i=0;i<keys.length;i++) {
            for(int j=0;j<keys.length;j++) {
                if(i != j) {
                    JSONArray jsonArray = getScatterRels(date, (ArrayList) map.get(keys[i]), (ArrayList) map.get(keys[j]));
                    try {
                        object.put(keys[i].toString()+"-"+keys[j].toString(),jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        array.put(object);
        DBManager.closeCon(con);
        return array;
    }
    
    public static JSONArray getScatterRels(String day,ArrayList vertices) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        String[] date = getDate(day);
        String from = date[0],to = date[1];
        String station = "";
        JSONArray relations = new JSONArray();
        for (Object str : vertices) {
            station += "'"+str.toString()+"',";
        }
        station = station.substring(0,station.length()-1);
        try {
            String sql="select leasestation, returnstation, COUNT(bikenum) as nums from B_LEASEINFOHIS_SUM_PART  where (leasedate BETWEEN ? AND ?) AND leasestation in ("+station+") and returnstation in ("+station+") group by leasestation,returnstation";
            ps=con.prepareStatement(sql);
            ps.setString(1,from);
            ps.setString(2,to);
            rs=ps.executeQuery();
            while(rs.next()){
                String lease = rs.getString("leasestation");
                String retur = rs.getString("returnstation");
                if (rs.getString("nums").equals("0") || lease.equals(retur)) {
                    continue;
                }
                JSONObject object = new JSONObject();
                object.put("lease",lease);
                object.put("return",retur);
                object.put("nums",rs.getString("nums"));
                relations.put(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.closeAll(ps,rs);
        }
        return relations;
    }
    public static JSONArray getScatterRels(String day,ArrayList fromArr, ArrayList toArr) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        String[] date = getDate(day);
        String from = date[0],to = date[1];
        String stationFr = "",stationTo = "";
        JSONArray relations = new JSONArray();
        for (Object str : fromArr) {
            stationFr += "'"+str.toString()+"',";
        }
        for (Object str : toArr) {
            stationTo += "'"+str.toString()+"',";
        }
        stationFr = stationFr.substring(0,stationFr.length()-1);
        stationTo = stationTo.substring(0,stationTo.length()-1);
        try {
            String sql="select leasestation, returnstation, COUNT(bikenum) as nums from B_LEASEINFOHIS_SUM_PART  where (leasedate BETWEEN ? AND ?) AND leasestation in ("+stationFr+") and returnstation in ("+stationTo+") group by leasestation,returnstation";
            ps=con.prepareStatement(sql);
            ps.setString(1,from);
            ps.setString(2,to);
            rs=ps.executeQuery();
            while(rs.next()){
                String lease = rs.getString("leasestation");
                String retur = rs.getString("returnstation");
                if (rs.getString("nums").equals("0") || lease.equals(retur)) {
                    continue;
                }
                JSONObject object = new JSONObject();
                object.put("lease",lease);
                object.put("return",retur);
                object.put("nums",rs.getString("nums"));
                relations.put(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.closeAll(ps,rs);
        }
        return relations;
    }

    public static JSONArray getRelations(TreeMap<String,ArrayList<String>> map, String day) {
        int len = map.size();
        ArrayList<String> lease,retn;
        JSONArray relations = new JSONArray();
        JSONObject relation;
        con= DBManager.getConnection();
        for (Iterator<String> it1 = map.keySet().iterator(); it1.hasNext();) {
            String i = it1.next();
            for (Iterator<String> it2 = map.keySet().iterator(); it2.hasNext();) {
                String j = it2.next();
                if (!i.equals(j)) {
                    lease = map.get(i);
                    retn = map.get(j);
                    relation = SqlHelper.getColRels(day,i,j,lease,retn);
                    relations.put(relation);
                }
            }
        }
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return relations;
    }

    public static String[] getDate(String day) {
        String from,to;
        if (day.length() == 9){
            String[] date = day.split("_");
            from = "2014-" + date[0].substring(0,2)+"-"+date[0].substring(2,4);
            to = "2014-" + date[1].substring(0,2)+"-"+date[1].substring(2,4);
        } else {
            from = "2014-"+day.substring(0,2)+"-"+day.substring(2,4);
            to = "2014-"+day.substring(0,2)+"-"+day.substring(2,4);
        }
        return new String[]{from,to};
    }

    public static JSONObject getColRels(String day, String lease_id, String retn_id, ArrayList lease,ArrayList retn) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        String[] date = getDate(day);
        String from = date[0],to = date[1];
        String returnStr="", leaseStr="";
        for (int i = 0; i < lease.size(); i++) {
            leaseStr += "'"+lease.get(i).toString()+"',";
        }
        for (int i = 0; i < retn.size(); i++) {
            returnStr += "'"+retn.get(i).toString()+"',";
        }
        leaseStr = leaseStr.substring(0,leaseStr.length()-1);
        returnStr = returnStr.substring(0,returnStr.length()-1);
        JSONObject relations = new JSONObject();
        try {
            String sql="select COUNT(bikenum) as nums from B_LEASEINFOHIS_SUM_PART where (leasedate BETWEEN ? AND ?) and leasestation in ("+leaseStr+") and returnstation in ("+returnStr+")";
            ps=con.prepareStatement(sql);
            ps.setString(1,from);
            ps.setString(2,to);
            rs=ps.executeQuery();
            while(rs.next()){
                relations.put("leaseid",lease_id);
                relations.put("returnid",retn_id);
                relations.put("nums",rs.getString("nums"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.closeAll(ps,rs);
        }
        return relations;
    }
    public static JSONObject getColRels(String day, String hour, String lease_id, String retn_id, ArrayList lease,ArrayList retn) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        String[] date = getDate(day);
        String from=date[0],to=date[1];
        String leaseStr ="",returnStr="";
        for (int i = 0; i < lease.size(); i++) {
            leaseStr += "'"+lease.get(i).toString()+"',";
        }
        for (int i = 0; i < retn.size(); i++) {
            returnStr += "'"+retn.get(i).toString()+"',";
        }
        leaseStr = leaseStr.substring(0,leaseStr.length()-1);
        returnStr = returnStr.substring(0,returnStr.length()-1);
        JSONObject relations = new JSONObject();
        con= DBManager.getConnection();
        try {
            String sql="select COUNT(bikenum) as nums from B_LEASEINFOHIS_SUM_PART where (leasedate BETWEEN ? AND ?)  AND leasestation in ("+leaseStr+") and returnstation in ("+returnStr+") and leasetime = ?";
//            System.out.println(sql);
            ps=con.prepareStatement(sql);
            ps.setString(1,from);
            ps.setString(2,to);
            ps.setString(1,hour);
            rs=ps.executeQuery();
            while(rs.next()){
                relations.put("leaseid",lease_id);
                relations.put("returnid",retn_id);
                relations.put("nums",rs.getString("nums"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(con,ps,rs);
        }
        return relations;
    }

    public static JSONArray getCurveInfo(String lease, String retur, String time) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        JSONArray curveInfos = new JSONArray();
        String[] date = getDate(time);
        String from = date[0], to = date[1];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date _from = null,_to = null;
        try {
            _from = sdf.parse(from);
            _to = sdf.parse(to);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int len = (int)(_to.getTime()-_from.getTime())/(60*60*1000*24);
        Calendar _time = Calendar.getInstance();
        _time.setTime(_from);
        String _date = "";
        con= DBManager.getConnection();
        do {
            _date = sdf.format(_time.getTime());
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            for (int j = 6; j <= 21; j++) {
                try {
                    String sql= "SELECT COUNT(bikenum) as nums FROM B_LEASEINFOHIS_SUM_PART partition(D"+_date.replace("-","")+") WHERE leasestation in ? AND returnstation = ? AND leasetime = ?";
                    ps=con.prepareStatement(sql);
                    ps.setString(1,lease);
                    ps.setString(2,retur);
                    ps.setString(3, String.format("%02d",j));
                    rs=ps.executeQuery();
                    while(rs.next()){
                        array.put(rs.getString("nums"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                object.put(_date, array);
                curveInfos.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            _time.add(Calendar.DAY_OF_YEAR, 1);
        } while ((len--) > 0);
        DBManager.close(con,ps,rs);
        return curveInfos;
    }
}
