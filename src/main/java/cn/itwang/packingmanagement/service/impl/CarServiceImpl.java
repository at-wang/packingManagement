package cn.itwang.packingmanagement.service.impl;

import cn.itwang.packingmanagement.dao.CarDao;
import cn.itwang.packingmanagement.dao.ParkingCardDao;
import cn.itwang.packingmanagement.dao.ParkingDao;
import cn.itwang.packingmanagement.dao.UserDao;
import cn.itwang.packingmanagement.domain.Car;
import cn.itwang.packingmanagement.domain.CarVO;
import cn.itwang.packingmanagement.domain.Parking;
import cn.itwang.packingmanagement.domain.User;
import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.CarService;
import cn.itwang.packingmanagement.utils.DownloadUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.web.bind.annotation.PutMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CarServiceImpl extends ServiceImpl<CarDao, Car> implements CarService {

    @Autowired
    private CarDao carDao;

    @Autowired
    private ParkingDao parkingDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ParkingCardDao parkingCardDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RestHighLevelClient highLevelClient;

    /**
     * ??????????????????
     *
     * @param licencePlate
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Result queryCarInformation(String licencePlate, int currentPage, int pageSize) {
        String redisKey = licencePlate + currentPage + "-" + pageSize;
        //???????????????redis?????????
        String carInformation1 = (String) stringRedisTemplate.boundHashOps("carInformation").get(redisKey);
        if (carInformation1 != null) {
            Object carList1 = JSON.parse(carInformation1);
            System.out.println(carList1);
            System.out.println("??????redis??????");
            return new Result(true, "????????????", carList1);
        } else {
            //???????????????
            //??????1000??????????????????????????? ??????redis???????????????????????????????????? 1000??????????????????1000????????? ??????????????????service
            synchronized (this) {//???????????????????????????????????????
                //???????????????redis
                String carInformation2 = (String) stringRedisTemplate.boundHashOps("carInformation").get(redisKey);
                if (carInformation2 == null) {//???????????????????????????????????????null;????????????????????????????????????
                    int start=(currentPage-1)*pageSize;
                    int end=pageSize;
                    List<CarVO> carVOList = carDao.selectCarInformation(licencePlate, start, end);
                    long total = carDao.selectCarCount(licencePlate);
                    if (total>0){
                        carVOList.get(0).setTotal(total);
                    }
                    System.out.println("???????????????");
                    if (carVOList != null) {
                        //??????redis
                        String carPage = JSON.toJSONString(carVOList);
                        stringRedisTemplate.boundHashOps("carInformation").put(redisKey, carPage);
                        stringRedisTemplate.boundHashOps("carInformation").expire(1, TimeUnit.DAYS);
                        System.out.println("??????redis");
                        return new Result(true, "????????????", carVOList);
                    } else {
                        //?????????????????????????????????null ?????????????????????redis ?????????????????????
                        //????????????????????????redis????????????????????????
                        stringRedisTemplate.boundHashOps("carInformation").put(redisKey, "[]");
                        stringRedisTemplate.boundHashOps("carInformation").expire(30, TimeUnit.SECONDS);
                        //???????????????redis
                        String carInformation3 = (String) stringRedisTemplate.boundHashOps("carInformation").get(redisKey);
                        Object carList3 = JSON.parse(carInformation3);
                        System.out.println("??????redis3");
                        return new Result(false, "????????????", carList3);
                    }
                } else {
                    Object carList2 = JSON.parse(carInformation2);
                    System.out.println("??????redis2");
                    return new Result(true, "????????????", carList2);
                }
            }
        }
    }

    /**
     * ??????????????????
     * @param response
     */
    @Override
    public void exportCar(HttpServletResponse response) throws IOException {
        //??????????????? excel 2007
        Workbook workbook=new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("????????????");
        Row row = sheet.createRow(0);
        Cell cell=null;
        cell = row.createCell(0);
        cell.setCellValue("??????");
        cell = row.createCell(1);
        cell.setCellValue("???????????????");
        cell = row.createCell(2);
        cell.setCellValue("?????????");
        cell = row.createCell(3);
        cell.setCellValue("????????????");
        cell = row.createCell(4);
        cell.setCellValue("??????");
        cell = row.createCell(5);
        cell.setCellValue("????????????");
        cell = row.createCell(6);
        cell.setCellValue("????????????");
        cell=row.createCell(7);
        cell.setCellValue("??????");
        List<CarVO> carVOList = carDao.selectPoi();
        int i=1;
        for (CarVO carVO : carVOList) {
            Row rowValue = sheet.createRow(i++);
            sheet.setColumnWidth(1,30*256);
            sheet.setColumnWidth(4,20*256);
            sheet.setColumnWidth(5,20*256);
            cell= rowValue.createCell(0);
            cell.setCellValue(carVO.getCarId());
            cell= rowValue.createCell(1);
            cell.setCellValue(carVO.getParkingName());
            cell= rowValue.createCell(2);
            cell.setCellValue(carVO.getLicencePlate());
            cell= rowValue.createCell(3);
            cell.setCellValue(carVO.getType());
            cell= rowValue.createCell(4);
            cell.setCellValue(carVO.getUserName());
            cell= rowValue.createCell(5);
            cell.setCellValue(carVO.getUserPhone());
            cell= rowValue.createCell(6);
            cell.setCellValue(carVO.getUserSex());
            cell=rowValue.createCell(7);
            cell.setCellValue(carVO.getCarState());
        }

        //??????excel??????
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        DownloadUtils.download(byteArrayOutputStream,response,"????????????.xlsx");

    }

    /**
     * ??????????????????
     * @param car
     * @return
     */
    @Override
    public Result addCarInformation(Car car) throws IOException {
        Integer parkingId = car.getParkingId();
        //Integer userId = car.getUserId();
        Parking parking = parkingDao.selectById(parkingId);
       //User user = userDao.selectById(userId);
        if (parking==null){
            return new Result(false,"?????????????????????");
        }
//        if (user==null){
//            return new Result(false,"??????????????????");
//        }
        int insert = carDao.insert(car);
        if (insert>0){
            Integer carId = car.getCarId();
            List<Car> cars = carDao.selectList(null);
            for (Car car1 : cars) {
                List<String> carSuggestion=new ArrayList<>();
                String licencePlate = car1.getLicencePlate();
                carSuggestion.add(licencePlate);
                IndexRequest indexRequest=new IndexRequest("packingmanagement");
                indexRequest.source("carSuggestion",carSuggestion);
                highLevelClient.index(indexRequest,RequestOptions.DEFAULT);
            }
            stringRedisTemplate.delete("carInformation");
            return new Result(true,"????????????",carId);
        }else {
            return new Result(false,"????????????");
        }
    }

    /**
     * ??????????????????
     * @param car
     * @return
     */
    @Override
    public Result modifyCarInformation(Car car) throws IOException {
        Integer userId = car.getUserId();
        User user = userDao.selectById(userId);
        if (user==null){
            return new Result(false,"??????????????????");
        }
        int update = carDao.updateById(car);
        if (update>0){
            List<Car> cars = carDao.selectList(null);
            for (Car car1 : cars) {
                List<String> carSuggestion=new ArrayList<>();
                String licencePlate = car1.getLicencePlate();
                carSuggestion.add(licencePlate);
                IndexRequest indexRequest=new IndexRequest("packingmanagement");
                indexRequest.source("carSuggestion",carSuggestion);
                highLevelClient.index(indexRequest,RequestOptions.DEFAULT);
            }
            stringRedisTemplate.delete("carInformation");
            return new Result(true,"????????????");
        }else {
            return new Result(false,"????????????");
        }
    }

    /**
     * ??????????????????
     * @param carId
     * @return
     */
    @Override
    public Result removeCarInformation(Integer carId) throws IOException {
        int delete = carDao.deleteById(carId);
        if (delete>0){
            List<Car> cars = carDao.selectList(null);
            for (Car car1 : cars) {
                List<String> carSuggestion=new ArrayList<>();
                String licencePlate = car1.getLicencePlate();
                carSuggestion.add(licencePlate);
                IndexRequest indexRequest=new IndexRequest("packingmanagement");
                indexRequest.source("carSuggestion",carSuggestion);
                highLevelClient.index(indexRequest,RequestOptions.DEFAULT);
            }
            stringRedisTemplate.delete("carInformation");
            return new Result(true,"????????????");
        }else {
            return new Result(false,"????????????");
        }
    }

    /**
     * ????????????id????????????
     * @param userId
     * @return
     */
    @Override
    public boolean queryUserIdExist(Integer userId) {
        LambdaQueryWrapper<Car> lqw=new LambdaQueryWrapper<>();
        lqw.eq(userId!=null,Car::getUserId,userId);
        List<Car> carList = carDao.selectList(lqw);
        if (carList.size()>0){
            return true;
        }else {
            return false;
        }
    }

    /**
     * ???????????????????????? ??????elasticsearch
     * @param prefix
     * @return
     */
    @Override
    public Result queryCarCompletion(String prefix) throws IOException {
        SearchRequest searchRequest=new SearchRequest("packingmanagement");
        searchRequest.source().suggest(
                new SuggestBuilder().addSuggestion("suggestions", SuggestBuilders
                        .completionSuggestion("carSuggestion")//?????????????????????
                        .prefix(prefix)//????????????
                        .size(10)//????????????
                        .skipDuplicates(true)//??????
                )
        );
        //????????????
        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //????????????
        Suggest suggest = searchResponse.getSuggest();
        //?????????????????????????????????????????????
        CompletionSuggestion suggestSuggestion = suggest.getSuggestion("suggestions");
        //??????options
        List<CompletionSuggestion.Entry.Option> options = suggestSuggestion.getOptions();
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
     * @param carId
     * @param state
     * @return
     */
    @Override
    public Result modifyCarState(Integer carId, String state) {
        if (state.equals("?????????")){
            Car car = carDao.selectById(carId);
            car.setCarState("??????");
            int update = carDao.updateById(car);
            if (update>0){
                stringRedisTemplate.delete("carInformation");
                return new Result(true,"????????????",state);
            }
        }else if (state.equals("??????")){
            Car car = carDao.selectById(carId);
            car.setCarState("?????????");
            int update = carDao.updateById(car);
            if (update>0){
                stringRedisTemplate.delete("carInformation");
                return new Result(true,"????????????");
            }
        }
        return null;
    }

    /**
     * ??????????????????
     * @param type
     * @return
     */
    @Override
    public Result queryCarTypeCount(String type) {
        try {
            LambdaQueryWrapper<Car> lqw=new LambdaQueryWrapper<>();
            lqw.eq(type.length()>0,Car::getType,type);
            Integer count = carDao.selectCount(lqw);
            return new Result(true,"????????????",count);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"????????????");
        }

    }


}
