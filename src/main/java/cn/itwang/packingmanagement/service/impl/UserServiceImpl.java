package cn.itwang.packingmanagement.service.impl;

import cn.itwang.packingmanagement.dao.*;
import cn.itwang.packingmanagement.domain.Parking;
import cn.itwang.packingmanagement.domain.ParkingCard;
import cn.itwang.packingmanagement.domain.User;
import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.CarService;
import cn.itwang.packingmanagement.service.UserService;
import cn.itwang.packingmanagement.utils.DateUtils;
import cn.itwang.packingmanagement.utils.DownloadUtils;
import cn.itwang.packingmanagement.utils.RandomNumberUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private CarService carService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RestHighLevelClient highLevelClient;

    @Autowired
    private ParkingCardDao parkingCardDao;

    @Autowired
    private ParkingRecordDao parkingRecordDao;

    /**
     * ??????????????????
     *
     * @param userName
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Result queryUserInformation(String userName, int currentPage, int pageSize) {
        //??????redis
        String redisKey = userName + currentPage + "-" + pageSize;
        String userInformation1 = (String) stringRedisTemplate.boundHashOps("userInformation").get(redisKey);
        if (userInformation1 != null) {
            Object userList = JSON.parse(userInformation1);
            return new Result(true, "????????????", userList);
        } else {
            synchronized (this) {
                String userInformation2 = (String) stringRedisTemplate.boundHashOps("userInformation").get(redisKey);
                if (userInformation2 == null) {
                    LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
                    lqw.like(Strings.isNotEmpty(userName), User::getUsername, userName);
                    IPage<User> page = new Page(currentPage, pageSize);
                    page = userDao.selectPage(page, lqw);
                    if (page != null) {
                        //??????redis
                        String pageList = JSON.toJSONString(page);
                        stringRedisTemplate.boundHashOps("userInformation").put(redisKey, pageList);
                        stringRedisTemplate.boundHashOps("userInformation").expire(1, TimeUnit.DAYS);
                        return new Result(true, "????????????", page);
                    } else {
                        //?????????????????????????????????null ?????????????????????redis ?????????????????????
                        //????????????????????????redis????????????????????????
                        stringRedisTemplate.boundHashOps("userInformation").put(redisKey, "[]");
                        stringRedisTemplate.boundHashOps("userInformation").expire(30, TimeUnit.SECONDS);
                        //??????redis
                        String userInformation3 = (String) stringRedisTemplate.boundHashOps("userInformation").get(redisKey);
                        Object userList3 = JSON.parse(userInformation3);
                        return new Result(false, "????????????", userList3);
                    }
                } else {
                    Object parse = JSON.parse(userInformation1);
                    return new Result(true, "????????????", parse);
                }
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param user
     * @return
     */
    @Override
    public Result addUserInformation(User user) throws IOException {
        int insert = userDao.insert(user);
        if (insert > 0) {
            //?????????????????????elasticsearch?????????elasticsearch
            //????????????????????????
            List<User> users = userDao.selectList(null);
            for (User user1 : users) {
                List<String> userSuggestion=new ArrayList<>();
                IndexRequest indexRequest=new IndexRequest("packingmanagement");
                String username = user1.getUsername();
                userSuggestion.add(username);
                indexRequest.source("userSuggestion",userSuggestion);
                highLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            }

            //???????????????????????????  ???????????????1000000
            ParkingCard parkingCard=new ParkingCard();
            parkingCard.setCardBalance(1000000);
            parkingCard.setCardNumber(RandomNumberUtils.getRandomNumber());
            parkingCard.setCardOwner(user.getUsername());
            parkingCard.setCreatedTime(DateUtils.date(new Date()));
            int insert1 = parkingCardDao.insert(parkingCard);
            if (insert1>0){
                //??????????????????????????????elasticsearch
                List<ParkingCard> parkingCardList = parkingCardDao.selectList(null);
                for (ParkingCard card : parkingCardList) {
                    List<String> cardSuggestion=new ArrayList<>();
                    String cardNumber = card.getCardNumber();
                    cardSuggestion.add(cardNumber);
                    IndexRequest indexRequest=new IndexRequest("packingmanagement");
                    indexRequest.source("parkingCardSuggestion",cardSuggestion);
                    highLevelClient.index(indexRequest,RequestOptions.DEFAULT);
                }
            }
            Integer userId = user.getUserId();
            stringRedisTemplate.delete("userInformation");
            return new Result(true, "????????????",userId);
        } else {
            return new Result(false, "????????????",null);
        }
    }

    /**
     * ??????????????????
     *
     * @param user
     * @return
     */
    @Override
    public Result modifyUserInformation(User user) throws IOException {
        //?????????????????????????????? ?????????????????????
        Integer userId = user.getUserId();
        User user1 = userDao.selectById(userId);
        String username = user1.getUsername();

        int update = userDao.updateById(user);
        if (update > 0) {
            //?????????????????????elasticsearch?????????elasticsearch
            //????????????????????????
            List<User> users = userDao.selectList(null);
            for (User userElasticsearch : users) {
                List<String> userSuggestion=new ArrayList<>();
                IndexRequest indexRequest=new IndexRequest("packingmanagement");
                String usernameElasticsearch = userElasticsearch.getUsername();
                userSuggestion.add(usernameElasticsearch);
                indexRequest.source("userSuggestion",userSuggestion);
                highLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            }

            User user2 = userDao.selectById(userId);
            String username1 = user2.getUsername();
            if (!username1.equals(username)){//??????????????????????????????
                LambdaQueryWrapper<ParkingCard> lqw=new LambdaQueryWrapper<>();
                lqw.eq(ParkingCard::getCardOwner,username);
                //?????????????????????????????????
                List<ParkingCard> parkingCardList = parkingCardDao.selectList(lqw);
                //??????????????????
                if (parkingCardList.size()>0){
                    for (ParkingCard parkingCard : parkingCardList) {
                        parkingCard.setCardOwner(username1);
                        parkingCardDao.updateById(parkingCard);
                    }
                }
            }
            stringRedisTemplate.delete("userInformation");
            return new Result(true, "????????????");
        }else {
            return new Result(false,"????????????");
        }
    }

    /**
     * ??????????????????
     *
     * @param userId
     * @return
     */
    @Override
    public Result removeUserInformation(Integer userId) throws IOException {
        boolean flag = carService.queryUserIdExist(userId);
        if (!flag) {
            int delete = userDao.deleteById(userId);
            if (delete > 0) {
                //?????????????????????elasticsearch?????????elasticsearch
                //????????????????????????
                List<User> users = userDao.selectList(null);
                for (User user1 : users) {
                    List<String> userSuggestion=new ArrayList<>();
                    IndexRequest indexRequest=new IndexRequest("packingmanagement");
                    String username = user1.getUsername();
                    userSuggestion.add(username);
                    indexRequest.source("userSuggestion",userSuggestion);
                    highLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                }
                stringRedisTemplate.delete("userInformation");
                return new Result(true, "????????????");
            } else {
                return new Result(false, "????????????");
            }
        } else {
            return new Result(false, "?????????????????????????????????????????????");
        }
    }

    /**
     * ?????????????????????
     *
     * @param response
     * @throws IOException
     */
    @Override
    public void exportUser(HttpServletResponse response) throws IOException {
        //??????????????? 2007 excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("????????????");
        Row row = sheet.createRow(0);
        sheet.setColumnWidth(2,20*256);
        Cell cell = null;
        cell = row.createCell(0);
        cell.setCellValue("??????");

        cell = row.createCell(1);
        cell.setCellValue("????????????");

        cell = row.createCell(2);
        cell.setCellValue("????????????");

        cell = row.createCell(3);
        cell.setCellValue("????????????");

        int i = 1;
        List<User> userList = userDao.selectList(null);
        for (User user : userList) {
            Row row1 = sheet.createRow(i++);
            cell = row1.createCell(0);
            cell.setCellValue(user.getUserId());
            cell = row1.createCell(1);
            cell.setCellValue(user.getUsername());
            cell=row1.createCell(2);
            cell.setCellValue(user.getUserPhone());
            cell=row1.createCell(3);
            cell.setCellValue(user.getUserSex());
        }
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        DownloadUtils.download(byteArrayOutputStream,response,"????????????.xlsx");
    }

    /**
     * ????????????????????????
     * @param prefix
     * @return
     * @throws IOException
     */
    @Override
    public Result queryUserCompletion(String prefix) throws IOException {
        SearchRequest searchRequest=new SearchRequest("packingmanagement");
        searchRequest.source().suggest(
                new SuggestBuilder().addSuggestion("suggestions",SuggestBuilders
                        .completionSuggestion("userSuggestion")//???????????????????????????
                        .skipDuplicates(true)//??????
                        .prefix(prefix)//????????????
                        .size(10)//??????10???
                )
        );
        //????????????
        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //??????????????????
        Suggest suggest = searchResponse.getSuggest();
        //?????????????????????????????????????????????
        CompletionSuggestion suggestionParking = suggest.getSuggestion("suggestions");
        //??????options
        List<CompletionSuggestion.Entry.Option> options = suggestionParking.getOptions();
        //??????
        List<String> completionFiledList=new ArrayList<>();
        for (CompletionSuggestion.Entry.Option option : options) {
            String string = option.getText().string();
            completionFiledList.add(string);
        }
        if (completionFiledList!=null){
            return new Result(true,"????????????",completionFiledList);
        }else {
            return new Result(false,"????????????");
        }
    }

    /**
     * ??????????????????
     * @param sex
     * @return
     */
    @Override
    public Result queryUserSexCount(String sex) {
        try {
            LambdaQueryWrapper<User> lqw=new LambdaQueryWrapper<>();
            lqw.eq(sex.length()>0,User::getUserSex,sex);
            Integer count = userDao.selectCount(lqw);
            return new Result(true,"????????????",count);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"????????????");
        }
    }


}
