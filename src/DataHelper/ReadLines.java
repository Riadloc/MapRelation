package DataHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Alien on 2017/4/12.
 */
public class ReadLines {
    static String path = "C:\\Users\\Alien\\Documents\\relations\\";
    public static void generate(String from,String to) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = null,dt2 = null;
        try {
            dt = sdf.parse(from);
            dt2 = sdf.parse(to);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int len = (int)(dt2.getTime()-dt.getTime())/(60*60*1000*24);
        System.out.println(len);
        Calendar time = Calendar.getInstance();
        time.setTime(dt);
        String name,date;
        ArrayList<String> stations;
        JSONArray array;
        for (int i = 0; i <= len; i++) {
            date = sdf.format(time.getTime());
            stations = SqlHelper.getStationsArray(date);
            array = SqlHelper.getRelations(date);
            date = date.replaceAll("2014|-","");
            System.out.println(stations);
            name = "byday_"+date+".net";
            File file;
            try {
                file = new File(path+name);
                if(!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fos = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fos);
                bw.write("*Vertices "+stations.size()+"\r\n");
                for (int j = 0; j < stations.size(); j++) {
                    bw.write((j+1)+" \""+stations.get(j)+"\"");
                    bw.newLine();
                }
                bw.write("*Arcs "+array.length()+"\r\n");
                for (int k = 0; k < array.length(); k++) {
                    int lease = stations.indexOf(array.getJSONObject(k).getString("lease"))+1;
                    int ret = stations.indexOf(array.getJSONObject(k).getString("return"))+1;
                    String nums = array.getJSONObject(k).getString("nums");
                    bw.write(lease+" "+ret+" "+nums);
                    bw.newLine();
                }
                bw.flush();
                bw.close();
                time.add(Calendar.DAY_OF_YEAR, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void undirGenerate(String from,String to) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = null,dt2 = null;
        try {
            dt = sdf.parse(from);
            dt2 = sdf.parse(to);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int len = (int)(dt2.getTime()-dt.getTime())/(60*60*1000*24);
        System.out.println(len);
        Calendar time = Calendar.getInstance();
        time.setTime(dt);
        String name,date;
        ArrayList<String> stations, lines = new ArrayList<>(), numbers = new ArrayList<>();
        JSONArray array;
        for (int i = 0; i <= len; i++) {
            date = sdf.format(time.getTime());
            stations = SqlHelper.getStationsArray(date);
            array = SqlHelper.getRelations(date);
            date = date.replaceAll("2014|-","");
            System.out.println(stations);
            name = "undir_byday_"+date+".net";
            File file;
            try {
                file = new File(path+name);
                if(!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fos = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fos);
                for (int k = 0; k < array.length(); k++) {
                    int lease = stations.indexOf(array.getJSONObject(k).getString("lease"))+1;
                    int ret = stations.indexOf(array.getJSONObject(k).getString("return"))+1;
                    String nums = array.getJSONObject(k).getString("nums");
                    int idx = lines.indexOf(ret+" "+lease);
                    if(idx>-1) {
                        numbers.set(idx,(Integer.parseInt(numbers.get(idx)) + Integer.parseInt(nums))+"");
                    } else {
                        lines.add(lease+" "+ret);
                        numbers.add(nums);
                    }
                }
                bw.write("*Vertices "+stations.size()+"\r\n");
                for (int j = 0; j < stations.size(); j++) {
                    bw.write((j+1)+" \""+stations.get(j)+"\"");
                    bw.newLine();
                }
                bw.write("*Arcs "+numbers.size()+"\r\n");
                for (int m = 0;m < numbers.size();m++) {
                    bw.write(lines.get(m)+" "+numbers.get(m));
                    bw.newLine();
                }
                bw.flush();
                bw.close();
                time.add(Calendar.DAY_OF_YEAR, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void generateBlock(String from,String to) {
        String name;
        String date = from.replaceAll("2014|-","") + "_" + to.replaceAll("2014|-","");
        ArrayList<String> stations;
        JSONArray array;
        stations = SqlHelper.getStationsArrayBlock(from,to);
        array = SqlHelper.getRelationsBlock(from, to);
        System.out.println(stations);
        name = "byday_"+date+".net";
        File file;
        try {
            file = new File(path+name);
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fos = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fos);
            bw.write("*Vertices "+stations.size()+"\r\n");
            for (int i = 0; i < stations.size(); i++) {
                bw.write((i+1)+" \""+stations.get(i)+"\"");
                bw.newLine();
            }
            bw.write("*Arcs "+array.length()+"\r\n");
            for (int j = 0; j < array.length(); j++) {
                int lease = stations.indexOf(array.getJSONObject(j).getString("lease"))+1;
                int ret = stations.indexOf(array.getJSONObject(j).getString("return"))+1;
                String nums = array.getJSONObject(j).getString("nums");
                bw.write(lease+" "+ret+" "+nums);
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void undirGenerateBlock(String from,String to) {
        String name;
        String date = from.replaceAll("2014|-","") + "_" + to.replaceAll("2014|-","");
        ArrayList<String> stations,numbers = new ArrayList<>(),lines = new ArrayList<>();
        JSONArray array;
        stations = SqlHelper.getStationsArrayBlock(from,to);
        array = SqlHelper.getRelationsBlock(from, to);
        System.out.println(stations);
        name = "undir_byday_"+date+".net";
        File file;
        try {
            file = new File(path+name);
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fos = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fos);
            for (int j = 0; j < array.length(); j++) {
                int lease = stations.indexOf(array.getJSONObject(j).getString("lease"))+1;
                int ret = stations.indexOf(array.getJSONObject(j).getString("return"))+1;
                String nums = array.getJSONObject(j).getString("nums");
                int idx = lines.indexOf(ret+" "+lease);
                if(idx>-1) {
                    numbers.set(idx,(Integer.parseInt(numbers.get(idx)) + Integer.parseInt(nums))+"");
                } else {
                    lines.add(lease+" "+ret);
                    numbers.add(nums);
                }
            }
            bw.write("*Vertices "+stations.size()+"\r\n");
            for (int i = 0; i < stations.size(); i++) {
                bw.write((i+1)+" \""+stations.get(i)+"\"");
                bw.newLine();
            }
            bw.write("*Arcs "+numbers.size()+"\r\n");
            for (int m = 0;m < numbers.size();m++) {
                bw.write(lines.get(m)+" "+numbers.get(m));
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void generateBlock(String from,String to,String num,String[] points) {
        String name;
        String date = "";
        if (from.equals(to)) {
            date = from;
        } else {
            date = from+"_"+to;
        }
        List<String> stations;
        JSONArray array;
        stations = Arrays.asList(points);
        array = SqlHelper.getRelationsBlock(from, to, points);
        name = "byday_"+date+"_cluster"+num+".net";
        File file;
        try {
            file = new File(path+name);
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fos = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fos);
            bw.write("*Vertices "+stations.size()+"\r\n");
            for (int i = 0; i < stations.size(); i++) {
                bw.write((i+1)+" \""+stations.get(i)+"\"");
                bw.newLine();
            }
            bw.write("*Arcs "+array.length()+"\r\n");
            for (int j = 0; j < array.length(); j++) {
                int lease = stations.indexOf(array.getJSONObject(j).getString("lease"))+1;
                int ret = stations.indexOf(array.getJSONObject(j).getString("return"))+1;
                String nums = array.getJSONObject(j).getString("nums");
                bw.write(lease+" "+ret+" "+nums);
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void generate(String day) {
        String name;
        ArrayList<String> stations;
        JSONArray array;
        for (int i = 6; i <= 22; i++) {
            stations = SqlHelper.getStationsArray(day,String.format("%02d",i));
            array = SqlHelper.getRelations(day,String.format("%02d",i));
            System.out.println(stations);
            name = "byhour_"+i+".net";
            File file;
            try {
                file = new File(path+name);
                if(!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fos = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fos);
                bw.write("*Vertices\r\n");
                for (int j = 0; j < stations.size(); j++) {
                    bw.write((j+1)+" \""+stations.get(j)+"\"");
                    bw.newLine();
                }
                bw.write("*Arcs\r\n");
                for (int k = 0; k < array.length(); k++) {
                    int lease = stations.indexOf(array.getJSONObject(k).getString("lease"))+1;
                    int ret = stations.indexOf(array.getJSONObject(k).getString("return"))+1;
                    String nums = array.getJSONObject(k).getString("nums");
                    bw.write(lease+" "+ret+" "+nums);
                    bw.newLine();
                }
                bw.flush();
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<String> getVertices(String date) {
        String name_net = "byday_"+date+".net";
        String vertice;
        ArrayList<String> vertices = new ArrayList<>();
        File file_net = new File(path+name_net);
        FileReader fr;
        BufferedReader br;
        try {
            fr = new FileReader(file_net);
            br = new BufferedReader(fr);
            vertice = br.readLine();
            while((vertice = br.readLine())!=null) {
                String[] stringArray = vertice.split(" ");
                if (stringArray[0].equals("*Arcs")){
                    break;
                }
                vertice = stringArray[1].replace("\"","");
                vertices.add(vertice);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vertices;
    }
    public static TreeMap getCollection(String date) {
        String name_clu = "byday_"+date+".clu";
        String name_net = "byday_"+date+".net";
        String col;
        int len = 0;
        ArrayList<String> collection = new ArrayList<>();
        File file_clu = new File(path+name_clu);
        File file_net = new File(path+name_net);
        if (!file_clu.exists() || !file_net.exists()) {
            System.out.println("file "+name_clu+" does not exist or file "+name_net+" does not exist!");
            return null;
        }
        FileReader fr;
        BufferedReader br;
        try {
            fr = new FileReader(file_clu);
            br = new BufferedReader(fr);
            col = br.readLine();
            while((col = br.readLine())!=null) {
                if(col.equals("")) {
                    continue;
                }
                len = len>Integer.parseInt(col)?len:Integer.parseInt(col);
                collection.add(col);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> vertices = getVertices(date);
        TreeMap scatter = new TreeMap();
        for (int i=1;i<=len;i++) {
            ArrayList<String> array = new ArrayList<>();
            for (int j = 0; j < collection.size(); j++) {
                if (Integer.parseInt(collection.get(j)) == i) {
                    array.add(vertices.get(j));
                }
            }
            scatter.put(String.valueOf(i),array);
        }
        return scatter;
    }

    public static JSONArray getRelations(TreeMap<String,ArrayList<String>> map,String day,String hour) {
        int len = map.size();
        ArrayList<String> lease,retn;
        String lease_id,retn_id;
        JSONArray relations = new JSONArray();
        JSONObject relation;
        for (int i = 1; i <= len; i++) {
            for (int j = 1; j <= len; j++) {
                if (i != j) {
                    lease_id = String.valueOf(i);
                    retn_id = String.valueOf(j);
                    lease = map.get(String.valueOf(i));
                    retn = map.get(String.valueOf(j));
                    relation = SqlHelper.getColRels(day,hour,lease_id,retn_id,lease,retn);
                    relations.put(relation);
                }
            }
        }
        return relations;
    }

    public static void writeClusterToFile(ArrayList<TreeSet> array) {
        String name;
        File file;
        name = "kmeans_" + array.size() + ".txt";
        try {
            file = new File(path+name);
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fos = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fos);
            for (int i = 0; i < array.size(); i++) {
                TreeSet set = array.get(i);
                bw.write("No"+(i+1)+":  "+set.toString().replaceAll("[\\[\\]]", ""));
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
