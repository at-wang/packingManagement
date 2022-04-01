package cn.itwang.packingmanagement.test;


import com.alibaba.fastjson.JSON;

import com.baidu.aip.ocr.AipOcr;
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;

public class bduiTest {
    //设置APPID/AK/SK
    public static final String APP_ID = "25501692";
    public static final String API_KEY = "lnnhcIst1ZPbu83RPGUUjWpE";
    public static final String SECRET_KEY = "Z3acq7pDBymYmeKWOo8TMi8krXgqMwAA";

    public static void main(String[] args) {
        AipOcr client=new AipOcr(APP_ID,API_KEY,SECRET_KEY);
        sample(client);
    }
    public static void sample(AipOcr client) {
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("language_type", "CHN_ENG");
        options.put("detect_direction", "true");
        options.put("detect_language", "true");
        options.put("probability", "true");


        // 参数为本地图片路径
        String image = "D:\\images\\bdcar.jpeg";
        JSONObject res = client.plateLicense(image, options);
        System.out.println(res.toString(2));

        JSONObject res2 = client.plateLicense("http://r5l0wzjdd.hd-bkt.clouddn.com/252ef319-a23e-4c83-8d47-ca5395b91781.jpeg", options);
        System.out.println(res2.toString(2));
    }
}
