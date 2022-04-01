package cn.itwang.packingmanagement.service.impl;

import ch.qos.logback.classic.sift.SiftAction;
import cn.itwang.packingmanagement.dao.ParkingRecordDao;
import cn.itwang.packingmanagement.domain.ParkingRecord;
import cn.itwang.packingmanagement.domain.ParkingRecordVO;
import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.ParkingRecordService;
import cn.itwang.packingmanagement.utils.DateUtils;
import cn.itwang.packingmanagement.utils.DownloadUtils;
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
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ParkingRecordServiceImpl extends ServiceImpl<ParkingRecordDao, ParkingRecord> implements ParkingRecordService {
    @Autowired
    private ParkingRecordDao parkingRecordDao;

    @Autowired
    private RestHighLevelClient highLevelClient;

    /**
     * 查询全部停车记录
     *
     * @param licencePlate
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Result queryParkingRecord(String licencePlate, int currentPage, int pageSize) {
        int start = (currentPage - 1) * pageSize;
        int end = pageSize;
        List<ParkingRecordVO> parkingRecordVOList = parkingRecordDao.selectParkingRecord(licencePlate, start, end);
        long total = parkingRecordDao.selectParkingRecordCount(licencePlate);
        if (parkingRecordVOList != null) {//计算停车时间花费钱数
            for (ParkingRecordVO parkingRecordVO : parkingRecordVOList) {
                //获取数据库时间
                Date stopTime = parkingRecordVO.getStopTime();
                Date driveAwayTime = parkingRecordVO.getDriveAwayTime();
                //转换时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formatStopTime = sdf.format(stopTime);
                String formatDriveAwayTime = sdf.format(driveAwayTime);
                parkingRecordVO.setStopTimeVO(formatStopTime);
                parkingRecordVO.setDriveAwayTimeVO(formatDriveAwayTime);
                //计算花费
                long time = driveAwayTime.getTime() - stopTime.getTime();
                double parkingCharge = parkingRecordVO.getParkingCharge();
                //获取花费金额
                double costMoney = (parkingCharge / 3600) * (time / 1000);
                parkingRecordVO.setCostMoney(Math.ceil(costMoney));
            }
            ParkingRecordVO parkingRecordVO = parkingRecordVOList.get(0);
            parkingRecordVO.setTotal(total);
            return new Result(true, "查询成功", parkingRecordVOList);
        } else {
            return new Result(false, "查询失败");
        }
    }

    /**
     * 根据id删除停车记录
     *
     * @param parkingRecordId
     * @return
     */
    @Override
    public Result removeParkingRecordById(Integer parkingRecordId) {
        int delete = parkingRecordDao.deleteById(parkingRecordId);
        if (delete > 0) {
            return new Result(true, "删除成功");
        } else {
            return new Result(false, "删除失败");
        }
    }

    /**
     * 导出数据
     *
     * @param httpServletResponse
     */
    @Override
    public void exportParkingRecord(HttpServletResponse httpServletResponse) throws IOException {
        //创建工作薄 2007
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("停车记录信息");
        Row row = sheet.createRow(0);
        Cell cell = null;
        cell = row.createCell(0);
        cell.setCellValue("编号");
        cell = row.createCell(1);
        cell.setCellValue("停车场名称");
        cell = row.createCell(2);
        cell.setCellValue("车牌号");
        cell = row.createCell(3);
        cell.setCellValue("卡号");
        cell = row.createCell(4);
        cell.setCellValue("停车时间");
        cell = row.createCell(5);
        cell.setCellValue("离开时间");
        cell = row.createCell(6);
        cell.setCellValue("花费");
        List<ParkingRecordVO> parkingRecordVOS = parkingRecordDao.selectParkingRecord("", 0, 0);
        int i = 1;
        for (ParkingRecordVO parkingRecordVO : parkingRecordVOS) {
            //获取数据库时间
            Date stopTime = parkingRecordVO.getStopTime();
            Date driveAwayTime = parkingRecordVO.getDriveAwayTime();
            //转换时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatStopTime = sdf.format(stopTime);
            String formatDriveAwayTime = sdf.format(driveAwayTime);
            parkingRecordVO.setStopTimeVO(formatStopTime);
            parkingRecordVO.setDriveAwayTimeVO(formatDriveAwayTime);
            //计算花费
            long time = driveAwayTime.getTime() - stopTime.getTime();
            double parkingCharge = parkingRecordVO.getParkingCharge();
            //获取花费金额
            double costMoney = (parkingCharge / 3600) * (time / 1000);
            parkingRecordVO.setCostMoney(Math.ceil(costMoney));
            //设计表格
            Row rowValue = sheet.createRow(i++);
            sheet.setColumnWidth(1, 30 * 256);
            sheet.setColumnWidth(3, 22 * 256);
            sheet.setColumnWidth(4, 22 * 256);
            sheet.setColumnWidth(5, 22 * 256);
            cell = rowValue.createCell(0);
            cell.setCellValue(parkingRecordVO.getParkingRecordId());
            cell = rowValue.createCell(1);
            cell.setCellValue(parkingRecordVO.getParkingName());
            cell = rowValue.createCell(2);
            cell.setCellValue(parkingRecordVO.getLicencePlate());
            cell = rowValue.createCell(3);
            cell.setCellValue(parkingRecordVO.getCardNumber());
            cell = rowValue.createCell(4);
            cell.setCellValue(parkingRecordVO.getStopTimeVO());
            cell = rowValue.createCell(5);
            cell.setCellValue(parkingRecordVO.getDriveAwayTimeVO());
            cell = rowValue.createCell(6);
            cell.setCellValue(parkingRecordVO.getCostMoney());
        }
        //输出excel文件
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        DownloadUtils.download(byteArrayOutputStream, httpServletResponse, "停车记录.xlsx");

    }

    /**
     * 自动补全
     *
     * @param prefix
     * @return
     * @throws IOException
     */
    @Override
    public Result queryParkingRecordCompletion(String prefix) throws IOException {
        SearchRequest searchRequest = new SearchRequest("packingmanagement");
        searchRequest.source().suggest(
                new SuggestBuilder().addSuggestion("suggestions", SuggestBuilders
                        .completionSuggestion("parkingRecordSuggestion")//指定字段
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
        List<String> completionFiledList = new ArrayList<>();
        for (CompletionSuggestion.Entry.Option option : options) {
            String string = option.getText().string();
            completionFiledList.add(string);
        }
        if (completionFiledList != null) {
            return new Result(true, "查询成功", completionFiledList);
        } else {
            return new Result(false, "查询失败");
        }
    }


    /**
     * 添加停车记录
     * @param parkingRecord
     * @return
     */
    @Override
    public Result addParkingRecord(ParkingRecord parkingRecord) throws IOException {
        int insert = parkingRecordDao.insert(parkingRecord);
        if(insert>0){
            Integer parkingRecordId = parkingRecord.getParkingRecordId();
            List<ParkingRecordVO> parkingRecordVOS = parkingRecordDao.selectParkingRecord("", 0, 0);
            for (ParkingRecordVO parkingRecordVO : parkingRecordVOS) {
                List<String> parkingRecordList=new ArrayList<>();
                String licencePlate = parkingRecordVO.getLicencePlate();
                parkingRecordList.add(licencePlate);
                IndexRequest indexRequest=new IndexRequest("packingmanagement");
                indexRequest.source("parkingRecordSuggestion",parkingRecordList);
                highLevelClient.index(indexRequest,RequestOptions.DEFAULT);
            }
            return new Result(true,"添加成功",parkingRecordId);
        }
        return new Result(false,"添加失败");
    }

    /**
     * 根据id修改离开时间
     * @param parkingRecordId
     * @return
     */
    @Override
    public Result modifyAwayTime(Integer parkingRecordId) {
        ParkingRecord parkingRecord = parkingRecordDao.selectById(parkingRecordId);
        parkingRecord.setDriveAwayTime(new Date());
        int update = parkingRecordDao.updateById(parkingRecord);
        if (update>0){
            return new Result(true,"修改成功");
        }else {
            return new Result(false,"修改失败");
        }
    }


}
