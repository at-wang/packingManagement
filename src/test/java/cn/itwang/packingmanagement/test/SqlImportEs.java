package cn.itwang.packingmanagement.test;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SqlImportEs {
    @Autowired
    private RestHighLevelClient highLevelClient;

    /**
     * 添加索引
     * @throws IOException
     */
    @Test
    public void createIndex() throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("packingmanagement");
        CreateIndexResponse createIndexResponse = highLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse.isAcknowledged());
    }


}
