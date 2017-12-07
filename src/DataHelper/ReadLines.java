package DataHelper;

import com.csvreader.CsvReader;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Alien on 2017/4/12.
 */
public class ReadLines {
    static String path = "E:\\ColdAir\\infomap\\";
    public static void generate(String from, String to, String city) throws JSONException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = null,dt2 = null;
        try {
            dt = sdf.parse(from);
            dt2 = sdf.parse(to);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int len = (int)(dt2.getTime()-dt.getTime())/(60*60*1000*24);
        System.out.println("选择的天数为："+(len+1));
        System.out.println("开始生成关系文件...");
        System.out.println("................");
        Calendar time = Calendar.getInstance();
        time.setTime(dt);
        String name = "",date = "";
        ArrayList<String> stations = new ArrayList<>();
        JSONArray array = new JSONArray();
        for (int i = 0; i <= len; i++) {
            date = sdf.format(time.getTime());
            if (city.equals("hz")) {
                stations = SqlHelper.getStationsArray(date);
                array = SqlHelper.getRelations(date);
                date = date.replaceAll("-","").substring(4);
                name = "byday_"+date+".net";
            } else if (city.equals("ny")) {
                TreeMap<String, Object> collection = getRelationsFromFile(date);
                stations = (ArrayList<String>) collection.get("stations");
                array = (JSONArray) collection.get("relations");
                date = date.replaceAll("-","").substring(4);
                name = "byday_"+date+"_ny.net";
            }
            System.out.println("开始生成关系文件\""+name+"\"...");
            File file;
            try {
                file = new File(path+name);
                if(!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fos = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fos);
                bw.write("*Vertices "+stations.size()+"\n");
                for (int j = 0; j < stations.size(); j++) {
                    bw.write((j+1)+" \""+stations.get(j)+"\"");
                    bw.newLine();
                }
                bw.write("*Arcs\n");
                for (int k = 0; k < array.length(); k++) {
                    int lease = stations.indexOf(array.getJSONObject(k).getString("lease"))+1;
                    int ret = stations.indexOf(array.getJSONObject(k).getString("return"))+1;
                    String nums = array.getJSONObject(k).getString("nums");
                    bw.write(lease+" "+ret+" "+nums);
                    bw.write("\n");
                }
                bw.flush();
                bw.close();
                time.add(Calendar.DAY_OF_YEAR, 1);
                System.out.println("生成关系文件\""+name+"\"成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("................");
        System.out.println("生成所有关系文件成功");
    }

//    public static void getStationsFromFile(String date) {
//        String fileName = path + "station.json";
//        ArrayList<String> stations = new ArrayList<>();
//        String encoding = "ISO-8859-1";
//        File file = new File(fileName);
//        Long filelength = file.length();
//        String content = "";
//        byte[] filecontent = new byte[filelength.intValue()];
//        try {
//            FileInputStream in = new FileInputStream(file);
//            in.read(filecontent);
//            in.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            content =  new String(filecontent, encoding);
//        } catch (UnsupportedEncodingException e) {
//            System.err.println("The OS does not support " + encoding);
//            e.printStackTrace();
//        }
//        try {
//            JSONObject json = new JSONObject(content);
//            JSONArray array = json.getJSONArray("stationBeanList");
//            for (int i = 0; i < array.length(); i++) {
//
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    public static TreeMap<String, Object> getRelationsFromFile(String date, String... args) throws JSONException, ParseException {
        int len = args.length;
        String[] dates = args;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = path + "NY-"+date.replaceAll("-", "").substring(0, 6)+".csv";
        System.out.println(fileName);
        ArrayList<String> stations;
        TreeMap<String, Object> collection = new TreeMap<>();
        ArrayList<JSONObject> sortList = new ArrayList<>();
        TreeSet<String> set = new TreeSet<>();
        JSONArray relations;
        try {
            // 创建CSV读对象
            CsvReader csvReader = new CsvReader(fileName);
            // 读表头
            csvReader.readHeaders();
            while (csvReader.readRecord()){
                String startSt = csvReader.get("Start Station ID");
                String endSt = csvReader.get("End Station ID");
                String time = csvReader.get("Start Time");
                boolean isInDate = false;
                if (len == 0) isInDate = sdf.format(sdf.parse(time)).equals(date);
                else {
                    Calendar cl = Calendar.getInstance();
                    cl.setTime(sdf.parse(time));
                    long x = cl.getTimeInMillis();
                    cl.setTime(sdf.parse(date));
                    long y = cl.getTimeInMillis();
                    cl.setTime(sdf.parse(dates[0]));
                    long z = cl.getTimeInMillis();
                    isInDate = x >= y && x <= z;
                }
                if (isInDate) {
                    set.add(startSt);
                    set.add(endSt);
                    int idxInArray = -1;
                    for (int i = 0; i < sortList.size(); i++) {
                        if (sortList.get(i).getString("lease").equals(startSt) && sortList.get(i).getString("return").equals(endSt)) {
                            idxInArray = i;
                            break;
                        }
                    }
                    if (idxInArray == -1) {
                        JSONObject rel = new JSONObject();
                        rel.put("lease", startSt);
                        rel.put("return", endSt);
                        rel.put("nums", 1);
                        sortList.add(rel);
                    } else {
                        int num = sortList.get(idxInArray).getInt("nums");
                        sortList.get(idxInArray).put("nums", num + 1);
                    }
                }
            }
            stations = new ArrayList<>(set);
            Collections.sort(sortList, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    try {
                        int l = Integer.parseInt(o1.getString("lease"));
                        int r = Integer.parseInt(o2.getString("lease"));
                        if (l > r) {
                            return 1;
                        } else if (l < r) {
                            return -1;
                        } else {
                            l = Integer.parseInt(o1.getString("return"));
                            r = Integer.parseInt(o2.getString("return"));
                            return Integer.compare(l, r);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });
            relations = new JSONArray(sortList);
            collection.put("stations", stations);
            collection.put("relations", relations);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return collection;
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
                bw.write("*Vertices "+stations.size()+"\n");
                for (int j = 0; j < stations.size(); j++) {
                    bw.write((j+1)+" \""+stations.get(j)+"\"");
                    bw.write("\n");
                }
                bw.write("*Arcs\n");
                for (int m = 0;m < numbers.size();m++) {
                    bw.write(lines.get(m)+" "+numbers.get(m));
                    bw.write("\n");
                }
                bw.flush();
                bw.close();
                time.add(Calendar.DAY_OF_YEAR, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void generateBlock(String from,String to, String city) throws JSONException, ParseException {
        String name = "";
        String date = from.replaceAll("-","").substring(4) + "_" + to.replaceAll("-","").substring(4);
        ArrayList<String> stations = new ArrayList<>();
        JSONArray array = new JSONArray();
        long a,b;
        if (city.equals("hz")) {
            a = System.currentTimeMillis();
            stations = SqlHelper.getStationsArrayBlock(from,to);
            array = SqlHelper.getRelationsBlock(from, to);
            b = System.currentTimeMillis();
            System.out.println("查询数据库耗时："+(b-a)+"ms");
            name = "byday_"+date+".net";
        } else if (city.equals("ny")) {
            TreeMap<String, Object> collection = getRelationsFromFile(from, to);
            stations = (ArrayList<String>) collection.get("stations");
            array = (JSONArray) collection.get("relations");
            name = "byday_"+date+"_ny.net";
        }
        a = System.currentTimeMillis();
        System.out.println("开始生成关系文件\""+name+"\"...");
        System.out.println("................");
        File file;
        try {
            file = new File(path+name);
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fos = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fos);
            bw.write("*Vertices "+stations.size()+"\n");
            for (int i = 0; i < stations.size(); i++) {
                bw.write((i+1)+" \""+stations.get(i)+"\"");
                bw.write("\n");
            }
            bw.write("*Arcs\n");
            for (int j = 0; j < array.length(); j++) {
                int lease = stations.indexOf(array.getJSONObject(j).getString("lease"))+1;
                int ret = stations.indexOf(array.getJSONObject(j).getString("return"))+1;
                String nums = array.getJSONObject(j).getString("nums");
                bw.write(lease+" "+ret+" "+nums);
                bw.write("\n");
            }
            bw.flush();
            bw.close();
            b = System.currentTimeMillis();
            System.out.println("生成关系文件\""+name+"\"成功");
            System.out.println("生成文件耗时："+(b-a)+"ms");
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
            bw.write("*Vertices "+stations.size()+"\n");
            for (int i = 0; i < stations.size(); i++) {
                bw.write((i+1)+" \""+stations.get(i)+"\"");
                bw.write("\n");
            }
            bw.write("*Arcs\n");
            for (int m = 0;m < numbers.size();m++) {
                bw.write(lines.get(m)+" "+numbers.get(m));
                bw.write("\n");
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void generateBlock(String from,String to,String num,String[] points) {
        class SortByLease implements Comparator {
            @Override
            public int compare(Object o1, Object o2) {
                String[] str1 = (String[]) o1;
                String[] str2 = (String[]) o2;
                if(Integer.parseInt(str1[0]) > Integer.parseInt(str2[0])) return 1;
                return -1;
            }
        }
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
            bw.write("*Vertices "+stations.size()+"\n");
            for (int i = 0; i < stations.size(); i++) {
                bw.write((i+1)+" \""+stations.get(i)+"\"");
                bw.write("\n");
            }
            bw.write("*Arcs");
            ArrayList<String[]> relArr = new ArrayList<>();
            for (int j = 0; j < array.length(); j++) {
                int lease = stations.indexOf(array.getJSONObject(j).getString("lease"))+1;
                int ret = stations.indexOf(array.getJSONObject(j).getString("return"))+1;
                String nums = array.getJSONObject(j).getString("nums");
                relArr.add(new String[]{ String.valueOf(lease), String.valueOf(ret), nums });
            }
            Collections.sort(relArr, new SortByLease());
            for (int i = 0; i < relArr.size(); i++) {
                bw.write("\n");
                bw.write(relArr.get(i)[0]+" "+relArr.get(i)[1]+" "+relArr.get(i)[2]);
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
                bw.write("*Vertices\n");
                for (int j = 0; j < stations.size(); j++) {
                    bw.write((j+1)+" \""+stations.get(j)+"\"");
                    bw.write("\n");
                }
                bw.write("*Arcs\n");
                for (int k = 0; k < array.length(); k++) {
                    int lease = stations.indexOf(array.getJSONObject(k).getString("lease"))+1;
                    int ret = stations.indexOf(array.getJSONObject(k).getString("return"))+1;
                    String nums = array.getJSONObject(k).getString("nums");
                    bw.write(lease+" "+ret+" "+nums);
                    bw.write("\n");
                }
                bw.flush();
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<String> getVertices(String fileName) {
        String name_net = fileName+".net";
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
    public static TreeMap<String, ArrayList<String>> getCollection(String fileName, String type) {
        String name_clu = type + "_" + fileName+".clu";
        String name_net = fileName+".net";
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
            while((col = br.readLine())!=null) {
                if(!col.equals("") && StringUtils.isNumeric(col)) {
                    len = len>Integer.parseInt(col)?len:Integer.parseInt(col);
                    collection.add(col);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> vertices = getVertices(fileName);
        TreeMap<String, ArrayList<String>> scatter = new TreeMap();
        int begin = 0;
        if (type.equals("infomap")) begin = 1;
        for (int i=begin;i<=len;i++) {
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

    public static TreeMap<String, ArrayList<String>> getCollectionFromFile(String fileName, String level) {
        String name_clu = "";
        String name_net = fileName+".net";
        if (Integer.parseInt(level) > 0) {
            name_clu = "louvain_"+fileName+"_level"+level+".clu";
        } else {
            name_clu = "louvain_"+fileName+".clu";
        }
        String col;
        TreeMap<String, ArrayList<String>> scatter = new TreeMap();
        File file_clu = new File(path+name_clu);
        File file_net = new File(path+name_net);
        if (!file_clu.exists() || !file_net.exists()) {
            System.out.println("file "+name_clu+" does not exist or file "+name_net+" does not exist!");
            return null;
        }
        ArrayList<String> vertices = getVertices(fileName);
        ArrayList<String> collection = new ArrayList<>();
        FileReader fr;
        BufferedReader br;
        try {
            fr = new FileReader(file_clu);
            br = new BufferedReader(fr);
            while((col = br.readLine())!=null) {
                if(col.equals("")) {
                    continue;
                }
                collection.add(col.replaceAll("\\d*:|\\[|\\]|\\s*|'", ""));
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int len = vertices.size();
        for (int i = 0; i < collection.size(); i++) {
            String[] line = collection.get(i).split(",");
            ArrayList<String> colList = new ArrayList<>();
            for (int j = 0; j < line.length; j++) {
                colList.add(vertices.get(Integer.parseInt(line[j])-1));
            }
            scatter.put(String.valueOf(i+1), colList);
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
                bw.write("\n");
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
