package cn.itwang.packingmanagement.controller;


import cn.itwang.packingmanagement.domain.Parking;
import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.ParkingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/parking")
@Api(tags = "停车场管理",value = "停车场相关接口")
public class ParkingController {

    @Autowired
    private ParkingService parkingService;

    @ApiOperation(value = "查询停车场信息")
    @GetMapping("getParkingInformation")
    public Result getParkingInformation(String parkingName,int currentPage,int pageSize){
        Result result = parkingService.queryParkingInformation(parkingName, currentPage, pageSize);
        return result;
    }

    @ApiOperation(value = "导出停车场信息")
    @GetMapping("exportParking")
    public void exportParking(HttpServletResponse response) throws IOException {
        parkingService.exportParking(response);

    }

    @ApiOperation(value = "添加停车场信息")
    @PostMapping("saveParkingInformation")
    public Result saveParkingInformation(@RequestBody Parking parking) throws IOException {
        Result result = parkingService.addParkingInformation(parking);
        return result;
    }

    @ApiOperation(value = "修改停车场信息")
    @PutMapping("reviseParkingInformation")
    public Result reviseParkingInformation(@RequestBody Parking parking) throws IOException {
        Result result = parkingService.modifyParkingInformation(parking);
        return result;
    }


    @ApiOperation(value = "删除停车场信息")
    @DeleteMapping("cancelParkingInformation")
    public Result cancelParkingInformation(@RequestParam Integer parkingId) throws IOException {
        Result result = parkingService.removeParkingInformation(parkingId);
        return result;
    }

    @ApiOperation(value = "根据id添加车数量")
    @PutMapping("reviseAddParkingCarCountById")
    public Result reviseAddParkingCarCountById(@RequestParam Integer parkingId){
        Result result = parkingService.modifyAddParkingCarCountById(parkingId);
        return result;
    }

    @ApiOperation(value = "根据id减少车数量")
    @PutMapping("reviseReduceParkingCarCountById")
    public Result reviseReduceParkingCarCountById(@RequestParam Integer parkingId){
        Result result = parkingService.modifyReduceParkingCarCountById(parkingId);
        return result;
    }

    @ApiOperation(value = "自动补全")
    @GetMapping("getParkingCompletion")
    public Result getParkingCompletion(@RequestParam String prefix) throws IOException {
        Result result = parkingService.queryParkingCompletion(prefix);
        return result;
    }

}
