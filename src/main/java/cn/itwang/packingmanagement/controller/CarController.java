package cn.itwang.packingmanagement.controller;


import cn.itwang.packingmanagement.domain.Car;
import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.CarService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("/car")
@Api(tags = "停车管理",value = "车辆相关接口")
public class CarController {

    @Autowired
    private CarService carService;

    @ApiOperation(value = "查询车辆信息")
    @GetMapping("getCarInformation")
    public Result getCarInformation(String licencePlate, int currentPage, int pageSize){
        Result result = carService.queryCarInformation(licencePlate, currentPage, pageSize);
        return result;
    }

    @ApiOperation("导出车辆信息")
    @GetMapping("exportCar")
    public void exportCar(HttpServletResponse response) throws IOException{
        carService.exportCar(response);
    }

    @ApiOperation("添加车辆信息")
    @PostMapping("saveCarInformation")
    public Result saveCarInformation(@RequestBody Car car) throws IOException {
        Result result = carService.addCarInformation(car);
        return result;
    }

    @ApiOperation("修改车辆信息")
    @PutMapping("reviseCarInformation")
    public Result reviseCarInformation(@RequestBody Car car) throws IOException {
        Result result = carService.modifyCarInformation(car);
        return result;
    }

    @ApiOperation("删除车辆信息")
    @DeleteMapping("cancelCarInformation")
    public Result cancelCarInformation(@RequestParam Integer carId) throws IOException {
        Result result = carService.removeCarInformation(carId);
        return result;
    }

    @ApiOperation("自动补全搜索")
    @GetMapping("getCarCompletion")
    public Result getCarCompletion(@RequestParam String prefix) throws IOException {
        Result result = carService.queryCarCompletion(prefix);
        return result;
    }

    @ApiOperation("修改停车状态")
    @PutMapping("reviseCarState")
    public Result reviseCarState(@RequestParam Integer carId,@RequestParam String state){
        Result result = carService.modifyCarState(carId, state);
        return result;
    }

    @ApiOperation("查询停车类型数量")
    @GetMapping("getCarTypeCount")
    public Result getCarTypeCount(@RequestParam String type){
        Result result = carService.queryCarTypeCount(type);
        return result;
    }
}
