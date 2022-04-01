package cn.itwang.packingmanagement.controller;

import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.ParkingCardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("/parkingCard")
@Api(tags = "停车卡管理",value = "停车卡相关接口")
public class ParkingCardController {
    @Autowired
    private ParkingCardService parkingCardService;

    @ApiOperation(value = "查询停车卡")
    @GetMapping("getParkingCard")
    public Result getParkingCard(String cardNumber, int currentPage, int pageSize){
        Result result = parkingCardService.queryParkingCard(cardNumber, currentPage, pageSize);
        return result;
    }

    @ApiOperation(value = "自动补全")
    @GetMapping("getParkingCardCompletion")
    public Result getParkingCardCompletion(@RequestParam String prefix)throws IOException {
        Result result = parkingCardService.queryParkingCardCompletion(prefix);
        return result;
    }

    @ApiOperation(value = "充值")
    @PutMapping("reviseCardBalance")
    public Result reviseCardBalance(@RequestParam Integer parkingCardId,@RequestParam int money){
        Result result = parkingCardService.modifyCardBalance(parkingCardId, money);
        return result;
    }

    @ApiOperation(value = "导出数据")
    @GetMapping("exportParkingCard")
    public void exportParkingCard(HttpServletResponse response) throws IOException{
        parkingCardService.exportParkingCard(response);
    }

    @ApiOperation(value = "查询停车卡id")
    @GetMapping("getCardNumber")
    public Result getCardNumber(@RequestParam String cardOwner){
        Result result = parkingCardService.queryCardId(cardOwner);
        return result;
    }

    @ApiOperation(value = "查询办卡数量")
    @GetMapping("getCardCount")
    public Result getCardCount(@RequestParam String startTime,@RequestParam String stopTime){
        Result result = parkingCardService.queryCardCount(startTime, stopTime);
        return result;
    }
}
