package cn.itwang.packingmanagement.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    //@Bean  非springboot应用需要配置
    public RestHighLevelClient getRestHighLevelClient(){
        HttpHost httpHost = new HttpHost("localhost", 9200, "http");
        RestClientBuilder restClientBuilder= RestClient.builder(httpHost);
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClientBuilder);
        return restHighLevelClient;
    }
}
