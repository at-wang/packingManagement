package cn.itwang.packingmanagement.entity;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class test {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate?access_token=24.b423aabfe305dea3cf55b1410c7596dc.2592000.1644742675.282335-25501692&url=http://r5l0wzjdd.hd-bkt.clouddn.com/10d5aaba-ca6e-4091-84e8-4b6041dd9552.jpeg");
        InputStream is = url.openStream();
        InputStreamReader isr = new InputStreamReader(is,"utf-8");
        //为字符输入流添加缓冲
        BufferedReader br = new BufferedReader(isr);
        String data = br.readLine();//读取数据
        Map<String,Map<String,String>> parse = (Map<String, Map<String, String>>) JSON.parse(data);
        System.out.println(parse.get("words_result").get("number"));
        System.out.println(data);
        br.close();
        isr.close();
        is.close();
    }
}
