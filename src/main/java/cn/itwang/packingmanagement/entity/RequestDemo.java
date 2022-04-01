package cn.itwang.packingmanagement.entity;

import com.baidubce.http.ApiExplorerClient;
import com.baidubce.http.HttpMethodName;
import com.baidubce.model.ApiExplorerRequest;
import com.baidubce.model.ApiExplorerResponse;

// 车牌识别 示例代码
public class RequestDemo {
    public static void main(String[] args) {
        String path = "https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate" +
                "?access_token=24.b423aabfe305dea3cf55b1410c7596dc.2592000.1644742675.282335-25501692&url" +
                "=http://r5l0wzjdd.hd-bkt.clouddn.com/10d5aaba-ca6e-4091-84e8-4b6041dd9552.jpeg";
        ApiExplorerRequest request = new ApiExplorerRequest(HttpMethodName.POST, path);


        // 设置header参数
//        request.addHeaderParameter("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

//        // 设置query参数
//        request.addQueryParameter("access_token", "24.b423aabfe305dea3cf55b1410c7596dc.2592000.1644742675.282335-25501692");

        // 设置jsonBody参数
        //String jsonBody = "url=https://baidu-ai.bj.bcebos.com/ocr/license_plate.jpeg";

        ApiExplorerClient client = new ApiExplorerClient();

        try {
            ApiExplorerResponse response = client.sendRequest(request);
            // 返回结果格式为Json字符串
            System.out.println(response.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}