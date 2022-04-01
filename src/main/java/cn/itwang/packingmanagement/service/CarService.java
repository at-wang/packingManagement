package cn.itwang.packingmanagement.service;

import cn.itwang.packingmanagement.domain.Car;
import cn.itwang.packingmanagement.entity.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


public interface CarService extends IService<Car> {
    //查询car信息
    public Result queryCarInformation(String licencePlate,int currentPage,int pageSize);
    //导出车辆信息
    public void exportCar(HttpServletResponse response) throws IOException;
    //添加车辆信息
    public Result addCarInformation(Car car) throws IOException;
    //修改车辆信息
    public Result modifyCarInformation(Car car) throws IOException;
    //删除车辆信息
    public Result removeCarInformation(Integer carId) throws IOException;
    //查询用户id是否存在
    public boolean queryUserIdExist(Integer userId);
    //搜索自动补全
    public Result queryCarCompletion(String prefix) throws IOException;
    //修改停车状态
    public Result modifyCarState(Integer carId,String state);
    //查询车辆类型的数量
    public Result queryCarTypeCount(String type);
}
