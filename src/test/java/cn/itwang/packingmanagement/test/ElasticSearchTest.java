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
     * ????????????
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
     * ????????????
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
     * ??????????????????
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
     * ??????
     */
    @Test
    public void test5() throws IOException {
        //????????????????????????
        SearchRequest searchRequest = new SearchRequest("index7");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //????????????
        //????????????
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //????????????????????????
        searchSourceBuilder.query(QueryBuilders.matchQuery("username", "???"));
        //??????????????????
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("???", "userSex", "username"));

        //????????????
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);

        //????????????
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
            //????????????
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
     * ????????????
     */
    @Test
    public void test6() throws IOException {
        SearchRequest searchRequest = new SearchRequest("packingmanagement");

        searchRequest.source().suggest(new SuggestBuilder().addSuggestion(
                "suggestions", SuggestBuilders
                        .completionSuggestion("parkingSuggestion")
                        .prefix("???")//????????????
                        .skipDuplicates(true) //??????
                        .size(10)//???????????????

        ));

        //????????????
        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //????????????
        Suggest suggest = searchResponse.getSuggest();
        //?????????????????????????????????????????????
        CompletionSuggestion mysuggestion = suggest.getSuggestion("suggestions");
        //??????options
        for (CompletionSuggestion.Entry.Option option : mysuggestion.getOptions()) {
            String string = option.getText().string();
            System.out.println(string);
        }
    }

    /**
     * ?????????????????????
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
     * ?????????????????????
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
     * ????????????????????????
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
