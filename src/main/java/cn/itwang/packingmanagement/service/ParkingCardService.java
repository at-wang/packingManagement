package cn.itwang.packingmanagement.service;

import cn.itwang.packingmanagement.domain.ParkingCard;
import cn.itwang.packingmanagement.entity.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ParkingCardService extends IService<ParkingCard> {
    //查询停车卡信息
    public Result queryParkingCard(String cardNumber,int currentPage,int pageSize);
    //自动补全
    public Result queryParkingCardCompletion(String prefix) throws IOException;
    //根据id改变余额
    public Result modifyCardBalance(Integer parkingCardId,int money);
    //导出数据
    public void exportParkingCard(HttpServletResponse response) throws IOException;
    //根据用户名查询卡号
    public Result queryCardId(String cardOwner);
    //查询办卡数量
    public Result queryCardCount(String startTime,String stopTime);
}
