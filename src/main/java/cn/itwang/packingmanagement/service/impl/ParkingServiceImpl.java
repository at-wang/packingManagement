package cn.itwang.packingmanagement.service.impl;

import cn.itwang.packingmanagement.dao.ParkingDao;
import cn.itwang.packingmanagement.domain.Parking;
import cn.itwang.packingmanagement.domain.User;
import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.ParkingService;
import cn.itwang.packingmanagement.utils.DownloadUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.*;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ParkingServiceImpl extends ServiceImpl<ParkingDao, Parking> implements ParkingService {
    @Autowired
    private ParkingDao parkingDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RestHighLevelClient highLevelClient;

    /**
     * 查询parking信息
     *
     * @param parkingName
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Result queryParkingInformation(String parkingName, int currentPage, int pageSize) {
        String redisKey = parkingName + currentPage + "-" + pageSize;
        //第一次查询redis数据库
        String parkingInformation1 = (String) stringRedisTemplate.boundHashOps("parkingInformation").get(redisKey);
        if (parkingInformation1 != null) {
            Object parkingList1 = JSON.parse(parkingInformation1);
            System.out.println(parkingList1);
            System.out.println("查询redis成功");
            return new Result(true, "查询成功", parkingList1);
        } else {
            //查询数据库
            //如果1000个并发访问数据库？ 使用redis并没有解决减少数据库压力 1000个并发会启用1000个线程 但是够用一个service
            synchronized (this) {//使用双重检测锁解决缓存击穿
                //第二次查询redis
                String parkingInformation2 = (String) stringRedisTemplate.boundHashOps("parkingInformation").get(redisKey);
                if (parkingInformation2 == null) {//只有第一个进入的请求查询为null;只有一个请求会查询数据库
                    LambdaQueryWrapper<Parking> lqw = new LambdaQueryWrapper<>();
                    lqw.like(Strings.isNotEmpty(parkingName), Parking::getParkingName, parkingName);
                    IPage<Parking> page = new Page(currentPage, pageSize);
                    page = parkingDao.selectPage(page, lqw);
                    System.out.println("查询数据库");
                    if (page != null) {
                        //存入redis
                        String parkingPage = JSON.toJSONString(page);
                        stringRedisTemplate.boundHashOps("parkingInformation").put(redisKey, parkingPage);
                        stringRedisTemplate.boundHashOps("parkingInformation").expire(1, TimeUnit.DAYS);
                        System.out.println("查询redis");
                        return new Result(true, "查询成功", page);
                    } else {
                        //如果查询数据库中数据为null 大量请求会越过redis 继续访问数据库
                        //写一个非空数据到redis，并设置过期时间
                        stringRedisTemplate.boundHashOps("parkingInformation").put(redisKey, "[]");
                        stringRedisTemplate.boundHashOps("parkingInformation").expire(30, TimeUnit.SECONDS);
                        //第三次查询redis
                        String parkingInformation3 = (String) stringRedisTemplate.boundHashOps("parkingInformation").get(redisKey);
                        Object parkingList3 = JSON.parse(parkingInformation3);
                        System.out.println("查询redis3");
                        return new Result(false, "查询失败", parkingList3);
                    }
                } else {
                    Object parkingList2 = JSON.parse(parkingInformation2);
                    System.out.println("查询redis2");
                    return new Result(true, "查询成功", parkingList2);
                }
            }
        }
    }

    /**
     * 导出报表
     *
     * @param response
     * @return
     */
    @Override
    public void exportParking(HttpServletResponse response) throws IOException {
        List<Parking> parkingList = parkingDao.selectList(null);
        //创建工作薄 excel2007版
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("停车场信息");
        Row row = sheet.createRow(0);
        Cell cell = null;
        //设置序号
        cell = row.createCell(0);
        cell.setCellValue("序号");
        //设置序号
        cell = row.createCell(1);
        cell.setCellValue("停车场名称");
        //设置序号
        cell = row.createCell(2);
        cell.setCellValue("价格");
        //设置序号
        cell = row.createCell(3);
        cell.setCellValue("停车场总数");
        //设置序号
        cell = row.createCell(4);
        cell.setCellValue("车的数量");
        int i = 1;
        for (Parking parking : parkingList) {
            Row rowValue = sheet.createRow(i++);
            sheet.setColumnWidth(1,50*256);
            //设置序号
            cell = rowValue.createCell(0);
            cell.setCellValue(parking.getParkingId());
            //设置停车场名称
            cell = rowValue.createCell(1);
            cell.setCellValue(parking.getParkingName());
            //设置价格
            cell = rowValue.createCell(2);
            cell.setCellValue(parking.getParkingCharge());
            //设置停车位总数
            cell = rowValue.createCell(3);
            cell.setCellValue(parking.getParkingSpace());
            //设置车的数量
            cell = rowValue.createCell(4);
            cell.setCellValue(parking.getCarNumber());
        }
        //输出Excel文件
        //输出流
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        DownloadUtils.download(byteArrayOutputStream,response,"停车场信息.xlsx");

//
//        String filename = "停车场信息.xlsx";
//        response.setHeader("Content-Disposition",
//                "attachment; filename=" + new String(filename.getBytes(), "ISO8859-1"));
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        workbook.write(response.getOutputStream());

    }

    /**
     * 添加停车场信息
     * @param parking
     * @return
     */
    @Override
    public Result addParkingInformation(Parking parking) throws IOException {
        int insert = parkingDao.insert(parking);
        if (insert>0){
            //添加停车场名字到elasticsearch索引到elasticsearch
            //查询停车场全部信息
            List<Parking> parkingList = parkingDao.selectList(null);
            for (Parking parking1 : parkingList) {
                IndexRequest indexRequest=new IndexRequest("packingmanagement");
                List<String> parkingSuggestion=new ArrayList<>();
                String parkingName = parking1.getParkingName();
                parkingSuggestion.add(parkingName);
                indexRequest.source("parkingSuggestion",parkingSuggestion);
                highLevelClient.index(indexRequest,RequestOptions.DEFAULT);
            }

            stringRedisTemplate.delete("parkingInformation");
            return new Result(true,"添加成功");
        }else {
            return new Result(false,"添加失败");
        }
    }

    /**
     * 修改停车场信息
     * @param parking
     * @return
     */
    @Override
    public Result modifyParkingInformation(Parking parking) throws IOException {
        int update = parkingDao.updateById(parking);
        if (update>0){
            //添加停车场名字到elasticsearch索引到elasticsearch
            //查询停车场全部信息
            List<Parking> parkingList = parkingDao.selectList(null);
            for (Parking parking1 : parkingList) {
                IndexRequest indexRequest=new IndexRequest("packingmanagement");
                List<String> parkingSuggestion=new ArrayList<>();
                String parkingName = parking1.getParkingName();
                parkingSuggestion.add(parkingName);
                indexRequest.source("parkingSuggestion",parkingSuggestion);
                highLevelClient.index(indexRequest,RequestOptions.DEFAULT);
            }
            stringRedisTemplate.delete("parkingInformation");
            return new Result(true,"修改成功");
        }else {
            return new Result(false,"修改失败");
        }
    }

    /**
     * 删除停车场信息
     * @param parkingId
     * @return
     */
    @Override
    public Result removeParkingInformation(Integer parkingId) throws IOException {
        int delete = parkingDao.deleteById(parkingId);
        if (delete>0){
            //添加停车场名字到elasticsearch索引到elasticsearch
            //查询停车场全部信息
            List<Parking> parkingList = parkingDao.selectList(null);
            for (Parking parking1 : parkingList) {
                IndexRequest indexRequest=new IndexRequest("packingmanagement");
                List<String> parkingSuggestion=new ArrayList<>();
                String parkingName = parking1.getParkingName();
                parkingSuggestion.add(parkingName);
                indexRequest.source("parkingSuggestion",parkingSuggestion);
                highLevelClient.index(indexRequest,RequestOptions.DEFAULT);
            }
            stringRedisTemplate.delete("parkingInformation");
            return new Result(true,"删除成功");
        }else {
            return new Result(false,"删除失败");
        }
    }

    /**
     * 根据id添加车数量
     * @param parkingId
     * @return
     */
    @Override
    public Result modifyAddParkingCarCountById(Integer parkingId) {
        Parking parking = parkingDao.selectById(parkingId);
        String carNumber = parking.getCarNumber();
        int number = Integer.parseInt(carNumber);//车的数量
        String parkingSpace = parking.getParkingSpace();
        int space = Integer.parseInt(parkingSpace);//停车位
        if (number<space){
            parking.setCarNumber(Integer.toString(number+1));
            int update = parkingDao.updateById(parking);
            if (update>0){
                stringRedisTemplate.delete("parkingInformation");
                return new Result(true,"添加成功");
            }else {
                return new Result(false,"添加失败");
            }
        }else {
            return new Result(false,"当前停车场车位已满");
        }
    }

    /**
     * 根据id减少车数量
     * @param paringId
     * @return
     */
    @Override
    public Result modifyReduceParkingCarCountById(Integer paringId) {
        Parking parking = parkingDao.selectById(paringId);
        String carNumber = parking.getCarNumber();
        int number = Integer.parseInt(carNumber);
        parking.setCarNumber(Integer.toString(number-1));
        int update = parkingDao.updateById(parking);
        if (update>0){
            stringRedisTemplate.delete("parkingInformation");
            return new Result(true,"删除成功");
        }else {
            return new Result(false,"删除失败");
        }
    }

    /**
     * 自动补全
     * @param prefix
     * @return
     * @throws IOException
     */
    @Override
    public Result queryParkingCompletion(String prefix) throws IOException {
        SearchRequest searchRequest=new SearchRequest("packingmanagement");
        searchRequest.source().suggest(
                new SuggestBuilder().addSuggestion("suggestions", SuggestBuilders
                        .completionSuggestion("parkingSuggestion")//指定字段
                        .prefix(prefix)//
                        .skipDuplicates(true)
                        .size(10)
                )
        );
        //发送请求
        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
       //处理响应结果
        Suggest suggest = searchResponse.getSuggest();
        //根据补全查询名称，获取补全结果
        CompletionSuggestion suggestionParking = suggest.getSuggestion("suggestions");
        //获取options
        List<CompletionSuggestion.Entry.Option> options = suggestionParking.getOptions();
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
}
