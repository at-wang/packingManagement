package cn.itwang.packingmanagement.test;


import cn.itwang.packingmanagement.utils.QiniuUtils;
import com.baidubce.http.ApiExplorerClient;
import com.baidubce.http.HttpMethodName;
import com.baidubce.model.ApiExplorerRequest;
import com.baidubce.model.ApiExplorerResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
public class baiduTest2 {
    @Test
    public void test() {
        String path = "https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate";
        ApiExplorerRequest request = new ApiExplorerRequest(HttpMethodName.POST, path);


        // 设置header参数
        request.addHeaderParameter("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        // 设置query参数
        request.addQueryParameter("access_token", "24.b423aabfe305dea3cf55b1410c7596dc.2592000.1644742675.282335-25501692");

        // 设置jsonBody参数
        String jsonBody = "url=http://r5l0wzjdd.hd-bkt.clouddn.com/d87e2942-2539-456f-ace3-c32c2c00f134.jpeg";
        request.setJsonBody(jsonBody);

        ApiExplorerClient client = new ApiExplorerClient();

        try {
            ApiExplorerResponse response = client.sendRequest(request);
            // 返回结果格式为Json字符串
            System.out.println(response.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() throws InterruptedException {
        long l1 = System.currentTimeMillis();
        Thread.sleep(1000);
        long l = System.currentTimeMillis();
        long l2 = l - l1;
        System.out.println(l2);


    }
}
