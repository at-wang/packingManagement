package cn.itwang.packingmanagement.controller;

import cn.itwang.packingmanagement.domain.ParkingRecord;
import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.ParkingRecordService;
import cn.itwang.packingmanagement.service.ParkingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

@CrossOrigin
@RestController
@Api(tags = "停车记录管理",value = "停车记录相关接口")
@RequestMapping("/parkingRecord")
public class ParkingRecordController {

    @Autowired
    private ParkingRecordService parkingRecordService;

    @ApiOperation(value = "查询停车记录")
    @GetMapping("getParkingRecord")
    public Result getParkingRecord(@RequestParam String licencePlate,@RequestParam int currentPage,@RequestParam int pageSize) {
        Result result = parkingRecordService.queryParkingRecord(licencePlate, currentPage, pageSize);
        return result;
    }


    @ApiOperation(value = "删除停车记录")
    @DeleteMapping("cancelParkingRecord")
    public Result cancelParkingRecord(@RequestParam Integer parkingRecordId){
        Result result = parkingRecordService.removeParkingRecordById(parkingRecordId);
        return result;
    }

    @ApiOperation(value = "导出停车记录")
    @GetMapping("exportParkingRecord")
    public void exportParkingRecord(HttpServletResponse response) throws IOException {
        parkingRecordService.exportParkingRecord(response);
    }

    @ApiOperation(value = "自动补全")
    @GetMapping("getParkingRecordCompletion")
    public Result getParkingRecordCompletion(String prefix) throws IOException{
        Result result = parkingRecordService.queryParkingRecordCompletion(prefix);
        return result;
    }

    @ApiOperation(value = "添加停车记录")
    @PostMapping("saveParkingRecord")
    public Result saveParkingRecord(@RequestBody ParkingRecord parkingRecord) throws IOException {
        Result result = parkingRecordService.addParkingRecord(parkingRecord);
        return result;
    }

    @ApiOperation(value = "修改车辆离开时间")
    @PutMapping("reviseAwayTime")
    public Result reviseAwayTime(@RequestParam Integer parkingRecordId){
        Result result = parkingRecordService.modifyAwayTime(parkingRecordId);
        return result;
    }
}
