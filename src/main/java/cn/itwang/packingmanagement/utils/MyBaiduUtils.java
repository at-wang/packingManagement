package cn.itwang.packingmanagement.utils;


import com.alibaba.fastjson.JSON;
import com.baidu.aip.ocr.AipOcr;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 百度ui工具类
 */
public class MyBaiduUtils {
    //设置APPID/AK/SK
    public static final String APP_ID = "25501692";
    public static final String API_KEY = "lnnhcIst1ZPbu83RPGUUjWpE";
    public static final String SECRET_KEY = "Z3acq7pDBymYmeKWOo8TMi8krXgqMwAA";
    public static final String ACCESS_TOKEN = "24.b423aabfe305dea3cf55b1410c7596dc.2592000.1644742675.282335-25501692";

    public static String getLicencePlate(String url) {
        try {
            URL newUrl = new URL("https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate?access_token=" + ACCESS_TOKEN + "&url=" + url);
            InputStream inputStream = newUrl.openStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            //为字符输入流添加缓冲
            BufferedReader br = new BufferedReader(inputStreamReader);
            String data = br.readLine();//读取数据
            Map<String, Map<String, String>> parse = (Map<String, Map<String, String>>) JSON.parse(data);
            System.out.println(parse.get("words_result").get("number"));
            String licencePlate = parse.get("words_result").get("number");
            return licencePlate;
        } catch (Exception e) {
            e.printStackTrace();
            return "汽车图片出错啦啦";
        }
    }
}