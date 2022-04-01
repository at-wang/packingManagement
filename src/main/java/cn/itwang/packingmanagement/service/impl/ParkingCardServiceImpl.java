package cn.itwang.packingmanagement.service.impl;

import cn.itwang.packingmanagement.dao.ParkingCardDao;
import cn.itwang.packingmanagement.domain.ParkingCard;
import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.ParkingCardService;
import cn.itwang.packingmanagement.utils.DownloadUtils;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ParkingCardServiceImpl extends ServiceImpl<ParkingCardDao, ParkingCard> implements ParkingCardService {

    @Autowired
    private ParkingCardDao parkingCardDao;

    @Autowired
    private RestHighLevelClient highLevelClient;

    /**
     * 查询提车卡信息
     * @param cardNumber
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Result queryParkingCard(String cardNumber, int currentPage, int pageSize) {
        LambdaQueryWrapper<ParkingCard> lqw=new LambdaQueryWrapper<>();
        lqw.like(Strings.isNotEmpty(cardNumber),ParkingCard::getCardNumber,cardNumber);
        IPage<ParkingCard> page=new Page(currentPage,pageSize);
        page=parkingCardDao.selectPage(page, lqw);
        if (page!=null){
            return new Result(true,"查询成功",page);
        }else {
            return new Result(false,"查询失败");
        }
    }

    /**
     * 根据前缀自动补全
     * @param prefix
     * @return
     */
    @Override
    public Result queryParkingCardCompletion(String prefix) throws IOException {
        SearchRequest searchRequest=new SearchRequest("packingmanagement");
        searchRequest.source().suggest(
                new SuggestBuilder().addSuggestion("suggestions", SuggestBuilders
                        .completionSuggestion("parkingCardSuggestion")//指定字段
                        .prefix(prefix)//搜索前缀
                        .skipDuplicates(true)//去重
                        .size(10)//查询条数
                )
        );
        //发送请求
        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //获取suggestion
        Suggest suggest = searchResponse.getSuggest();
        //解析suggest
        CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
        //获取options
        List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
        //遍历
        List<String> completionSuggestion=new ArrayList<>();
        for (CompletionSuggestion.Entry.Option option : options) {
            String string = option.getText().string();
            completionSuggestion.add(string);
        }
       if (options!=null){
           return new Result(true,"查询成功",completionSuggestion);
       }else {
           return new Result(false,"查询失败");
       }
    }




    /**
     * 修改余额
     * @param parkingCardId
     * @param money
     * @return
     */
    @Override
    public Result modifyCardBalance(Integer parkingCardId,int money) {
        ParkingCard parkingCard = parkingCardDao.selectById(parkingCardId);
        if (parkingCard!=null){
            Integer cardBalance = parkingCard.getCardBalance();
            parkingCard.setCardBalance(cardBalance+money);
            parkingCardDao.updateById(parkingCard);
            return new Result(true,"充值成功");
        }else {
            return new Result(false,"充值失败");
        }
    }


    /**
     * 导出数据
     * @param response
     */
    @Override
    public void exportParkingCard(HttpServletResponse response) throws IOException {
        //创建工作簿 excel2007
        Workbook workbook=new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("停车卡信息");
        sheet.setColumnWidth(1,20*256);
        sheet.setColumnWidth(3,25*256);
        sheet.setColumnWidth(4,25*256);
        Row row = sheet.createRow(0);
        Cell cell=null;
        cell = row.createCell(0);
        cell.setCellValue("编号");
        cell = row.createCell(1);
        cell.setCellValue("余额");
        cell = row.createCell(2);
        cell.setCellValue("卡主");
        cell = row.createCell(3);
        cell.setCellValue("卡号");
        cell = row.createCell(4);
        cell.setCellValue("办卡时间");

        int i=1;
        List<ParkingCard> parkingCardList = parkingCardDao.selectList(null);
        for (ParkingCard parkingCard : parkingCardList) {
            Row rowValue = sheet.createRow(i++);
            cell= rowValue.createCell(0);
            cell.setCellValue(parkingCard.getParkingCardId());
            cell= rowValue.createCell(1);
            cell.setCellValue(parkingCard.getCardBalance());
            cell= rowValue.createCell(2);
            cell.setCellValue(parkingCard.getCardOwner());
            cell= rowValue.createCell(3);
            cell.setCellValue(parkingCard.getCardNumber());
            cell= rowValue.createCell(4);
            Date createdTime = parkingCard.getCreatedTime();
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = sdf.format(createdTime);
            cell.setCellValue(format);
        }

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        DownloadUtils.download(byteArrayOutputStream,response,"停车卡信息.xlsx");

    }

    /**
     *  根据用户名查询卡号
     * @param cardOwner
     * @return
     */
    @Override
    public Result queryCardId(String cardOwner) {
        LambdaQueryWrapper<ParkingCard> lqw=new LambdaQueryWrapper<>();
        lqw.eq(Strings.isNotEmpty(cardOwner),ParkingCard::getCardOwner,cardOwner);
        List<ParkingCard> parkingCardList = parkingCardDao.selectList(lqw);
        if (parkingCardList!=null){
            Integer parkingCardId = parkingCardList.get(0).getParkingCardId();
            return new Result(true,"查询成功",parkingCardId);
        }else {
            return new Result(false,"查询失败");
        }
    }

    /**
     * 查询办卡数量
     * @param startTime
     * @param stopTime
     * @return
     */
    @Override
    public Result queryCardCount(String startTime, String stopTime) {
        Integer count = null;
        try {
            LambdaQueryWrapper<ParkingCard> lqw=new LambdaQueryWrapper<>();
            lqw.between(ParkingCard::getCreatedTime,startTime,stopTime);
            count = parkingCardDao.selectCount(lqw);
            return new Result(true,"查询成功",count);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"查询失败",count);
        }

    }


}
