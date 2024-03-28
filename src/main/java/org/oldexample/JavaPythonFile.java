package org.oldexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class JavaPythonFile {
    public static String getType(Object o) { //获取变bai量类型方法du

        return o.getClass().toString(); //使用int类型的getClass()方法

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO Auto-generated method stub

        String cmds = String.format("python ./resources/plus.py %s", "中国");
        Process pcs = Runtime.getRuntime().exec(cmds);
        pcs.waitFor();

        BufferedReader in = new BufferedReader(new InputStreamReader(pcs.getInputStream(), "GB2312"));

        Map<String, String> map = new HashMap<>();
        String line = null;
//        System.out.println(in.readLine());
        while ((line = in.readLine()) != null) {
            System.out.println(line);
            String[] s = line.split("\t");
//            System.out.println(s[0]+s[1]);
            map.put(s[0], s[1]);
        }
//        System.out.println(in.readLine());
        if (in.readLine() == null) {
            System.out.println("yes hhhhhh");
        }
//        String key1 = (String) map.keySet().toArray()[0];
        String key1 = (String) map.keySet().toArray()[0];
        String d1 = map.get(key1);
        double xx = Double.parseDouble(d1);
//        System.out.println(getType(xx));
//        if (xx > 0.6){
//            System.out.println("nice   ................");
//        }
    }
}
