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
     * 查询车辆信息
     *
     * @param licencePlate
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Result queryCarInformation(String licencePlate, int currentPage, int pageSize) {
        String redisKey = licencePlate + currentPage + "-" + pageSize;
        //第一次查询redis数据库
        String carInformation1 = (String) stringRedisTemplate.boundHashOps("carInformation").get(redisKey);
        if (carInformation1 != null) {
            Object carList1 = JSON.parse(carInformation1);
            System.out.println(carList1);
            System.out.println("查询redis成功");
            return new Result(true, "查询成功", carList1);
        } else {
            //查询数据库
            //如果1000个并发访问数据库？ 使用redis并没有解决减少数据库压力 1000个并发会启用1000个线程 但是够用一个service
            synchronized (this) {//使用双重检测锁解决缓存击穿
                //第二次查询redis
                String carInformation2 = (String) stringRedisTemplate.boundHashOps("carInformation").get(redisKey);
                if (carInformation2 == null) {//只有第一个进入的请求查询为null;只有一个请求会查询数据库
                    int start=(currentPage-1)*pageSize;
                    int end=pageSize;
                    List<CarVO> carVOList = carDao.selectCarInformation(licencePlate, start, end);
                    long total = carDao.selectCarCount(licencePlate);
                    if (total>0){
                        carVOList.get(0).setTotal(total);
                    }
                    System.out.println("查询数据库");
                    if (carVOList != null) {
                        //存入redis
                        String carPage = JSON.toJSONString(carVOList);
                        stringRedisTemplate.boundHashOps("carInformation").put(redisKey, carPage);
                        stringRedisTemplate.boundHashOps("carInformation").expire(1, TimeUnit.DAYS);
                        System.out.println("查询redis");
                        return new Result(true, "查询成功", carVOList);
                    } else {
                        //如果查询数据库中数据为null 大量请求会越过redis 继续访问数据库
                        //写一个非空数据到redis，并设置过期时间
                        stringRedisTemplate.boundHashOps("carInformation").put(redisKey, "[]");
                        stringRedisTemplate.boundHashOps("carInformation").expire(30, TimeUnit.SECONDS);
                        //第三次查询redis
                        String carInformation3 = (String) stringRedisTemplate.boundHashOps("carInformation").get(redisKey);
                        Object carList3 = JSON.parse(carInformation3);
                        System.out.println("查询redis3");
                        return new Result(false, "查询失败", carList3);
                    }
                } else {
                    Object carList2 = JSON.parse(carInformation2);
                    System.out.println("查询redis2");
                    return new Result(true, "查询成功", carList2);
                }
            }
        }
    }

    /**
     * 导出车辆信息
     * @param response
     */
    @Override
    public void exportCar(HttpServletResponse response) throws IOException {
        //创建工作薄 excel 2007
        Workbook workbook=new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("停车信息");
        Row row = sheet.createRow(0);
        Cell cell=null;
        cell = row.createCell(0);
        cell.setCellValue("序号");
        cell = row.createCell(1);
        cell.setCellValue("停车场名称");
        cell = row.createCell(2);
        cell.setCellValue("车牌号");
        cell = row.createCell(3);
        cell.setCellValue("车的类型");
        cell = row.createCell(4);
        cell.setCellValue("车主");
        cell = row.createCell(5);
        cell.setCellValue("车主电话");
        cell = row.createCell(6);
        cell.setCellValue("车主性别");
        cell=row.createCell(7);
        cell.setCellValue("状态");
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

        //输出excel文件
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        DownloadUtils.download(byteArrayOutputStream,response,"停车信息.xlsx");

    }

    /**
     * 添加车辆信息
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
            return new Result(false,"没有当前停车场");
        }
//        if (user==null){
//            return new Result(false,"没有当前用户");
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
            return new Result(true,"添加成功",carId);
        }else {
            return new Result(false,"添加失败");
        }
    }

    /**
     * 修改车辆信息
     * @param car
     * @return
     */
    @Override
    public Result modifyCarInformation(Car car) throws IOException {
        Integer userId = car.getUserId();
        User user = userDao.selectById(userId);
        if (user==null){
            return new Result(false,"没有当前用户");
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
            return new Result(true,"修改成功");
        }else {
            return new Result(false,"修改失败");
        }
    }

    /**
     * 删除车辆信息
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
            return new Result(true,"删除成功");
        }else {
            return new Result(false,"删除失败");
        }
    }

    /**
     * 查询用户id是否存在
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
     * 实现搜索自动补全 采用elasticsearch
     * @param prefix
     * @return
     */
    @Override
    public Result queryCarCompletion(String prefix) throws IOException {
        SearchRequest searchRequest=new SearchRequest("packingmanagement");
        searchRequest.source().suggest(
                new SuggestBuilder().addSuggestion("suggestions", SuggestBuilders
                        .completionSuggestion("carSuggestion")//自动补全字段名
                        .prefix(prefix)//搜索前缀
                        .size(10)//搜素条数
                        .skipDuplicates(true)//去重
                )
        );
        //发送请求
        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //处理结果
        Suggest suggest = searchResponse.getSuggest();
        //根据补全查询名称，获取补全结果
        CompletionSuggestion suggestSuggestion = suggest.getSuggestion("suggestions");
        //获取options
        List<CompletionSuggestion.Entry.Option> options = suggestSuggestion.getOptions();
        //遍历
        List<String> completionFiledList=new ArrayList<>();
        for (CompletionSuggestion.Entry.Option option : options) {
            String string = option.getText().string();
            completionFiledList.add(string);
        }
       if (completionFiledList!=null){
           return new Result(true,"查询成功",completionFiledList);
       }else {
           return new Result(false,"查询失败");
       }
    }


    /**
     * 修改停车状态
     * @param carId
     * @param state
     * @return
     */
    @Override
    public Result modifyCarState(Integer carId, String state) {
        if (state.equals("未离开")){
            Car car = carDao.selectById(carId);
            car.setCarState("离开");
            int update = carDao.updateById(car);
            if (update>0){
                stringRedisTemplate.delete("carInformation");
                return new Result(true,"修改成功",state);
            }
        }else if (state.equals("离开")){
            Car car = carDao.selectById(carId);
            car.setCarState("未离开");
            int update = carDao.updateById(car);
            if (update>0){
                stringRedisTemplate.delete("carInformation");
                return new Result(true,"修改成功");
            }
        }
        return null;
    }

    /**
     * 查询车辆数量
     * @param type
     * @return
     */
    @Override
    public Result queryCarTypeCount(String type) {
        try {
            LambdaQueryWrapper<Car> lqw=new LambdaQueryWrapper<>();
            lqw.eq(type.length()>0,Car::getType,type);
            Integer count = carDao.selectCount(lqw);
            return new Result(true,"查询成功",count);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"查询失败");
        }

    }


}
