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
                name = "ny_byday_"+date+".net";
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
            name = "ny_byday_"+date+".net";
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

    public static HashMap<String, ArrayList<String>> getVertices(String fileName) {
        String name_net = fileName+".net";
        String vertice = "";
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        ArrayList<String> vertices = new ArrayList<>();
        ArrayList<String> sequences = new ArrayList<>();
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
                sequences.add(stringArray[0]);
                vertices.add(vertice);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("sequences", sequences);
        map.put("vertices", vertices);
        return map;
    }
    public static HashMap<String, TreeMap<String, ArrayList<String>>> getCollection(String fileName, String type, String... args) {
        String name_clu = "";
        String city =  "";
        String newFileName = fileName;
        if (fileName.substring(0,2).equals("ny")) {
            city = "ny_";
            newFileName = fileName.substring(3);
        }
        if (type.equals("louvain"))  {
            String level = args[0];
            if (Integer.parseInt(level) > 0) {
                name_clu = city + "louvain_"+newFileName+"_level"+level+".clu";
            } else {
                name_clu = city + "louvain_"+newFileName+".clu";
            }
        } else {
            name_clu = city + type + "_" + newFileName+".clu";
        }
        String col;
        int len = 0;
        HashMap<String, TreeMap<String, ArrayList<String>>> map = new HashMap<>();
        TreeMap<String, ArrayList<String>> scatter = new TreeMap<>();
        TreeMap<String, ArrayList<String>> scatter_seq = new TreeMap<>();
        ArrayList<String> collection = new ArrayList<>();
        File file_clu = new File(path+name_clu);
        if (!file_clu.exists()) {
            System.out.println("file "+name_clu+" does not exist!");
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
        HashMap<String, ArrayList<String>> vMap = getVertices(fileName);
        ArrayList<String> vertices = vMap.get("vertices");
        ArrayList<String> sequences = vMap.get("sequences");
        for (int i=0;i<=len;i++) {          // 循环得到len+1个聚类（infomap为len个）
            ArrayList<String> array = new ArrayList<>();
            ArrayList<String> array_seq = new ArrayList<>();
            for (int j = 0; j < collection.size(); j++) {
                if (Integer.parseInt(collection.get(j)) == i) {
                    array.add(vertices.get(j));
                    array_seq.add(sequences.get(j));
                }
            }
            scatter_seq.put(String.valueOf(i), array_seq);
            scatter.put(String.valueOf(i),array);
        }
        map.put("scatter", scatter);
        map.put("scatter_seq", scatter_seq);
        return map;
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
            System.out.println("file "+name_clu+" does not exist!");
            return null;
        }
        HashMap<String, ArrayList<String>> map = getVertices(fileName);
        ArrayList<String> vertices = map.get("vertices");
        ArrayList<String> sequences = map.get("sequences");
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

    public static JSONArray getRelations(TreeMap<String,ArrayList<String>> map, String fileName) throws JSONException {
        String name_net = fileName+".net";
        File file_net = new File(path+name_net);
        FileReader fr;
        BufferedReader br;
        HashMap<String, Integer> heat = new HashMap();
        JSONArray relations = new JSONArray();
        try {
            fr = new FileReader(file_net);
            br = new BufferedReader(fr);
            Boolean sign = false;
            String vertice = "";
            while((vertice = br.readLine())!=null) {
                String[] stringArray = vertice.split(" ");
                if (sign) {
                    heat.put(stringArray[0] + "_" +stringArray[1], Integer.parseInt(stringArray[2]));
                } else if(stringArray[0].equals("*Arcs")) sign = true;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Iterator<String> it1 = map.keySet().iterator(); it1.hasNext();) {
            String i = it1.next();

            for (Iterator<String> it2 = map.keySet().iterator(); it2.hasNext();) {
                String j = it2.next();
                int num = 0;
                JSONObject jsonObject = new JSONObject();
                if (!i.equals(j)) {
                    ArrayList<String> from = map.get(i);
                    ArrayList<String> to = map.get(j);
                    for (int k = 0; k < from.size(); k++) {
                        for (int l = 0; l < to.size(); l++) {
                            String key = from.get(k)+ "_" +to.get(l);
                            if (heat.containsKey(key))
                                num = num + heat.get(key);
                        }
                    }
                    jsonObject.put("leaseid", i);
                    jsonObject.put("returnid", j);
                    jsonObject.put("nums", String.valueOf(num));
                    relations.put(jsonObject);
                }
            }
        }
        return relations;
    }

    public static JSONArray getScatRels(TreeMap<String,ArrayList<String>> map, String fileName) {
        File file_net = new File(path+fileName+".net");
        FileReader fr;
        BufferedReader br;
        HashMap<String, Integer> heat = new HashMap();
        HashMap<String, String> stations = new HashMap<>();
        JSONArray scatRels = new JSONArray();
        try {
            fr = new FileReader(file_net);
            br = new BufferedReader(fr);
            Boolean sign = false;
            String vertice = "";
            br.readLine();
            while((vertice = br.readLine())!=null) {
                String[] stringArray = vertice.split(" ");
                if (sign) {
                    heat.put(stringArray[0] + "_" +stringArray[1], Integer.parseInt(stringArray[2]));
                }
                else if(stringArray[0].equals("*Arcs")) sign = true;
                else {
                    stations.put(stringArray[0], stringArray[1].replace("\"",""));
                }
            }
            br.close();
            Iterator it = map.keySet().iterator();
            JSONObject jsonObject = new JSONObject();
            while(it.hasNext()) {
                Object key = it.next();
                ArrayList<String> scatter = map.get(key);
                JSONArray array = new JSONArray();
                for (int i = 0; i < scatter.size(); i++) {
                    for (int j = 0; j < scatter.size(); j++) {
                        if (i != j) {
                            JSONObject obj = new JSONObject();
                            int num = 0;
                            String idx = scatter.get(i) + "_" + scatter.get(j);
                            if (heat.containsKey(idx))  num = heat.get(idx);
                            obj.put("lease",stations.get(scatter.get(i)));
                            obj.put("return",stations.get(scatter.get(j)));
                            obj.put("nums",num);
                            array.put(obj);
                        }
                    }
                }
                jsonObject.put(String.valueOf(key), array);
            }
            scatRels.put(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scatRels;
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
