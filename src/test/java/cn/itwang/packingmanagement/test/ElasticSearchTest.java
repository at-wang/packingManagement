package cn.itwang.packingmanagement.test;

import cn.itwang.packingmanagement.dao.CarDao;
import cn.itwang.packingmanagement.dao.ParkingCardDao;
import cn.itwang.packingmanagement.dao.ParkingDao;
import cn.itwang.packingmanagement.dao.ParkingRecordDao;
import cn.itwang.packingmanagement.domain.*;
import cn.itwang.packingmanagement.service.ParkingCardService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticSearchTest {

    @Autowired
    private RestHighLevelClient highLevelClient;

    @Autowired
    private CarDao carDao;

    @Autowired
    private ParkingDao parkingDao;

    @Autowired
    private ParkingCardDao parkingCardDao;

    @Autowired
    private ParkingRecordDao parkingRecordDao;

    @Test
    public void test() {
        System.out.println(highLevelClient);
    }


    /**
     * 创建索引
     */
    @Test
    public void test2() {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("index7");
        try {
            CreateIndexResponse createIndexResponse = highLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);

            System.out.println(createIndexResponse.isAcknowledged());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除索引
     */
    @Test
    public void test3() {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("index7");
        try {
            AcknowledgedResponse delete = highLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            System.out.println(delete.isAcknowledged());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加车辆数据
     */
    @Test
    public void test4() throws IOException {
        List<CarVO> carVOS = carDao.selectCarInformation(null, 0, 0);

        for (CarVO vo : carVOS) {
            String licencePlate = vo.getLicencePlate();
            List<String> suggestionList = new ArrayList<>();
            suggestionList.add(licencePlate);
            IndexRequest indexRequest = new IndexRequest("packingmanagement");
            indexRequest.source("carSuggestion",suggestionList);
            IndexResponse response = highLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        }
        System.out.println(carVOS);
    }

    /**
     * 查询
     */
    @Test
    public void test5() throws IOException {
        //用来封装检索条件
        SearchRequest searchRequest = new SearchRequest("index7");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //查询条件
        //查询全部
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //根据指定字段查询
        searchSourceBuilder.query(QueryBuilders.matchQuery("username", "波"));
        //查询多个字段
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("波", "userSex", "username"));

        //分页查询
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);

        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field username = new HighlightBuilder.Field("username");
        highlightBuilder.field(username);
        highlightBuilder.preTags("<label style='color: #8c222c'>");
        highlightBuilder.postTags("</label>");

        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = searchResponse.getHits();
        List<User> users = new ArrayList<>();
        Iterator<SearchHit> iterator = hits.iterator();
        while (iterator.hasNext()) {
            SearchHit next = iterator.next();
            String sourceAsString = next.getSourceAsString();
            User user = JSON.parseObject(sourceAsString, User.class);
            //高亮显示
            HighlightField highlightField = next.getHighlightFields().get("username");
            if (highlightField != null) {
                String s = Arrays.toString(highlightField.fragments());
                user.setUsername(s);
            }
            users.add(user);

        }
        System.out.println(users);
    }

    /**
     * 自动补全
     */
    @Test
    public void test6() throws IOException {
        SearchRequest searchRequest = new SearchRequest("packingmanagement");

        searchRequest.source().suggest(new SuggestBuilder().addSuggestion(
                "suggestions", SuggestBuilders
                        .completionSuggestion("parkingSuggestion")
                        .prefix("大")//搜索前缀
                        .skipDuplicates(true) //去重
                        .size(10)//查询多少条

        ));

        //发送请求
        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //处理结果
        Suggest suggest = searchResponse.getSuggest();
        //根据补全查询名称，获取补全结果
        CompletionSuggestion mysuggestion = suggest.getSuggestion("suggestions");
        //获取options
        for (CompletionSuggestion.Entry.Option option : mysuggestion.getOptions()) {
            String string = option.getText().string();
            System.out.println(string);
        }
    }

    /**
     * 添加停车场信息
     * @throws IOException
     */
    @Test
    public void test7() throws IOException {
        List<Parking> parkingList = parkingDao.selectList(null);
        for (Parking parking : parkingList) {
            List<String> parkingSuggestion = new ArrayList<>();
            String parkingName = parking.getParkingName();
            parkingSuggestion.add(parkingName);
            IndexRequest indexRequest = new IndexRequest("packingmanagement");
            indexRequest.source("parkingSuggestion",parkingSuggestion);
            IndexResponse response = highLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        }
    }

    /**
     * 添加停车卡信息
     * @throws IOException
     */
    @Test
    public void test8() throws IOException {
        List<ParkingCard> parkingCards = parkingCardDao.selectList(null);
        for (ParkingCard parkingCard : parkingCards) {
            List<String> parkingCardList=new ArrayList<>();
            String cardNumber = parkingCard.getCardNumber();
            parkingCardList.add(cardNumber);
            IndexRequest indexRequest=new IndexRequest("packingmanagement");
            indexRequest.source("parkingCardSuggestion",parkingCardList);
            IndexResponse response = highLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        }
    }

    /**
     * 添加停车记录信息
     * @throws IOException
     */
    @Test
    public void test9() throws IOException {
        List<ParkingRecordVO> parkingRecordVOS = parkingRecordDao.selectParkingRecord("", 0, 0);
        for (ParkingRecordVO parkingRecordVO : parkingRecordVOS) {
            List<String> parkingRecordList=new ArrayList<>();
            String licencePlate = parkingRecordVO.getLicencePlate();
            parkingRecordList.add(licencePlate);
            IndexRequest indexRequest=new IndexRequest("packingmanagement");
            indexRequest.source("parkingRecordSuggestion",parkingRecordList);
            highLevelClient.index(indexRequest,RequestOptions.DEFAULT);
        }
    }
}
