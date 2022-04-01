package cn.itwang.packingmanagement.service;

import cn.itwang.packingmanagement.domain.Parking;
import cn.itwang.packingmanagement.entity.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ParkingService extends IService<Parking> {
    //查询停车场信息
    public Result queryParkingInformation(String parkingName, int currentPage, int pageSize);
    //导出到本地
    public void exportParking(HttpServletResponse response) throws IOException;
    //添加停车场信息
    public Result addParkingInformation(Parking parking) throws IOException;
    //修改停车场信息
    public Result modifyParkingInformation(Parking parking) throws IOException;
    //删除停车场信息
    public Result removeParkingInformation(Integer parkingId) throws IOException;
    //根据id添加停车场车数量
    public Result modifyAddParkingCarCountById(Integer paringId);
    //根据id减少停车场车数量
    public Result modifyReduceParkingCarCountById(Integer paringId);
    //自动补全
    public Result queryParkingCompletion(String prefix) throws IOException;
}
